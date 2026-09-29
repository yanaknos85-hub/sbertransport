package ru.sberbank.ditsib.transport.request.service.validate.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.config.personal.TransportRequestSplitCheckProperties;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForPersonalRepository;
import ru.sberbank.ditsib.transport.request.database.dao.projection.RequestForPersonalSplitCheckProjection;
import ru.sberbank.ditsib.transport.request.dto.validation.ExistingConflictRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.validation.TripSplitCheckDTO;
import ru.sberbank.ditsib.transport.request.dto.validation.TripSplitCheckResultDTO;
import ru.sberbank.ditsib.transport.request.service.validate.TripSplitCheckService;
import ru.sberbank.ditsib.transport.request.util.DateTimeUtil;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripSplitCheckServiceImpl implements TripSplitCheckService {

    private final TransportRequestSplitCheckProperties personalTransportRequestCheckProperties;
    private final RequestForPersonalRepository requestRepository;

    @Override
    public TripSplitCheckResultDTO check(TripSplitCheckDTO tripSplitCheckDTO) {
        log.debug("Проверка на дробление поездок: {}", tripSplitCheckDTO);

        final var costThreshold = personalTransportRequestCheckProperties.getPersonal().getCostThreshold();
        final var durationThreshold = personalTransportRequestCheckProperties.getPersonal().getDurationThreshold();
        final var requestCountThreshold = personalTransportRequestCheckProperties.getPersonal().getRequestCountThreshold();
        final var ignoreStatuses = personalTransportRequestCheckProperties.getPersonal().getIgnoreStatuses();

        final var expectedCost = tripSplitCheckDTO.expectedCost();
        final var employeeId = tripSplitCheckDTO.employeeId();

        if (expectedCost > costThreshold) {
            log.debug("Стоимость поездки {} (коп.) > {} (коп.) — пропускаем проверку.", expectedCost, costThreshold);
            return new TripSplitCheckResultDTO(true, null);
        }

        final var startDateTimeOfNewRequest = computeStartDateTimeOfNewRequest(tripSplitCheckDTO);
        final var existingRequests = findExistingRequestsInStartDateTimeOfNewRequest(employeeId, ignoreStatuses, startDateTimeOfNewRequest, costThreshold);

        if (existingRequests.size() < requestCountThreshold) {
            log.debug("Найдено заявок: {}(шт). Проверка интервалов не требуется.", existingRequests.size());
            return new TripSplitCheckResultDTO(true, null);
        }

        final var newRequestDuration = Duration.ofMillis(tripSplitCheckDTO.expectedDuration());

        log.debug("Проверка на дробление поездок пройдена успешно.");

        final var checkAfterLastRequestResult = checkAfterLastRequest(existingRequests, startDateTimeOfNewRequest, durationThreshold);
        final var checkBeforeLastRequestResult = checkBeforeLastRequest(existingRequests, startDateTimeOfNewRequest, newRequestDuration, durationThreshold);

        final var result = prepareResult(checkAfterLastRequestResult, checkBeforeLastRequestResult);
        log.debug("Проверка на дробление поездок завершена. Заявка является дроблением? {}", result.isValid() ? "НЕТ" : "ДА");

        return result;
    }

    private TripSplitCheckResultDTO prepareResult(TripSplitCheckResultDTO checkAfterLastRequestResult, TripSplitCheckResultDTO checkBeforeLastRequestResult) {
        log.debug("Подготовка результата проверки: {}, {}", checkAfterLastRequestResult, checkBeforeLastRequestResult);
        if (checkAfterLastRequestResult.isValid() && checkBeforeLastRequestResult.isValid()) {
            return new TripSplitCheckResultDTO(true, null);
        }

        if (!checkAfterLastRequestResult.isValid() && checkAfterLastRequestResult.existingConflictRequest() != null) {
            return checkAfterLastRequestResult;
        }

        if (!checkBeforeLastRequestResult.isValid() && checkBeforeLastRequestResult.existingConflictRequest() != null) {
            return checkBeforeLastRequestResult;
        }

        return new TripSplitCheckResultDTO(false, null);
    }

    private static LocalDateTime computeStartDateTimeOfNewRequest(TripSplitCheckDTO rqDTO) {
        final var desiredDateMillis = rqDTO.desiredDate();
        final var userZone = DateTimeUtil.parseTimeZone(rqDTO.timeZone());

        return Instant.ofEpochMilli(desiredDateMillis)
                .atZone(userZone)
                .toLocalDateTime();
    }

    private List<RequestForPersonalSplitCheckProjection> findExistingRequestsInStartDateTimeOfNewRequest(UUID employeeId,
                                                                                                         List<TripRequestStatus> ignoreStatuses,
                                                                                                         LocalDateTime newRequestStartDateTime,
                                                                                                         Double costThreshold) {
        final var requestDay = newRequestStartDateTime.toLocalDate();
        final var startOfDay = requestDay.atStartOfDay();
        final var endOfDay = requestDay.plusDays(1).atStartOfDay();
        log.debug("Получаем заявки по сотруднику {} за день с {}(включительно) по {}(не включительно) с минимальной стоимостью {}.", employeeId, startOfDay, endOfDay, costThreshold);

        List<RequestForPersonalSplitCheckProjection> existingRequestsForPersonal =
                requestRepository.findSplitCheckProjectionsByPassengerIdAndStatusNotInAndDesiredDateBetween(
                                employeeId,
                                startOfDay,
                                endOfDay,
                                ignoreStatuses,
                                costThreshold
                        ).stream()
                        .sorted(Comparator.comparing(RequestForPersonalSplitCheckProjection::desiredDate))
                        .toList();
        log.debug("Найдено заявок: {}", existingRequestsForPersonal);

        return existingRequestsForPersonal;
    }

    private TripSplitCheckResultDTO checkAfterLastRequest(List<RequestForPersonalSplitCheckProjection> existingRequests, LocalDateTime newRequestStart,
                                          int minDuration) {
        log.debug("Проверяем, что новая заявка не начинается слишком рано после предыдущей. Начало новой заявки: {}.", newRequestStart);

        final var existingBeforeNew = existingRequests.stream()
                .filter(r -> {
                    final var zoneId = DateTimeUtil.parseTimeZone(r.employeeDeviceTimeZone());
                    final var start = r.desiredDate().atZone(ZoneOffset.UTC).withZoneSameInstant(zoneId).toLocalDateTime();
                    final var end = start.plus(r.expectedDuration());
                    return end.isBefore(newRequestStart);
                })
                .max(Comparator.comparing(r -> {
                            final var zoneId = DateTimeUtil.parseTimeZone((r.employeeDeviceTimeZone()));
                            final var startInLocal = r.desiredDate().atZone(ZoneOffset.UTC).withZoneSameInstant(zoneId).toLocalDateTime();
                            return startInLocal.plus(r.expectedDuration());
                        }
                ))
                .orElse(null);

        if (existingBeforeNew == null) {
            log.debug("Нет поездок до начала новой — пропускаем проверку.");
            return new TripSplitCheckResultDTO(true, null);
        }
        final var lastZone = ZoneId.of(existingBeforeNew.employeeDeviceTimeZone());
        final var lastStart = existingBeforeNew.desiredDate().atZone(ZoneOffset.UTC).withZoneSameInstant(lastZone).toLocalDateTime();
        final var lastEnd = lastStart.plus(existingBeforeNew.expectedDuration());

        final var minutesAfter = ChronoUnit.MINUTES.between(lastEnd, newRequestStart);

        if (minutesAfter < minDuration) {
            log.error("Слишком малый интервал после предыдущей поездки: [{} / {}] , {} мин.", existingBeforeNew.id(), existingBeforeNew.humanReadableId(), minutesAfter);
            final var existingConflictRequest = new ExistingConflictRequestDTO(existingBeforeNew.id(), existingBeforeNew.humanReadableId(), existingBeforeNew.status().name());
            return new TripSplitCheckResultDTO(false, existingConflictRequest);
        }

        return new TripSplitCheckResultDTO(true, null);
    }

    private TripSplitCheckResultDTO checkBeforeLastRequest(List<RequestForPersonalSplitCheckProjection> existingRequests,
                                                           LocalDateTime newRequestStart,
                                                           Duration newRequestDuration, int minDuration) {
        log.debug("Проверяем, что перед следующей заявкой достаточно времени. Начало новой заявки: {}.", newRequestStart);

        final var newRequestEnd = newRequestStart.plus(newRequestDuration);

        final var existingAfterNew = existingRequests.stream()
                .filter(r -> {
                    final var zoneId = DateTimeUtil.parseTimeZone(r.employeeDeviceTimeZone());
                    final var startInLocal = r.desiredDate().atZone(ZoneOffset.UTC).withZoneSameInstant(zoneId).toLocalDateTime();
                    return startInLocal.isAfter(newRequestEnd);
                })
                .min(Comparator.comparing(r -> {
                    final var zoneId = DateTimeUtil.parseTimeZone(r.employeeDeviceTimeZone());
                    return r.desiredDate().atZone(ZoneOffset.UTC).withZoneSameInstant(zoneId).toLocalDateTime();
                }))
                .orElse(null);

        if (existingAfterNew == null) {
            log.debug("Заявок после новой нет — пропускаем проверку.");
            return new TripSplitCheckResultDTO(true, null);
        }

        final var nextZone = DateTimeUtil.parseTimeZone(existingAfterNew.employeeDeviceTimeZone());
        final var nextStart = existingAfterNew.desiredDate().atZone(ZoneOffset.UTC).withZoneSameInstant(nextZone).toLocalDateTime();

        final var minutesBefore = ChronoUnit.MINUTES.between(newRequestEnd, nextStart);

        if (minutesBefore < minDuration) {
            log.error("Слишком малый интервал перед следующей поездкой: [{} / {}] , {} мин.", existingAfterNew.id(), existingAfterNew.humanReadableId(), minutesBefore);
            final var existingConflictRequest = new ExistingConflictRequestDTO(existingAfterNew.id(), existingAfterNew.humanReadableId(), existingAfterNew.status().name());
            return new TripSplitCheckResultDTO(false, existingConflictRequest);
        }
        return new TripSplitCheckResultDTO(true, null);
    }
}
