package ru.sber.transport.request_checks.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static ru.sber.transport.request_checks.util.Constants.METERS_IN_KILOMETER;
import static ru.sber.transport.request_checks.util.Constants.PUBLIC_TRANSPORT_TYPE;
import static ru.sber.transport.request_checks.util.Constants.TIMEZONE_UTC;
import static ru.sber.transport.request_checks.util.Constants.YANDEX_TRANSPORT_TYPE;
import static ru.sber.transport.request_checks.util.ErrorMessages.DURATION_LIMIT;
import static ru.sber.transport.request_checks.util.ErrorMessages.MULTIPOINT_LIMIT;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request.messaging.AddressMessage;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sber.transport.request_checks.RequestChecksApplication;
import ru.sber.transport.request_checks.dto.DurationCheckRequestDto;
import ru.sber.transport.request_checks.dto.MultipointCheckRequestDto;
import ru.sber.transport.request_checks.dto.OverrunCheckRequestDto;
import ru.sber.transport.request_checks.dto.OverrunCheckResponseDto;
import ru.sber.transport.request_checks.entity.TripRequestEntity;
import ru.sber.transport.request_checks.entity.WaypointEntity;
import ru.sber.transport.request_checks.exception.DurationLimitExceededException;
import ru.sber.transport.request_checks.exception.MultipointRequestsLimitExceededException;
import ru.sber.transport.request_checks.messaging.message.ExternalRequestMessage;
import ru.sber.transport.request_checks.util.ExcludedRequestStatuses;
import ru.sber.transport.request_checks.repository.TripRequestRepository;
import ru.sber.transport.request_checks.repository.WaypointRepository;

@SpringBootTest(classes = RequestChecksApplication.class)
@ActiveProfiles({"test"})
@EmbeddedPostgres
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка работы TripRequestService")
class TripRequestServiceTest {

    @Autowired
    private TripRequestService tripRequestService;

    @Autowired
    private TripRequestRepository tripRequestRepository;

    @Autowired
    private WaypointRepository waypointRepository;

    @Test
    @DisplayName("Сохранение заявки и путевых точек из сервиса request-external")
    void testSaveFromExternalMessage() {
        val startWaypoint = ExternalRequestMessage.WaypointMessage.builder()
            .id(UUID.randomUUID())
            .longitude(37.618423)
            .latitude(55.751244)
            .build();
        val endWaypoint = ExternalRequestMessage.WaypointMessage.builder()
            .id(UUID.randomUUID())
            .longitude(38.618423)
            .latitude(56.751244)
            .build();

        val inputExternalMessage = ExternalRequestMessage.builder()
            .id(UUID.randomUUID())
            .humanReadableId("YA-123")
            .passengerId(UUID.randomUUID())
            .date(LocalDateTime.now())
            .timeZone("+3:00")
            .planned(ExternalRequestMessage.PlannedData.builder()
                .distance(1000L)
                .duration("PT30M")
                .build())
            .waypoints(List.of(startWaypoint, endWaypoint))
            .build();

        tripRequestService.saveFromExternalMessage(inputExternalMessage);

        val savedEntity = tripRequestRepository.findById(inputExternalMessage.getId());
        assertThat(savedEntity).isPresent();
        assertThat(savedEntity.get().getId()).isEqualTo(inputExternalMessage.getId());
        assertThat(savedEntity.get().getHumanReadableId()).isEqualTo(inputExternalMessage.getHumanReadableId());
        assertThat(savedEntity.get().getTransportType()).isEqualTo(YANDEX_TRANSPORT_TYPE);
        assertThat(savedEntity.get().getPassengerId()).isEqualTo(inputExternalMessage.getPassengerId());
        assertThat(savedEntity.get().getDesiredDate().truncatedTo(ChronoUnit.MILLIS))
            .isEqualTo(inputExternalMessage.getDate().atOffset(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS));
        assertThat(savedEntity.get().getDistance())
            .isEqualTo(inputExternalMessage.getPlanned().getDistance());
        assertThat(savedEntity.get().getDuration())
            .isEqualTo(Duration.parse(inputExternalMessage.getPlanned().getDuration()).toSeconds());

        val savedWaypoints = waypointRepository.findByTripRequestId(savedEntity.get().getId());
        for (int i = 0; i < savedWaypoints.size(); i++) {
            val waypointEntity = savedWaypoints.get(i);
            assertThat(waypointEntity.getTripRequestId()).isEqualTo(savedEntity.get().getId());
            assertThat(waypointEntity.getOrderingIndex()).isEqualTo(i);
            assertThat(waypointEntity.getLongitude()).isNotNull();
            assertThat(waypointEntity.getLatitude()).isNotNull();
        }
    }

