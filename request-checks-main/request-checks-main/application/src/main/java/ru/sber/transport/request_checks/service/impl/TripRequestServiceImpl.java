package ru.sber.transport.request_checks.service.impl;

import static ru.sber.transport.request_checks.util.Constants.METERS_IN_KILOMETER;
import static ru.sber.transport.request_checks.util.ErrorMessages.MULTIPOINT_LIMIT;
import static ru.sber.transport.request_checks.util.ErrorMessages.OVERRUN_LIMIT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sber.transport.request_checks.dto.DurationCheckRequestDto;
import ru.sber.transport.request_checks.dto.MultipointCheckRequestDto;
import ru.sber.transport.request_checks.dto.OverrunCheckRequestDto;
import ru.sber.transport.request_checks.dto.OverrunCheckResponseDto;
import ru.sber.transport.request_checks.exception.DurationLimitExceededException;
import ru.sber.transport.request_checks.exception.MultipointRequestsLimitExceededException;
import ru.sber.transport.request_checks.mapper.TripRequestMapper;
import ru.sber.transport.request_checks.mapper.WaypointMapper;
import ru.sber.transport.request_checks.messaging.message.ExternalRequestMessage;
import ru.sber.transport.request_checks.repository.TripRequestRepository;
import ru.sber.transport.request_checks.repository.WaypointRepository;
import ru.sber.transport.request_checks.service.TripRequestService;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripRequestServiceImpl implements TripRequestService {

    private final TripRequestRepository tripRequestRepository;

    private final WaypointRepository waypointRepository;

    private final TripRequestMapper tripRequestMapper;

    private final WaypointMapper waypointMapper;

    @Value("${request-checks.multipoint.max}")
    private int maxMultipointRequests;

    @Value("${request-checks.duration.max}")
    private long maxDuration;

    @Value("${request-checks.duration.day-start-hour}")
    private int dayStartHour;

    @Value("${request-checks.duration.tz-for-day-start}")
    private String dayStartTz;

    @Value("${request-checks.maxMonthlyDistance}")
    private int maxMonthlyDistance;

    @Override
    @Transactional
    public void saveFromExternalMessage(ExternalRequestMessage message) {
        val tripRequest = tripRequestMapper.externalRequestMessageToTripRequestEntity(message);
        val waypoints = message.getWaypoints().stream()
            .map(waypointMessage -> waypointMapper.waypointMessageToWaypointEntity(
                waypointMessage,
                message.getWaypoints().indexOf(waypointMessage),
                tripRequest.getId()))
            .toList();
        tripRequestRepository.save(tripRequest);
        waypointRepository.saveAll(waypoints);
    }

    @Override
    @Transactional
    public void saveFromRequestMessage(RequestMessage message) {
        val tripRequest = tripRequestMapper.requestMessageToTripRequestEntity(message);
        val waypoints = message.getWaypoints().stream()
            .map(waypointMessage -> waypointMapper.requestMessageWaypointToWaypointEntity(
                waypointMessage,
                message.getWaypoints().indexOf(waypointMessage),
                tripRequest.getId()))
            .toList();
        tripRequestRepository.save(tripRequest);
        waypointRepository.saveAll(waypoints);
    }

    @Override
    @Transactional(readOnly = true)
    public void checkMultipointLimit(MultipointCheckRequestDto request) {
        val count = tripRequestRepository.countMultipointRequests(request.passengerId(), request.desiredDate(),
                request.timeZone());

        if (count >= maxMultipointRequests) {
            log.warn("Превышен лимит многоточечных поездок: passengerId={}, desiredDate={}, count={}, limit={}",
                    request.passengerId(), request.desiredDate(), count, maxMultipointRequests);
            throw new MultipointRequestsLimitExceededException(MULTIPOINT_LIMIT);
        }

        log.debug("Проверка многоточечных поездок пройдена: passengerId={}, desiredDate={}, count={}, limit={}",
                request.passengerId(), request.desiredDate(), count, maxMultipointRequests);
    }

    @Override
    @Transactional(readOnly = true)
    public void checkDurationLimit(DurationCheckRequestDto request) {
        UUID passengerId = request.passengerId();
        OffsetDateTime desiredDate = request.desiredDate();
        String reqTimeZone = request.timeZone();
        long expectedDurationMs = request.expectedDuration();
        long expectedDurationSeconds = (expectedDurationMs + 999L) / 1000L;

        long totalDurationByDateSeconds = tripRequestRepository.sumDurationByDate(
                passengerId, desiredDate, reqTimeZone, dayStartHour, dayStartTz);
        long totalWithNewSeconds = totalDurationByDateSeconds + expectedDurationSeconds;
        long maxDurationSeconds = maxDuration / 1000L;

        if (totalWithNewSeconds > maxDurationSeconds) {
            log.warn("Превышена длительность поездок: passengerId={}, desiredDate={}, totalDurationByDateSeconds={}, expectedDurationMs={}, maxDurationMs={}",
                    passengerId, desiredDate, totalDurationByDateSeconds, expectedDurationMs, maxDuration);
            throw new DurationLimitExceededException("Превышена продолжительность поездок в 12 часов в сутки");
        }

        log.debug("Проверка длительности поездок пройдена: passengerId={}, desiredDate={}, totalDurationByDateSeconds={}, expectedDurationMs={}, maxDurationMs={}",
                passengerId, desiredDate, totalDurationByDateSeconds, expectedDurationMs, maxDuration);
    }

    @Override
    @Transactional(readOnly = true)
    public OverrunCheckResponseDto checkOverrunLimit(OverrunCheckRequestDto request) {
        UUID passengerId = request.passengerId();
        OffsetDateTime desiredDate = request.desiredDate();

        ZoneId zoneId = ZoneId.of(request.timeZone());

        ZonedDateTime zoned = desiredDate.atZoneSameInstant(zoneId);
        YearMonth ym = YearMonth.from(zoned);

        ZonedDateTime startOfMonth = ym.atDay(1).atStartOfDay(zoneId);
        ZonedDateTime startOfNextMonth = ym.plusMonths(1).atDay(1).atStartOfDay(zoneId);

        int sumDistance = tripRequestRepository.sumDistanceByPassengerAndDesiredTime(
                passengerId,
                startOfMonth.toOffsetDateTime(),
                startOfNextMonth.toOffsetDateTime()
        );
        int totalDistance = sumDistance + request.expectedDistance();
        return totalDistance > maxMonthlyDistance ? new OverrunCheckResponseDto(OVERRUN_LIMIT, totalDistance) : new OverrunCheckResponseDto(null, totalDistance);
    }

}
