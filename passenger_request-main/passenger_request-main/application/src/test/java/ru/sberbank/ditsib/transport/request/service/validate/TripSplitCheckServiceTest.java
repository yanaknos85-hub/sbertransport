package ru.sberbank.ditsib.transport.request.service.validate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.config.personal.TransportRequestSplitCheckProperties;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForPersonalRepository;
import ru.sberbank.ditsib.transport.request.database.dao.projection.RequestForPersonalSplitCheckProjection;
import ru.sberbank.ditsib.transport.request.dto.validation.TripSplitCheckDTO;
import ru.sberbank.ditsib.transport.request.service.validate.impl.TripSplitCheckServiceImpl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

class TripSplitCheckServiceTest {

    @Mock
    private RequestForPersonalRepository requestRepository;

    @Mock
    private TransportRequestSplitCheckProperties properties;

    @InjectMocks
    private TripSplitCheckServiceImpl service;

    private final UUID EMPLOYEE_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        TransportRequestSplitCheckProperties.Personal splitCheck = new TransportRequestSplitCheckProperties.Personal();
        splitCheck.setCostThreshold(10000.00); // 100 руб
        splitCheck.setDurationThreshold(60);  // 60 минут
        splitCheck.setRequestCountThreshold(2);
        splitCheck.setIgnoreStatuses(List.of(TripRequestStatus.PERSONAL_CANCELLED, TripRequestStatus.PERSONAL_AWAITING_TRIP_DECLINED));

        when(properties.getPersonal()).thenReturn(splitCheck);
    }

    @Test
    @DisplayName("Должен пропустить проверку, если стоимость заявки больше порога")
    void shouldSkipCheck_whenExpectedCostGreaterThanThreshold() {
        final var rq = new TripSplitCheckDTO(
                System.currentTimeMillis(), // desiredDate
                "GMT+3",
                EMPLOYEE_ID,
                15000L, // > 10000 → пропускаем
                3600000L
        );

        Assertions.assertDoesNotThrow(() -> service.check(rq));

        verify(requestRepository, never()).findSplitCheckProjectionsByPassengerIdAndStatusNotInAndDesiredDateBetween(
                any(), any(), any(), anyList(), any());
    }

    @Test
    @DisplayName("Должен пропустить проверку, если нет других заявок на этот день")
    void shouldAllowRequest_whenNoOtherRequestsExist() {
        final var rq = new TripSplitCheckDTO(
                toEpochMillis("2025-10-21T11:31:43"),
                "GMT+3",
                EMPLOYEE_ID,
                10000L, // = threshold
                1200000L  // 20 мин
        );

        when(requestRepository.findSplitCheckProjectionsByPassengerIdAndStatusNotInAndDesiredDateBetween(
                eq(EMPLOYEE_ID),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyList(),
                eq(10000.0)
        )).thenReturn(List.of());

        final var result = service.check(rq);

        Assertions.assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Должен разрешить, если новая поездка начинается через 61 минуту после предыдущей")
    void shouldAllow_whenIntervalAfterIs61Minutes() {

        final var existingRequest_1 = new RequestForPersonalSplitCheckProjection(
                UUID.randomUUID(),
                "OT-0001-00014879",
                LocalDateTime.of(2025, 10, 21, 9, 30).atZone(ZoneOffset.UTC).toLocalDateTime(),
                Duration.ofHours(1),
                TripRequestStatus.PERSONAL_AWAITING_APPROVAL,
                "GMT+3"

        );

        final var existingRequest_2 = new RequestForPersonalSplitCheckProjection(
                UUID.randomUUID(),
                "OT-0001-00014878",
                LocalDateTime.of(2025, 10, 21, 6, 30).atZone(ZoneOffset.UTC).toLocalDateTime(),
                Duration.ofHours(1),
                TripRequestStatus.PERSONAL_AWAITING_APPROVAL,
                "GMT+3"

        );

        when(requestRepository.findSplitCheckProjectionsByPassengerIdAndStatusNotInAndDesiredDateBetween(
                any(), any(),  any(), anyList(), any()))
                .thenReturn(List.of(existingRequest_1, existingRequest_2));

        final var rq = new TripSplitCheckDTO(
                1762869043879L,
                "GMT+3",
                EMPLOYEE_ID,
                10000L,
                146000L
        );

        final var result = service.check(rq);

        Assertions.assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Должен запретить, если новая поездка начинается через 59 минут после предыдущей")
    void shouldThrow_whenIntervalAfterIs59Minutes() {
        final var existingRequest_1 = new RequestForPersonalSplitCheckProjection(
                UUID.randomUUID(),
                "OT-0001-00014879",
                LocalDateTime.of(2025, 10, 21, 9, 30).atZone(ZoneOffset.UTC).toLocalDateTime(),
                Duration.ofHours(1),
                TripRequestStatus.PERSONAL_AWAITING_APPROVAL,
                "GMT+3"
        );

        final var existingRequest_2 = new RequestForPersonalSplitCheckProjection(
                UUID.randomUUID(),
                "OT-0001-00014878",
                LocalDateTime.of(2025, 10, 21, 10, 30).atZone(ZoneOffset.UTC).toLocalDateTime(),
                Duration.ofHours(1),
                TripRequestStatus.PERSONAL_AWAITING_APPROVAL,
                "GMT+3"
        );

        when(requestRepository.findSplitCheckProjectionsByPassengerIdAndStatusNotInAndDesiredDateBetween(
                any(), any(), any(), anyList(), any()))
                .thenReturn(List.of(existingRequest_1, existingRequest_2));

        final var rq = new TripSplitCheckDTO(
                LocalDateTime.of(2025, 10, 21, 11, 29).atZone(ZoneOffset.UTC).toEpochSecond() * 1000,
                "GMT+3",
                EMPLOYEE_ID,
                10000L,
                1200000L
        );
        final var result = service.check(rq);

        Assertions.assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Должен запретить, если новая поездка заканчивается за 50 минут до следующей")
    void shouldThrow_whenIntervalBeforeIs50Minutes() {
        final var existingRequest_1 = new RequestForPersonalSplitCheckProjection(
                UUID.randomUUID(),
                "OT-0001-00014878",
                LocalDateTime.of(2025, 10, 21, 15, 20).atZone(ZoneOffset.UTC).toLocalDateTime(),
                Duration.ofHours(2),
                TripRequestStatus.PERSONAL_AWAITING_APPROVAL,
                "GMT+3"
        );

        final var existingRequest_2 = new RequestForPersonalSplitCheckProjection(
                UUID.randomUUID(),
                "OT-0001-00014879",
                LocalDateTime.of(2025, 10, 21, 10, 20).atZone(ZoneOffset.UTC).toLocalDateTime(),
                Duration.ofHours(2),
                TripRequestStatus.PERSONAL_AWAITING_APPROVAL,
                "GMT+3"
        );


        when(requestRepository.findSplitCheckProjectionsByPassengerIdAndStatusNotInAndDesiredDateBetween(
                any(), any(), any(), anyList(), any()))
                .thenReturn(List.of(existingRequest_1, existingRequest_2));

        final var rq = new TripSplitCheckDTO(
                LocalDateTime.of(2025, 10, 21, 14, 10).atZone(ZoneOffset.UTC).toEpochSecond() * 1000,
                "GMT+3",
                EMPLOYEE_ID,
                10000L,
                Duration.ofMinutes(20).toMillis()
        );

        final var result = service.check(rq);

        Assertions.assertFalse(result.isValid());
    }

    private long toEpochMillis(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr)
                .atZone(ZoneId.of("UTC"))
                .toInstant()
                .toEpochMilli();
    }
}