    @Test
    @DisplayName("Сохранение заявки и путевых точек из сервиса request")
    void testSaveFromRequestMessage() {
        val startWaypoint = RequestMessage.Waypoint.builder()
            .id(UUID.randomUUID())
            .orderingIndex(0)
            .address(AddressMessage.builder()
                .longitude(37.618423)
                .latitude(55.751244)
                .build())
            .build();
        val endWaypoint = RequestMessage.Waypoint.builder()
            .id(UUID.randomUUID())
            .orderingIndex(1)
            .address(AddressMessage.builder()
                .longitude(38.618423)
                .latitude(56.751244)
                .build())
            .build();

        val inputRequestMessage = RequestMessage.builder()
            .id(UUID.randomUUID())
            .humanReadableId("OT-456")
            .passengerId(UUID.randomUUID())
            .transportType(PUBLIC_TRANSPORT_TYPE)
            .desiredDate(LocalDateTime.now())
            .timeZone("+3:00")
            .expected(RequestMessage.ExpectedData.builder()
                .distance(1.0)
                .time(Duration.parse("PT30M"))
                .build())
            .waypoints(List.of(startWaypoint, endWaypoint))
            .build();

        tripRequestService.saveFromRequestMessage(inputRequestMessage);

        val savedEntity = tripRequestRepository.findById(inputRequestMessage.getId());
        assertThat(savedEntity).isPresent();
        assertThat(savedEntity.get().getId()).isEqualTo(inputRequestMessage.getId());
        assertThat(savedEntity.get().getHumanReadableId()).isEqualTo(inputRequestMessage.getHumanReadableId());
        assertThat(savedEntity.get().getTransportType()).isEqualTo(inputRequestMessage.getTransportType());
        assertThat(savedEntity.get().getPassengerId()).isEqualTo(inputRequestMessage.getPassengerId());
        assertThat(savedEntity.get().getDesiredDate().truncatedTo(ChronoUnit.MILLIS))
            .isEqualTo(inputRequestMessage.getDesiredDate().atOffset(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS));
        assertThat(savedEntity.get().getDistance())
                .isEqualTo((long) inputRequestMessage.getExpected().getDistance() * METERS_IN_KILOMETER);
        assertThat(savedEntity.get().getDuration())
            .isEqualTo(inputRequestMessage.getExpected().getTime().toSeconds());

        val savedWaypoints = waypointRepository.findByTripRequestId(savedEntity.get().getId());
        for (int i = 0; i < savedWaypoints.size(); i++) {
            val waypointEntity = savedWaypoints.get(i);
            assertThat(waypointEntity.getTripRequestId()).isEqualTo(savedEntity.get().getId());
            assertThat(waypointEntity.getOrderingIndex()).isEqualTo(i);
            assertThat(waypointEntity.getLongitude()).isNotNull();
            assertThat(waypointEntity.getLatitude()).isNotNull();
        }
    }

