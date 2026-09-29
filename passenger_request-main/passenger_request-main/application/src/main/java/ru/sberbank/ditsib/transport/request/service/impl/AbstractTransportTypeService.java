package ru.sberbank.ditsib.transport.request.service.impl;

import static ru.sberbank.ditsib.transport.request.util.RequestHelper.waypointToWaypointDTO;

import jakarta.validation.constraints.NotNull;

import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.CheckinSettings;
import ru.sberbank.ditsib.transport.request.database.model.FraudData;
import ru.sberbank.ditsib.transport.request.database.model.FraudType;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.fraud.EasupAbsenceRequest;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.request.exceptions.DurationLimitExceededException;
import ru.sberbank.ditsib.transport.request.exceptions.EmployeeAbsenceException;
import ru.sberbank.ditsib.transport.request.exceptions.MultipointLimitExceededException;
import ru.sberbank.ditsib.transport.request.service.CheckinSettingsService;
import ru.sberbank.ditsib.transport.request.service.FraudMonitoringService;
import ru.sberbank.ditsib.transport.request.service.FraudService;
import ru.sberbank.ditsib.transport.request.service.RegionDataResolver;
import ru.sberbank.ditsib.transport.request.service.TransportTypeService;
import ru.sberbank.ditsib.transport.request.service.grpc.DurationRequestCheckGrpcClient;
import ru.sberbank.ditsib.transport.request.service.grpc.EasupGrpcService;
import ru.sberbank.ditsib.transport.request.service.grpc.OverrunRequestCheckGrpcClient;
import ru.sberbank.ditsib.transport.request.service.grpc.RequestChecksGrpcService;
import ru.sberbank.ditsib.transport.request.util.DateTimeUtil;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractTransportTypeService<T extends Request> implements TransportTypeService<T> {

    protected static final String SPLIT_DETECTED_FRAUD_COMMENT = "Обнаружены признаки дробления";
    protected static final String ABSENT_EMPLOYEE_FRAUD_COMMENT = "На выбранные дату/время пассажир в отпуске/на больничном";
    protected static final String DURATION_FRAUD_COMMENT = "Превышен лимит длительности поездок";
    protected static final String SINGLE_TRIP_DURATION_FRAUD_COMMENT = "Превышен лимит длительности одной заявки";
    protected static final String TOTAL_WAIT_TIME_FRAUD_COMMENT = "Время ожидания такси превышает %d минут и противоречит условиям предоставления такси";
    public static final int METER_IN_KILOMETER = 1000;
    @Getter(AccessLevel.PROTECTED)
    private final JpaRepository<T, UUID> repository;
    private final CheckinSettingsService checkinSettingsService;
    private final RegionDataResolver regionDataResolver;
    protected final EntityDTOMapper mapper;
    private final EasupGrpcService easupGrpcService;
    private final FraudService fraudService;
    private final RequestChecksGrpcService requestChecksGrpcService;
    private final DurationRequestCheckGrpcClient durationRequestCheckGrpcClient;
    private final OverrunRequestCheckGrpcClient overrunRequestCheckGrpcClient;
    private final FraudMonitoringService fraudMonitoringService;
    @Value("${default-timezone:Z}")
    protected String defaultTimezone;

    @Value("${request.validation.multipoint.enabled:true}")
    protected boolean isMultipointValidationEnabled;

    @Value("${request.fraud.single-trip-duration-limit-ms:28800000}")
    protected long singleTripDurationLimitMs;

    @Getter
    protected TransportTypeEnum transportType;

    @Override
    public T save(T request) {
        return repository.save(request);
    }

    protected void enrichWaypointsWithRadius(Request request, TransportTypeEnum transportType) {
        for (var waypoint : request.getWaypoints()) {
            var waypointDTO = waypointToWaypointDTO(waypoint);
            var regionDto = regionDataResolver.getRegion(waypointDTO);
            CheckinSettings checkinSettings =
                    regionDto == null
                            ? null
                            : checkinSettingsService.getByParams(TransportServiceType.EMPLOYEE_TRANSPORTATION,
                            transportType,
                            regionDto.getId()
                    );
            waypoint.setRadius(500);
            waypoint.setCheckinOnlyManual(false);
            if (checkinSettings != null) {
                waypoint.setRadius(checkinSettings.getRadius());
                waypoint.setCheckinOnlyManual(checkinSettings.getCheckinOnlyManual());
            }
        }
    }

    protected boolean checkOnAbsence(@NotNull Request request, @NotNull Employee employee,
                                     Optional<String> timeZone) {
        Optional<Long> expectedDuration = request.getExpected().getTime() == null ?
                Optional.empty() : Optional.of(request.getExpected().getTime().toMillis());
        var zone = timeZone.orElse(request.getTimeZone());
        if (request.getDesiredDate() != null) {
            var absenceRequest = new EasupAbsenceRequest(
                    request.getDesiredDate().toInstant(ZoneOffset.UTC).toEpochMilli(),
                    zone,
                    request.getPassenger().getPersonnelNumber(),
                    expectedDuration.orElse(null));
            var response = easupGrpcService.resolveAbsence(absenceRequest);
            if (response.isEmpty()) {
                return false;
            }
            if (employee.getPersonnelNumber().equals(request.getPassenger().getPersonnelNumber())) {
                throw new EmployeeAbsenceException();
            }
        }
        return true;
    }

    protected void checkMultipointLimit(@NotNull Request request, Optional<String> timeZone) {
        if (isMultipointValidationEnabled) {
            var waypointsCount = request.getWaypoints().size();
            if (request.getWaypoints().size() > 2) {
                log.debug("Checking multipoint limit started for passenger {} with {} waypoints",
                        request.getPassenger().getId(), waypointsCount);
                var zone = timeZone.orElse(request.getTimeZone());
                var isLimitExceeded = requestChecksGrpcService.isMultipointLimitExceeded(
                        request.getPassenger().getId(),
                        request.getDesiredDate(),
                        zone
                );

                if (isLimitExceeded) {
                    log.debug("Checking multipoint limit finished for passenger {} with {} waypoints. Limit exceeded",
                            request.getPassenger().getId(), waypointsCount);
                    throw new MultipointLimitExceededException();
                }
                log.debug("Checking multipoint limit finished for passenger {} with {} waypoints. Limit not exceeded",
                        request.getPassenger().getId(), waypointsCount);
            }
        }
    }

    /**
     * Проверка длительности поездки через gRPC.
     * Вызывается после checkOnAbsence() и checkMultipointLimit().
     * Бросает DurationLimitExceededException при превышении лимита.
     * <p>
     * Важное поведение:
     * - метод сохраняет сигнатуру, принимает Optional<String> timeZone;
     * - сначала вычисляет ZoneId безопасно через resolveZoneId;
     * - форматирует строку для передачи в gRPC через formatTimeZone;
     * - при некорректном идентификаторе часовой зоны не бросает исключение, а использует fallback (clock.getZone()).
     *
     * @param request  Заявка
     * @param timeZone Часовой пояс (Optional)
     */
    protected void checkDurationLimit(@NotNull Request request, Optional<String> timeZone) {
        if (request.getExpected().getTime() == null) return;

        var tzCandidate = timeZone.orElse(request.getTimeZone());
        var zoneId = DateTimeUtil.resolveZoneId(tzCandidate);

        var desiredDateWithOffset = request.getDesiredDate().atZone(zoneId).toOffsetDateTime();
        var formattedTimeZone = DateTimeUtil.formatTimeZone(tzCandidate);

        log.debug("Calling duration check for passenger: {}, desiredDate: {}, expectedDuration: {}, timeZone: {}",
                request.getPassenger().getId(), desiredDateWithOffset, request.getExpected().getTime().toMillis(), formattedTimeZone);
        try {
            durationRequestCheckGrpcClient.checkDurationLimit(
                    request.getPassenger().getId(),
                    desiredDateWithOffset,
                    request.getExpected().getTime().toMillis(),
                    formattedTimeZone
            );
        } catch (DurationLimitExceededException e) {
            log.debug("Duration limit exceeded for passenger: {}, desiredDate: {}, expectedDuration: {}, timeZone: {}",
                    request.getPassenger().getId(), desiredDateWithOffset, request.getExpected().getTime().toMillis(), formattedTimeZone);
            createFraud(request, FraudType.DURATION, DURATION_FRAUD_COMMENT);
        }
    }

    /**
     * Проверка превышения лимита суммарного километража через gRPC.
     * При наличии комментария в ответе сервиса request_checks создает fraud запись
     * с комментарием, склеенным из comment + totalDistance + " км".
     *
     * @param request  Заявка
     * @param timeZone Часовой пояс (Optional)
     */
    protected void checkOverrunLimit(@NotNull Request request, Optional<String> timeZone) {
        if (request.getExpected().getTime() == null) return;
        if (request.getExpected().getDistance() == null) {
            log.debug("Could not check overrun limit: expected distance is null");
            return;
        }

        var tzCandidate = timeZone.orElse(request.getTimeZone());
        var zoneId = DateTimeUtil.resolveZoneId(tzCandidate);

        var desiredDateWithOffset = request.getDesiredDate().atZone(zoneId).toLocalDateTime();
        var formattedTimeZone = DateTimeUtil.formatTimeZone(tzCandidate);

        log.debug("Calling overrun check for passenger: {}, expectedDistance: {}, timeZone: {}",
                request.getPassenger().getId(), request.getExpected().getDistance(), tzCandidate);

        var response = overrunRequestCheckGrpcClient.checkOverrunLimit(
                request.getPassenger().getId(),
                desiredDateWithOffset,
                (int) Math.round(request.getExpected().getDistance() * METER_IN_KILOMETER),
                formattedTimeZone
        );

        if (response != null && response.getComment() != null && !response.getComment().isEmpty()) {
            var comment = response.getComment() + " " + ((double) response.getTotalDistance() / METER_IN_KILOMETER) + " км";
            log.debug("Overrun limit exceeded for passenger {}: {}", request.getPassenger().getId(), comment);
            createFraud(request, FraudType.OVERRUN, comment);
        }
    }

    /**
     * Проверка превышения длительности одной заявки.
     * Если ожидаемая длительность поездки превышает заданный порог (по умолчанию 8 часов),
     * создаётся fraud-запись с типом SINGLE_TRIP_DURATION.
     * Применяется для всех типов транспорта, кроме GROUP_TRANSFER.
     *
     * @param request Заявка
     */
    protected void checkSingleTripDuration(@NotNull Request request) {
        if (request.getExpected().getTime() == null) {
            return;
        }

        var expectedDuration = request.getExpected().getTime().toMillis();
        if (expectedDuration > singleTripDurationLimitMs) {
            log.debug("Single trip duration exceeded for request: requestId={}, expectedDuration={}ms, limit={}ms",
                    request.getId(), expectedDuration, singleTripDurationLimitMs);
            createFraud(request, FraudType.SINGLE_TRIP_DURATION, SINGLE_TRIP_DURATION_FRAUD_COMMENT);
        }
    }

    @Transactional
    public void createFraud(Request entity, FraudType type, String comment) {
        try {
            final var fraudData = new FraudData();
            fraudData.setRequest(entity);
            fraudData.setType(type);
            fraudData.setComment(comment);
            entity.getFraudData().add(fraudData);
            fraudService.saveAndSend(fraudData);

            fraudMonitoringService.send(entity.getId(), type, comment);
            log.info("Fraud monitoring message sent for request: requestId={}, fraudType={}", entity.getId(), type);
        } catch (DataAccessException e) {
            log.error("Database error while saving fraud | requestId={} | type={} | error={}",
                    entity.getId(), type, e.getMessage());
        } catch (Exception e) {
            log.error("Error while creating fraud | requestId={} | type={} | error={}",
                    entity.getId(), type, e.getMessage());
        }
    }
}