    @Test
    @DisplayName("Проверка многоточечных поездок - лимит не превышен")
    void testCheckMultipointLimitWithinLimit() {
        val passengerId = UUID.randomUUID();
        val dateTime = LocalDateTime.of(2026, 5, 21, 0, 0);

        createMultipointRequests(passengerId, dateTime, 2);
        val request = new MultipointCheckRequestDto(passengerId, dateTime, TIMEZONE_UTC);

        assertDoesNotThrow(() -> tripRequestService.checkMultipointLimit(request));
    }

    @Test
    @DisplayName("Проверка многоточечных поездок - лимит превышен")
    void testCheckMultipointLimitLimitExceeded() {
        val passengerId = UUID.randomUUID();
        val dateTime = LocalDateTime.of(2026, 5, 21, 0, 0);

        createMultipointRequests(passengerId, dateTime, 3);

        val request = new MultipointCheckRequestDto(passengerId, dateTime, TIMEZONE_UTC);

        assertThatThrownBy(() -> tripRequestService.checkMultipointLimit(request))
            .isInstanceOf(MultipointRequestsLimitExceededException.class)
            .hasMessage(MULTIPOINT_LIMIT);
    }

    private TripRequestEntity createTripRequest(UUID passengerId, LocalDateTime date) {
        return TripRequestEntity.builder()
            .id(UUID.randomUUID())
            .humanReadableId("YA-" + System.currentTimeMillis())
            .transportType(YANDEX_TRANSPORT_TYPE)
            .passengerId(passengerId)
            .desiredDate(date.atOffset(ZoneOffset.UTC))
            .timeZone("+3:00")
            .distance(15500)
            .duration(1800L)
            .status("APPROVED")
            .build();
    }

    private List<WaypointEntity> createWaypoints(UUID tripRequestId) {
        return IntStream.range(0, 3)
            .mapToObj(i -> WaypointEntity.builder()
                .id(UUID.randomUUID())
                .tripRequestId(tripRequestId)
                .orderingIndex(i)
                .latitude(55.751244 + i * 0.001)
                .longitude(37.618423 + i * 0.001)
                .build())
            .toList();
    }

    private void createMultipointRequests(UUID passengerId, LocalDateTime dateTime, int count) {
        for (int i = 0; i < count; i++) {
            val tripRequestEntity = createTripRequest(passengerId, dateTime);
            tripRequestRepository.save(tripRequestEntity);
            waypointRepository.saveAll(createWaypoints(tripRequestEntity.getId()));
        }
    }

    @Test
    @DisplayName("Проверка длительности поездок - лимит не превышен")
    void testCheckDurationLimitWithinLimit() {
        val passengerId = UUID.randomUUID();
        val desiredDate = OffsetDateTime.now();
        val expectedDuration = 1800000L; // 30 минут в мс
        val timeZone = "+03:00";
        val request = new DurationCheckRequestDto(passengerId, desiredDate, expectedDuration, timeZone);

        assertDoesNotThrow(() -> tripRequestService.checkDurationLimit(request));
    }

    @Test
    @DisplayName("Проверка длительности поездок - лимит превышен")
    void testCheckDurationLimitLimitExceeded() {
        val passengerId = UUID.randomUUID();
        val desiredDate = OffsetDateTime.now();
        val expectedDuration = 43200001L; // 12 часов + 1 мс (превышает лимит)
        val timeZone = "+03:00";
        val request = new DurationCheckRequestDto(passengerId, desiredDate, expectedDuration, timeZone);

        assertThatThrownBy(() -> tripRequestService.checkDurationLimit(request))
                .isInstanceOf(DurationLimitExceededException.class)
                .hasMessage(DURATION_LIMIT);
    }

    @Test
    @DisplayName("Проверка длительности - добавление поездки в пределах лимита")
    void testCheckDurationLimitWithExistingRequests() {
        val passengerId = UUID.randomUUID();
        val desiredDate = OffsetDateTime.now();
        val timeZone = "+03:00";

        // Создаем существующую поездку на 6 часов (21600 сек)
        val existingTrip = createTripRequest(passengerId, desiredDate, 21600L);
        tripRequestRepository.save(existingTrip);

        // Добавляем поездку на 5 часов (18000 сек) - сумма 11 часов < 12 часов
        val expectedDuration = 18000000L; // 5 часов в мс
        val request = new DurationCheckRequestDto(passengerId, desiredDate, expectedDuration, timeZone);

        assertDoesNotThrow(() -> tripRequestService.checkDurationLimit(request));
    }

    @Test
    @DisplayName("Проверка длительности - сумма превышает лимит")
    void testCheckDurationLimitWithExistingRequestsExceeded() {
        val passengerId = UUID.randomUUID();
        val desiredDate = OffsetDateTime.now();
        val timeZone = "+03:00";

        // Создаем существующую поездку на 10 часов (36000 сек)
        val existingTrip = createTripRequest(passengerId, desiredDate, 36000L);
        tripRequestRepository.save(existingTrip);

        // Добавляем поездку на 3 часа (10800 сек) - сумма 13 часов > 12 часов
        val expectedDuration = 10800000L; // 3 часа в мс
        val request = new DurationCheckRequestDto(passengerId, desiredDate, expectedDuration, timeZone);

        assertThatThrownBy(() -> tripRequestService.checkDurationLimit(request))
                .isInstanceOf(DurationLimitExceededException.class)
                .hasMessage(DURATION_LIMIT);
    }

    @Test
    @DisplayName("Проверка длительности - граница лимита")
    void testCheckDurationLimitAtBoundary() {
        val passengerId = UUID.randomUUID();
        val desiredDate = OffsetDateTime.now();
        val timeZone = "+03:00";

        // Создаем существующую поездку на 11 часов 59 минут
        val existingTrip = createTripRequest(passengerId, desiredDate, 43140L); // 11ч 59мин = 43140 сек
        tripRequestRepository.save(existingTrip);

        // Добавляем поездку на 1 минуту (60 сек) - сумма 12 часов ровно
        val expectedDuration = 60000L; // 1 минута в мс
        val request = new DurationCheckRequestDto(passengerId, desiredDate, expectedDuration, timeZone);

        assertDoesNotThrow(() -> tripRequestService.checkDurationLimit(request));
    }

    @Test
    @DisplayName("Проверка длительности - чуть превышает лимит")
    void testCheckDurationLimitJustOverLimit() {
        val passengerId = UUID.randomUUID();
        val desiredDate = OffsetDateTime.now();
        val timeZone = "+03:00";

        // Создаем существующую поездку на 11 часов 59 минут
        val existingTrip = createTripRequest(passengerId, desiredDate, 43140L);
        tripRequestRepository.save(existingTrip);

        // Добавляем поездку на 2 минуты - сумма превышает 12 часов
        val expectedDuration = 120000L; // 2 минуты в мс
        val request = new DurationCheckRequestDto(passengerId, desiredDate, expectedDuration, timeZone);

        assertThatThrownBy(() -> tripRequestService.checkDurationLimit(request))
                .isInstanceOf(DurationLimitExceededException.class)
                .hasMessage(DURATION_LIMIT);
    }

    @Test
    @DisplayName("Проверка длительности - поездка после 22:00 при сбросе лимита")
    void testCheckDurationLimitAfterDayStart() {
        val passengerId = UUID.randomUUID();
        val timeZone = "+03:00";

        // 21 мая 20:00 - уже израсходовано 10 часов
        val existingTrip = createTripRequest(passengerId, OffsetDateTime.of(2026, 5, 21, 20, 0, 0, 0, ZoneOffset.of("+03:00")), 36000L);
        tripRequestRepository.save(existingTrip);

        // 22 мая 01:00 - поездка после 22:00, лимит сбросился
        // Проверяем поездку на 3 часа (10800 сек)
        // День начался в 22:00 21 мая, так что 01:00 22 мая - это уже новый день
        val request = new DurationCheckRequestDto(
                passengerId,
                OffsetDateTime.of(2026, 5, 22, 1, 0, 0, 0, ZoneOffset.of("+03:00")),
                10800000L, // 3 часа в мс
                timeZone
        );

        assertDoesNotThrow(() -> tripRequestService.checkDurationLimit(request));
    }

    @Test
    @DisplayName("Проверка длительности - поездка 21:00 с учетом остатка до 22:00")
    void testCheckDurationLimitAtDayBoundary() {
        val passengerId = UUID.randomUUID();
        val timeZone = "+03:00";

        // 21 мая 20:00 - уже израсходовано 10 часов
        val existingTrip = createTripRequest(passengerId, OffsetDateTime.of(2026, 5, 21, 20, 0, 0, 0, ZoneOffset.of("+03:00")), 36000L);
        tripRequestRepository.save(existingTrip);

        // 21 мая 21:00 - осталось 2 часа до 22:00, поездка на 2 часа 10 минут (7800 сек)
        val request = new DurationCheckRequestDto(
                passengerId,
                OffsetDateTime.of(2026, 5, 21, 21, 0, 0, 0, ZoneOffset.of("+03:00")),
                7800000L, // 2 часа 10 минут в мс
                timeZone
        );

        // Сумма: 10ч + 2ч 10мин = 12ч 10мин > 12ч - должно превысить
        assertThatThrownBy(() -> tripRequestService.checkDurationLimit(request))
                .isInstanceOf(DurationLimitExceededException.class)
                .hasMessage(DURATION_LIMIT);
    }

    @Test
    @DisplayName("Проверка длительности - поездка в таймзоне отличной от МСК (happy path)")
    void testCheckDurationLimitWithDifferentTimeZoneHappyPath() {
        val passengerId = UUID.randomUUID();
        val userTimeZone = "-04:00";

        // Создаем поездку на 7 часов (по МСК 14:00)
        val existingTrip = createTripRequestWithTimeZone(
                passengerId,
                OffsetDateTime.of(2026, 5, 21, 10, 0, 0, 0, ZoneOffset.of("-04:00")),
                25200L, // 7 часов
                userTimeZone
        );
        tripRequestRepository.save(existingTrip);

        // Пользователь хочет поехать в 16:00 по его времени (по МСК 20:00)
        // Планирует поездку на 6 часов
        val request = new DurationCheckRequestDto(
                passengerId,
                OffsetDateTime.of(2026, 5, 21, 16, 0, 0, 0, ZoneOffset.of("-04:00")),
                21600000L, // 6 часов в мс
                userTimeZone
        );

        assertDoesNotThrow(() -> tripRequestService.checkDurationLimit(request));
    }

    @Test
    @DisplayName("Проверка длительности - поездка в таймзоне отличной от МСК (ровно лимит)")
    void testCheckDurationLimitWithDifferentTimeZoneAtLimit() {
        val passengerId = UUID.randomUUID();
        val userTimeZone = "-04:00";

        val existingTrip = createTripRequestWithTimeZone(
                passengerId,
                OffsetDateTime.of(2026, 5, 21, 10, 0, 0, 0, ZoneOffset.of("-04:00")),
                36000L, // 10 часов
                userTimeZone
        );
        tripRequestRepository.save(existingTrip);

        val request = new DurationCheckRequestDto(
                passengerId,
                OffsetDateTime.of(2026, 5, 21, 16, 0, 0, 0, ZoneOffset.of("-04:00")),
                21600000L, // 6 часов в мс
                userTimeZone
        );

        assertDoesNotThrow(() -> tripRequestService.checkDurationLimit(request));
    }

    @Test
    @DisplayName("Проверка суммарного километража - лимит не превыщен")
    void testCheckOverrunLimitWithinLimit() {
        val passengerId = UUID.randomUUID();
        val desiredDate = OffsetDateTime.now();
        val expectedDistance = 3000000;
        val timeZone = "+03:00";
        val request = new OverrunCheckRequestDto(passengerId, desiredDate, expectedDistance, timeZone);

        OverrunCheckResponseDto response = tripRequestService.checkOverrunLimit(request);
        
        assertThat(response.comment()).isNull();
        assertThat(response.totalDistance()).isLessThan(5000000);
    }

    @Test
    @DisplayName("Проверка суммарного километража - лимит превышен")
    void testCheckOverrunLimitLimitExceeded() {
        val passengerId = UUID.randomUUID();
        val desiredDate = OffsetDateTime.now();
        val expectedDistance = 100000;
        val timeZone = "+03:00";
        
        val existingTrip = createTripRequest(passengerId, desiredDate, 18000000L, 5200000);
        tripRequestRepository.save(existingTrip);
        
        val request = new OverrunCheckRequestDto(passengerId, desiredDate, expectedDistance, timeZone);

        OverrunCheckResponseDto response = tripRequestService.checkOverrunLimit(request);

        assertThat(response.comment()).isNotBlank();
        assertThat(response.totalDistance()).isGreaterThanOrEqualTo(5000000);
    }

    @Test
    @DisplayName("Проверка суммарного километража - добавление поездки в пределах лимита")
    void testCheckOverrunLimitWithExistingRequest() {
        val passengerId = UUID.randomUUID();
        val desiredDate = OffsetDateTime.now();
        val timeZone = "+03:00";

        val existingTrip = createTripRequest(passengerId, desiredDate, 18000000L, 4000000);
        tripRequestRepository.save(existingTrip);

        val expectedDistance = 100000;
        val request = new OverrunCheckRequestDto(passengerId, desiredDate, expectedDistance, timeZone);

        OverrunCheckResponseDto response = tripRequestService.checkOverrunLimit(request);

        assertThat(response.comment()).isNull();
        assertThat(response.totalDistance()).isLessThan(5000000);
    }

    @Test
    @DisplayName("Проверка суммарного километража - сумма превышает лимит")
    void testCheckOverrunLimitWithExistingRequestExceeded() {
        val passengerId = UUID.randomUUID();
        val timeZone = "+03:00";
        val desiredDate = OffsetDateTime.now(ZoneOffset.of(timeZone))
                .truncatedTo(ChronoUnit.MINUTES);

        val existingTrip = createTripRequest(passengerId, desiredDate, 18000000L, 4500000);
        tripRequestRepository.save(existingTrip);

        val request = new OverrunCheckRequestDto(passengerId, desiredDate, 600000, timeZone);

        OverrunCheckResponseDto response = tripRequestService.checkOverrunLimit(request);

        assertThat(response.comment()).isNotBlank();
        assertThat(response.totalDistance()).isGreaterThanOrEqualTo(5000000);
    }


    @Test
    @DisplayName("Проверка суммарного километража - граница лимита")
    void testCheckOverrunLimitAtBoundary() {
        val passengerId = UUID.randomUUID();
        val desiredDate = OffsetDateTime.now();
        val timeZone = "+03:00";

        val existingTrip = createTripRequest(passengerId, desiredDate, 18000000L, 4900000);
        tripRequestRepository.save(existingTrip);

        val expectedDistance = 100000;
        val request = new OverrunCheckRequestDto(passengerId, desiredDate, expectedDistance, timeZone);

        OverrunCheckResponseDto response = tripRequestService.checkOverrunLimit(request);

        assertThat(response.comment()).isNull();
        assertThat(response.totalDistance()).isLessThanOrEqualTo(5000000);
    }

    @Test
    @DisplayName("Проверка суммарного километража - превышает лимит")
    void testCheckOverrunLimitJustOverLimit() {
        val passengerId = UUID.randomUUID();
        ZoneOffset zoneOffset = ZoneOffset.of("+03:00");

        val desiredDate = OffsetDateTime.now(zoneOffset).truncatedTo(ChronoUnit.MINUTES);
        val timeZone = "+03:00";

        val existingTrip = createTripRequest(passengerId, desiredDate, 18000000L, 4900000);
        tripRequestRepository.save(existingTrip);

        val request = new OverrunCheckRequestDto(passengerId, desiredDate, 110000, timeZone);

        OverrunCheckResponseDto response = tripRequestService.checkOverrunLimit(request);

        assertThat(response.comment()).isNotBlank();
        assertThat(response.totalDistance()).isGreaterThanOrEqualTo(5000000);
    }


    @Test
    @DisplayName("Проверка суммарного километража - поездка с APPROVED статусом учитывается")
    void testCheckOverrunLimitIncludesApprovedStatus() {
        val passengerId = UUID.randomUUID();
        val desiredDate = OffsetDateTime.of(2026, 5, 21, 10, 0, 0, 0, ZoneOffset.of("+03:00"));
        val timeZone = "+03:00";

        val approvedTrip = createTripRequest(passengerId, desiredDate, 18000000L, 3000000, "APPROVED");
        tripRequestRepository.save(approvedTrip);
        val cancelledTrip = createTripRequest(passengerId, desiredDate, 18000000L, 3000000, "CANCELLED");
        tripRequestRepository.save(cancelledTrip);
        val expectedDistance = 100000;
        val request = new OverrunCheckRequestDto(passengerId, desiredDate, expectedDistance, timeZone);
        OverrunCheckResponseDto response = tripRequestService.checkOverrunLimit(request);

        assertThat(response.comment()).isNull();
        assertThat(response.totalDistance()).isEqualTo(3100000);
    }

    @ParameterizedTest
    @MethodSource("excludedStatuses")
    @DisplayName("Проверка суммарного километража - '{0}' не учитывается")
    void testCheckOverrunLimitExcludesStatus(String status) {
        val passengerId = UUID.randomUUID();
        val desiredDate = OffsetDateTime.of(2026, 5, 21, 10, 0, 0, 0, ZoneOffset.of("+03:00"));
        val timeZone = "+03:00";

        val trip = createTripRequest(passengerId, desiredDate, 18000000L, 4500000, status);
        tripRequestRepository.save(trip);
        val expectedDistance = 100000;
        val request = new OverrunCheckRequestDto(passengerId, desiredDate, expectedDistance, timeZone);
        OverrunCheckResponseDto response = tripRequestService.checkOverrunLimit(request);

        assertThat(response.comment()).isNull();
        assertThat(response.totalDistance()).isEqualTo(expectedDistance);
    }

    private static Stream<String> excludedStatuses() {
        return ExcludedRequestStatuses.EXCLUDED_STATUSES.stream();
    }

    private TripRequestEntity createTripRequest(UUID passengerId, OffsetDateTime date, Long duration) {
        return createTripRequest(passengerId, date, duration, 15500);
    }

    private TripRequestEntity createTripRequest(UUID passengerId, OffsetDateTime date, Long duration, int distance) {
        return createTripRequest(passengerId, date, duration, distance, "APPROVED");
    }

    private TripRequestEntity createTripRequest(UUID passengerId, OffsetDateTime date, Long duration, int distance, String status) {
        return TripRequestEntity.builder()
                .id(UUID.randomUUID())
                .humanReadableId("YA-" + System.currentTimeMillis())
                .transportType("YANDEX")
                .passengerId(passengerId)
                .desiredDate(date)
                .timeZone("+03:00")
                .distance(distance)
                .duration(duration)
                .status(status)
                .build();
    }

    private TripRequestEntity createTripRequestWithTimeZone(UUID passengerId, OffsetDateTime date, Long duration, String timeZone) {
        return TripRequestEntity.builder()
                .id(UUID.randomUUID())
                .humanReadableId("YA-" + System.currentTimeMillis())
                .transportType("YANDEX")
                .passengerId(passengerId)
                .desiredDate(date)
                .timeZone(timeZone)
                .distance(15500)
                .duration(duration)
                .status("APPROVED")
                .build();
    }
}
