package ru.sber.transport.request.external.business.impl;

import io.qameta.allure.Feature;
import org.apache.logging.log4j.util.TriConsumer;
import org.instancio.Instancio;
import org.instancio.Select;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.business.providers.AssessmnentProvider;
import ru.sber.transport.business.providers.AvailableClasses;
import ru.sber.transport.business.providers.DelegatesProvider;
import ru.sber.transport.business.providers.DepartmentsProvider;
import ru.sber.transport.business.providers.DurationRequestCheckProvider;
import ru.sber.transport.business.providers.EmployeesProvider;
import ru.sber.transport.business.providers.FilesProvider;
import ru.sber.transport.business.providers.FraudsProvider;
import ru.sber.transport.business.providers.GeoZonesProvider;
import ru.sber.transport.business.providers.LimitsProvider;
import ru.sber.transport.business.providers.OverrunCheckProvider;
import ru.sber.transport.business.providers.PricesProvider;
import ru.sber.transport.business.providers.TripOrderHistoriesProvider;
import ru.sber.transport.business.providers.TripOrdersProvider;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.payout.messaging.message.RequestPayoutMessage;
import ru.sber.transport.request.external.business.TripOrdersService;
import ru.sber.transport.request.external.business.exception.BusinessException;
import ru.sber.transport.request.external.business.exception.CreatingException;
import ru.sber.transport.request.external.business.exception.DataConflictException;
import ru.sber.transport.request.external.business.exception.StatusSwitchForbiddenException;
import ru.sber.transport.request.external.messaging.mapper.ReceiptMapper;
import ru.sber.transport.request.external.messaging.mapper.RequestMapper;
import ru.sber.transport.request.external.messaging.message.FraudMonitoringMessage;
import ru.sber.transport.request.external.messaging.senders.FraudMonitoringSender;
import ru.sber.transport.request.external.messaging.senders.NotificationSender;
import ru.sber.transport.request.external.messaging.senders.ReceiptSender;
import ru.sber.transport.request.external.messaging.senders.RequestPayoutSender;
import ru.sber.transport.request.external.messaging.senders.RequestSender;
import ru.sber.transport.request.external.model.*;
import ru.sber.transport.request.external.model.geozone.GeoZoneDTO;
import ru.sber.transport.request.external.model.overrun.OverrunCheckResult;
import ru.sber.transport.request.external.model.triporder.TripOrderCreateDTO;
import ru.sber.transport.request.external.model.waypoint.WaypointDTO;
import ru.sber.transport.request.external.providers.exceptions.DatabaseLayerException;
import ru.sber.transport.request.external.providers.exceptions.DurationLimitExceededException;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.sber.transport.request.external.model.State.CANCELLED;
import static ru.sber.transport.request.external.model.State.CONFIRMATION;
import static ru.sber.transport.request.external.model.State.CONFIRMATION_NEEDED;
import static ru.sber.transport.request.external.model.State.CONFIRMED;
import static ru.sber.transport.request.external.model.State.DATA_NEEDED;
import static ru.sber.transport.request.external.model.State.DECLINED;
import static ru.sber.transport.request.external.model.State.NEW;
import static ru.sber.transport.request.external.model.State.ORDER_PAYMENT_FORMATION;
import static ru.sber.transport.request.external.model.State.PAYMENT_AWAITING;
import static ru.sber.transport.request.external.model.State.PAYMENT_DONE;
import static ru.sber.transport.request.external.model.State.PAYMENT_NOT_DONE;
import static ru.sber.transport.request.external.model.Tariff.ECONOMY;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка бизнес-логики заявки на поездку")
class TripOrdersServiceProviderImplTest {

    private final EmployeesProvider employeesProvider = mock(EmployeesProvider.class);

    private final TripOrdersProvider tripOrdersProvider = mock(TripOrdersProvider.class);

    private final AssessmnentProvider assessmnentProvider = mock(AssessmnentProvider.class);

    private final PricesProvider pricesProvider = mock(PricesProvider.class);

    private final RequestSender sender = mock(RequestSender.class);

    private final Clock clock = Clock.fixed(OffsetDateTime.parse("2022-01-01T00:00:00+03:00").toInstant(),
        ZoneOffset.UTC);

    private final NotificationSender notificationSender = mock(NotificationSender.class);

    private final FilesProvider filesProvider = mock(FilesProvider.class);

    private final AvailableClasses availableClasses = mock(AvailableClasses.class);

    private final DepartmentsProvider departmentsProvider = mock(DepartmentsProvider.class);

    private final LimitsProvider limitsProvider = mock(LimitsProvider.class);

    private final DelegatesProvider delegatesProvider = mock(DelegatesProvider.class);

    private final GeoZonesProvider geoZonesProvider = mock(GeoZonesProvider.class);

    private final TripOrderHistoriesProvider tripOrderHistoriesProvider = mock(TripOrderHistoriesProvider.class);

    private final RequestPayoutSender requestPayoutSender = mock(RequestPayoutSender.class);

    private final RequestMapper requestMapper = mock(RequestMapper.class);

    private final DurationRequestCheckProvider durationRequestCheckProvider = mock(DurationRequestCheckProvider.class);

    private final FraudMonitoringSender fraudMonitoringSender = mock(FraudMonitoringSender.class);

    private final OverrunCheckProvider overrunCheckProvider = mock(OverrunCheckProvider.class);

    private final FraudsProvider fraudsProvider = mock(FraudsProvider.class);

    private final ReceiptMapper receiptMapper = mock(ReceiptMapper.class);

    private final ReceiptSender receiptScannerSender = mock(ReceiptSender.class);

    private final ObjectProvider<LimitsProvider> objectProvider = new ObjectProvider<>() {
        @NotNull
        @Override
        public LimitsProvider getObject() throws BeansException {
            return limitsProvider;
        }

        @Override
        public LimitsProvider getIfAvailable() throws BeansException {
            return getObject();
        }
    };

    private final TripOrdersService tripOrdersService = new TripOrdersServiceImpl(
        employeesProvider, tripOrdersProvider, assessmnentProvider, pricesProvider, sender, List.of(notificationSender),
        clock,
        filesProvider,
        availableClasses, objectProvider, departmentsProvider, delegatesProvider, geoZonesProvider,
        tripOrderHistoriesProvider, requestPayoutSender,
        requestMapper, durationRequestCheckProvider, fraudMonitoringSender, overrunCheckProvider, fraudsProvider,
        28_800_000L, receiptMapper, receiptScannerSender
    );

    public static Stream<Arguments> editForbiddenStatusSource() {
        return Stream.of(
            Arguments.of(NEW, CONFIRMED),
            Arguments.of(NEW, DECLINED),
            Arguments.of(NEW, DATA_NEEDED),

            Arguments.of(CONFIRMATION_NEEDED, NEW),
            Arguments.of(CONFIRMATION_NEEDED, CONFIRMED),
            Arguments.of(CONFIRMATION_NEEDED, DECLINED),
            Arguments.of(CONFIRMATION_NEEDED, DATA_NEEDED),

            Arguments.of(CONFIRMATION, CONFIRMATION_NEEDED),
            Arguments.of(CONFIRMATION, NEW),

            Arguments.of(CONFIRMED, NEW),
            Arguments.of(CONFIRMED, CONFIRMATION_NEEDED),
            Arguments.of(CONFIRMED, CONFIRMATION),
            Arguments.of(CONFIRMED, DECLINED),
            Arguments.of(CONFIRMED, CANCELLED),

            Arguments.of(DECLINED, NEW),
            Arguments.of(DECLINED, CONFIRMATION_NEEDED),
            Arguments.of(DECLINED, CONFIRMATION),
            Arguments.of(DECLINED, CONFIRMED),
            Arguments.of(DECLINED, CANCELLED),

            Arguments.of(DATA_NEEDED, NEW),
            Arguments.of(DATA_NEEDED, CONFIRMATION_NEEDED),
            Arguments.of(DATA_NEEDED, CONFIRMED),
            Arguments.of(DATA_NEEDED, DECLINED),

            Arguments.of(CANCELLED, NEW),
            Arguments.of(CANCELLED, CONFIRMATION),
            Arguments.of(CANCELLED, CONFIRMED),
            Arguments.of(CANCELLED, DECLINED),
            Arguments.of(CANCELLED, CONFIRMATION_NEEDED),
            Arguments.of(CANCELLED, DATA_NEEDED),

            Arguments.of(ORDER_PAYMENT_FORMATION, CANCELLED),
            Arguments.of(PAYMENT_AWAITING, CANCELLED),
            Arguments.of(PAYMENT_DONE, CANCELLED),
            Arguments.of(PAYMENT_NOT_DONE, CANCELLED)
        );
    }

    public static Stream<Arguments> editAllowedStatusSource() {
        return Stream.of(
            Arguments.of(NEW, CONFIRMATION_NEEDED,
                (BiConsumer<TripOrderData, Object>) TripOrdersServiceProviderImplTest::assertPassengerConfigrmationSent,
                null, "+sum",
                (TriConsumer<LimitsProvider, TripOrderData, TripOrderData>) (l, o, n) -> verify(l).reserve(eq(o),
                    any())),
            Arguments.of(NEW, CANCELLED, null, null, "+sum",
                (TriConsumer<LimitsProvider, TripOrderData, TripOrderData>) (l, o, n) -> verify(l).cancel(o.getId())),
            Arguments.of(NEW, CONFIRMATION, null, "EXTERNAL_REQUEST_APPROVAL_NEEDED", "=sum",
                (TriConsumer<LimitsProvider, TripOrderData, TripOrderData>) (l, o, n) -> verify(l).reserve(eq(o),
                    any())),

            Arguments.of(CONFIRMATION_NEEDED, CONFIRMATION, null, "EXTERNAL_REQUEST_APPROVAL_NEEDED", null, null),
            Arguments.of(CONFIRMATION_NEEDED, CANCELLED, null, null, null, null),

            Arguments.of(CONFIRMATION, CANCELLED, null, null, null, null),

            Arguments.of(DATA_NEEDED, CONFIRMATION, null, "EXTERNAL_REQUEST_APPROVAL_NEEDED", null, null),
            Arguments.of(DATA_NEEDED, CANCELLED, null, null, null, null),

            Arguments.of(CONFIRMED, ORDER_PAYMENT_FORMATION, null, null, null, null),
            Arguments.of(ORDER_PAYMENT_FORMATION, PAYMENT_AWAITING, null, null, null, null)
        );
    }

    private static void assertPassengerConfigrmationSent(TripOrderData source, Object sender) {
        verify((NotificationSender) sender).send(source, "EXTERNAL_REQUEST_CONFIRMATION_NEEDED",
            source.getPassenger().getId());
    }

    @Test
    @DisplayName("Проверка создания заявки на поездку")
    void test_create() {
        final var waypoints = List.of(Instancio.create(WaypointDTO.class));
        final var tripOrderRq = Instancio.of(TripOrderCreateDTO.class)
            .set(Select.field(TripOrderCreateDTO::waypoints), waypoints)
            .set(Select.field(TripOrderCreateDTO::tariff), ECONOMY)
            .create();
        final var price = Instancio.create(TestPriceData.class);
        final var timeZone = "+03:00";
        final var geoZone = Instancio.of(GeoZoneDTO.class)
            .set(Select.field(GeoZoneDTO::timeZone), timeZone)
            .create();
        final var testEmployee = Instancio.create(TestEmployee.class);
        final var userId = UUID.randomUUID();
        final var expected = Instancio.create(TestTripOrder.class);

        when(employeesProvider.get(userId)).thenReturn(testEmployee);
        when(availableClasses.exists(
            testEmployee.getOrganizationId(),
            testEmployee.getPositionId(),
            testEmployee.getDepartmentId()))
            .thenReturn(true);
        when(pricesProvider.get(tripOrderRq.waypoints(), tripOrderRq.tariff())).thenReturn(price);
        when(geoZonesProvider.get(any())).thenReturn(geoZone);
        when(tripOrdersProvider.create(any(), any(), any(), any())).thenReturn(expected);
        when(overrunCheckProvider.checkOverrunLimit(any(), any(), anyInt(), any())).thenReturn(
            new OverrunCheckResult(null, 0));

        final var actual = tripOrdersService.create(userId, tripOrderRq);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected, actual);

        final var inOrder = inOrder(employeesProvider, availableClasses, geoZonesProvider, pricesProvider,
            tripOrdersProvider, durationRequestCheckProvider, overrunCheckProvider);
        inOrder.verify(employeesProvider).get(userId);
        inOrder.verify(availableClasses).exists(
            testEmployee.getOrganizationId(),
            testEmployee.getPositionId(),
            testEmployee.getDepartmentId());
        inOrder.verify(geoZonesProvider, times(2)).get(any());
        inOrder.verify(pricesProvider).get(tripOrderRq.waypoints(), tripOrderRq.tariff());
        inOrder.verify(tripOrdersProvider).create(any(), any(), any(), any());
        inOrder.verify(durationRequestCheckProvider).checkDurationLimit(
            eq(userId),
            eq(tripOrderRq.date()),
            eq(0L),
            eq(timeZone));
        inOrder.verify(overrunCheckProvider).checkOverrunLimit(
            eq(userId),
            eq(tripOrderRq.date()),
            eq((int) price.distance()),
            eq(timeZone));
    }

    @Test
    @DisplayName("Проверка создания заявки на поездку. Запрещено")
    void test_create_forbidden() {
        final var waypoints = List.of(Instancio.create(WaypointDTO.class));
        final var tripOrderRq = Instancio.of(TripOrderCreateDTO.class)
            .set(Select.field(TripOrderCreateDTO::waypoints), waypoints)
            .create();
        final var price = Instancio.create(TestPriceData.class);
        final var timeZone = "+03:00";
        final var geoZone = Instancio.of(GeoZoneDTO.class)
            .set(Select.field(GeoZoneDTO::timeZone), timeZone)
            .create();
        final var testEmployee = Instancio.create(TestEmployee.class);
        final var userId = UUID.randomUUID();

        when(employeesProvider.get(userId)).thenReturn(testEmployee);

        when(availableClasses.exists(
            testEmployee.getOrganizationId(),
            testEmployee.getPositionId(),
            testEmployee.getDepartmentId()))
            .thenReturn(false);

        when(pricesProvider.get(tripOrderRq.waypoints(), tripOrderRq.tariff())).thenReturn(price);

        when(geoZonesProvider.get(any())).thenReturn(geoZone);

        assertThrows(CreatingException.class, () -> {
            tripOrdersService.create(userId, tripOrderRq);
        });

        verify(durationRequestCheckProvider, never()).checkDurationLimit(any(), any(), any(), any());
        verify(tripOrdersProvider, never()).create(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Проверка создания заявки на поездку. Запрещено — не удалось получить timezone")
    void test_create_forbidden_withoutGeoZone() {
        final var waypoints = List.of(Instancio.create(WaypointDTO.class));
        final var tripOrderRq = Instancio.of(TripOrderCreateDTO.class)
            .set(Select.field(TripOrderCreateDTO::waypoints), waypoints)
            .create();
        final var price = Instancio.create(TestPriceData.class);
        final var testEmployee = Instancio.create(TestEmployee.class);
        final var userId = UUID.randomUUID();

        when(employeesProvider.get(userId)).thenReturn(testEmployee);
        when(availableClasses.exists(
            testEmployee.getOrganizationId(),
            testEmployee.getPositionId(),
            testEmployee.getDepartmentId()))
            .thenReturn(true);
        when(pricesProvider.get(tripOrderRq.waypoints(), tripOrderRq.tariff())).thenReturn(price);
        when(geoZonesProvider.get(any())).thenThrow(new RuntimeException("Failed to retrieve timezone"));

        assertThrows(RuntimeException.class, () -> {
            tripOrdersService.create(userId, tripOrderRq);
        });

        verify(tripOrdersProvider, never()).create(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Проверка создания заявки на поездку. Ошибка проверки duration")
    void test_create_forbidden_durationError() {
        final var waypoints = List.of(Instancio.create(WaypointDTO.class));
        final var tripOrderRq = Instancio.of(TripOrderCreateDTO.class)
            .set(Select.field(TripOrderCreateDTO::waypoints), waypoints)
            .create();
        final var price = Instancio.of(TestPriceData.class)
            .set(Select.field(TestPriceData::duration), Duration.ZERO)
            .create();
        final var timeZone = "+03:00";
        final var geoZone = Instancio.of(GeoZoneDTO.class)
            .set(Select.field(GeoZoneDTO::timeZone), timeZone)
            .create();
        final var testEmployee = Instancio.create(TestEmployee.class);
        final var userId = UUID.randomUUID();
        final var expected = Instancio.create(TestTripOrder.class);

        when(employeesProvider.get(userId)).thenReturn(testEmployee);
        when(availableClasses.exists(
            testEmployee.getOrganizationId(),
            testEmployee.getPositionId(),
            testEmployee.getDepartmentId()))
            .thenReturn(true);
        when(pricesProvider.get(tripOrderRq.waypoints(), tripOrderRq.tariff())).thenReturn(price);
        when(geoZonesProvider.get(any())).thenReturn(geoZone);
        when(tripOrdersProvider.create(any(), any(), any(), any())).thenReturn(expected);
        when(overrunCheckProvider.checkOverrunLimit(any(), any(), anyInt(), any())).thenReturn(
            new OverrunCheckResult(null, 0));
        doThrow(new DurationLimitExceededException())
            .when(durationRequestCheckProvider).checkDurationLimit(any(), any(), any(), any());

        final var actual = tripOrdersService.create(userId, tripOrderRq);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected, actual);

        verify(pricesProvider).get(tripOrderRq.waypoints(), tripOrderRq.tariff());
        verify(tripOrdersProvider).create(any(), any(), any(), any());
        verify(durationRequestCheckProvider).checkDurationLimit(any(), any(), any(), any());
        verify(fraudsProvider).save(any());
        verify(fraudMonitoringSender).send(any(FraudMonitoringMessage.class));
    }

    @Test
    @DisplayName("Проверка создания заявки на поездку. Проверка duration успешна")
    void test_create_durationCheckSuccess() {
        final var waypoints = List.of(Instancio.create(WaypointDTO.class));
        final var tripOrderRq = Instancio.of(TripOrderCreateDTO.class)
            .set(Select.field(TripOrderCreateDTO::waypoints), waypoints)
            .create();
        final var price = Instancio.create(TestPriceData.class);
        final var timeZone = "+03:00";
        final var geoZone = Instancio.of(GeoZoneDTO.class)
            .set(Select.field(GeoZoneDTO::timeZone), timeZone)
            .create();
        final var testEmployee = Instancio.create(TestEmployee.class);
        final var userId = UUID.randomUUID();
        final var expected = Instancio.create(TestTripOrder.class);

        when(employeesProvider.get(userId)).thenReturn(testEmployee);
        when(availableClasses.exists(
            testEmployee.getOrganizationId(),
            testEmployee.getPositionId(),
            testEmployee.getDepartmentId()))
            .thenReturn(true);
        when(pricesProvider.get(tripOrderRq.waypoints(), tripOrderRq.tariff())).thenReturn(price);
        when(geoZonesProvider.get(any())).thenReturn(geoZone);
        doNothing().when(durationRequestCheckProvider).checkDurationLimit(any(), any(), any(), any());
        when(overrunCheckProvider.checkOverrunLimit(any(), any(), anyInt(), any())).thenReturn(
            new OverrunCheckResult(null, 0));
        when(tripOrdersProvider.create(any(), any(), any(), any())).thenReturn(expected);

        final var actual = tripOrdersService.create(userId, tripOrderRq);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected, actual);

        verify(durationRequestCheckProvider).checkDurationLimit(
            eq(userId),
            eq(tripOrderRq.date()),
            eq(0L),
            eq(timeZone));
        verify(overrunCheckProvider).checkOverrunLimit(
            eq(userId),
            eq(tripOrderRq.date()),
            eq((int) price.distance()),
            eq(timeZone));
    }

    @Test
    @DisplayName("Проверка создания заявки на поездку. Превышен лимит километража")
    void test_create_overrunLimitExceeded() {
        final var waypoints = List.of(Instancio.create(WaypointDTO.class));
        final var tripOrderRq = Instancio.of(TripOrderCreateDTO.class)
            .set(Select.field(TripOrderCreateDTO::waypoints), waypoints)
            .set(Select.field(TripOrderCreateDTO::tariff), ECONOMY)
            .create();
        final var price = Instancio.of(TestPriceData.class)
            .set(Select.field(TestPriceData::duration), Duration.ZERO)
            .create();
        final var timeZone = "+03:00";
        final var geoZone = Instancio.of(GeoZoneDTO.class)
            .set(Select.field(GeoZoneDTO::timeZone), timeZone)
            .create();
        final var testEmployee = Instancio.create(TestEmployee.class);
        final var userId = UUID.randomUUID();
        final var expected = Instancio.create(TestTripOrder.class);
        final var totalDistance = 15250;
        final var overrunComment = "Лимит километража превышен";
        final var expectedComment = overrunComment + " " + 15.25 + " км";

        when(employeesProvider.get(userId)).thenReturn(testEmployee);
        when(availableClasses.exists(
            testEmployee.getOrganizationId(),
            testEmployee.getPositionId(),
            testEmployee.getDepartmentId()))
            .thenReturn(true);
        when(pricesProvider.get(tripOrderRq.waypoints(), tripOrderRq.tariff())).thenReturn(price);
        when(geoZonesProvider.get(any())).thenReturn(geoZone);
        when(tripOrdersProvider.create(any(), any(), any(), any())).thenReturn(expected);
        when(overrunCheckProvider.checkOverrunLimit(any(), any(), anyInt(), any()))
            .thenReturn(new OverrunCheckResult(overrunComment, totalDistance));

        tripOrdersService.create(userId, tripOrderRq);

        final var fraudCaptor = ArgumentCaptor.forClass(Fraud.class);
        verify(fraudsProvider).save(fraudCaptor.capture());

        final var fraud = fraudCaptor.getValue();
        assertThat(fraud.getId()).isEqualTo(expected.getId());
        assertThat(fraud.getType()).isEqualTo("OVERRUN");
        assertThat(fraud.getComment()).isEqualTo(expectedComment);
        verify(fraudMonitoringSender).send(any(FraudMonitoringMessage.class));
    }

    @Test
    @DisplayName("Проверка создания заявки на поездку. Превышение лимита километража. Пустой комментарий не сохраняется")
    void test_create_overrunLimitExceeded_blankCommentNotSaved() {
        final var waypoints = List.of(Instancio.create(WaypointDTO.class));
        final var tripOrderRq = Instancio.of(TripOrderCreateDTO.class)
            .set(Select.field(TripOrderCreateDTO::waypoints), waypoints)
            .set(Select.field(TripOrderCreateDTO::tariff), ECONOMY)
            .create();
        final var price = Instancio.of(TestPriceData.class)
            .set(Select.field(TestPriceData::duration), Duration.ZERO)
            .create();
        final var timeZone = "+03:00";
        final var geoZone = Instancio.of(GeoZoneDTO.class)
            .set(Select.field(GeoZoneDTO::timeZone), timeZone)
            .create();
        final var testEmployee = Instancio.create(TestEmployee.class);
        final var userId = UUID.randomUUID();
        final var expected = Instancio.create(TestTripOrder.class);

        when(employeesProvider.get(userId)).thenReturn(testEmployee);
        when(availableClasses.exists(
            testEmployee.getOrganizationId(),
            testEmployee.getPositionId(),
            testEmployee.getDepartmentId()))
            .thenReturn(true);
        when(pricesProvider.get(tripOrderRq.waypoints(), tripOrderRq.tariff())).thenReturn(price);
        when(geoZonesProvider.get(any())).thenReturn(geoZone);
        when(tripOrdersProvider.create(any(), any(), any(), any())).thenReturn(expected);
        when(overrunCheckProvider.checkOverrunLimit(any(), any(), anyInt(), any()))
            .thenReturn(new OverrunCheckResult("  ", 15000));

        tripOrdersService.create(userId, tripOrderRq);

        verify(fraudsProvider, never()).save(any());
        verify(fraudMonitoringSender, never()).send(any(FraudMonitoringMessage.class));
    }

    @Test
    @DisplayName("Проверка удаления заявки на поездку")
    void test_delete() {
        final var organizationId = UUID.randomUUID();
        final var requestId = UUID.randomUUID();
        final var tripOrder = Instancio.of(TestTripOrder.class)
            .set(Select.field(TestTripOrder::getId), requestId)
            .create();

        when(tripOrdersProvider.get(organizationId, requestId)).thenReturn(Optional.of(tripOrder));

        tripOrdersService.delete(organizationId, requestId);

        verify(tripOrdersProvider).delete(organizationId, requestId);
        verify(sender).send(tripOrder);
        verify(limitsProvider).cancel(requestId);
    }

    @Test
    @DisplayName("Проверка редактирования заявки на поездку. Заявка не найдена")
    void test_edit_order_not_found() {
        final var organizationId = UUID.randomUUID();
        final var id = UUID.randomUUID();
        final var newData = Instancio.create(TestTripOrder.class);
        final var userId = UUID.randomUUID();
        final var editedFields = Set.<String>of();

        try {
            tripOrdersService.edit(userId, false, organizationId, id, newData, editedFields);
            fail("EntityNotFoundException не выброшен");
        } catch (EntityNotFoundException exception) {
            assertThat(exception)
                .hasFieldOrPropertyWithValue("entityName", TripOrderData.class.getSimpleName())
                .hasFieldOrPropertyWithValue("entityId", id);
        }
    }

    @Test
    @DisplayName("Проверка редактирования заявки на поездку. Прикрепление оценки. Уже прикреплена")
    void test_edit_order_already_assessmented() {
        final var organizationId = UUID.randomUUID();
        final var id = UUID.randomUUID();
        final var newData = Instancio.create(TestTripOrder.class);
        final var assessmented = Instancio.create(TestTripOrder.class);
        final var userId = UUID.randomUUID();
        final var editedFields = Set.of("+assessments");

        when(tripOrdersProvider.get(organizationId, id)).thenReturn(Optional.of(assessmented));

        try {
            tripOrdersService.edit(userId, false, organizationId, id, newData, editedFields);
            fail("DataConflictException не выброшен");
        } catch (DataConflictException exception) {
            assertThat(exception.getEntityClass()).isEqualTo(TripOrderData.class);
            assertThat(exception.getConflictedField()).isEqualTo("assessments");
        }
    }

    @MethodSource("editForbiddenStatusSource")
    @ParameterizedTest
    @DisplayName("Проверка редактирования заявки на поездку. Отклонение изменений")
    void test_edit_forbidden(State oldState, State newState) {
        final var organizationId = UUID.randomUUID();
        final var id = UUID.randomUUID();
        final var newData = Instancio.of(TestTripOrder.class)
            .set(Select.field(TestTripOrder::getStatus), newState)
            .create();
        final var oldData = Instancio.of(TestTripOrder.class)
            .set(Select.field(TestTripOrder::getStatus), oldState)
            .create();
        final var userId = UUID.randomUUID();
        final var editedFields = Set.of("=status");

        when(tripOrdersProvider.get(organizationId, id)).thenReturn(Optional.of(oldData));

        try {
            tripOrdersService.edit(userId, true, organizationId, id, newData, editedFields);
            fail("DataConflictException не выброшен");
        } catch (DataConflictException exception) {
            assertThat(exception.getEntityClass()).isEqualTo(TripOrderData.class);
            assertThat(exception.getConflictedField()).isEqualTo("status");
            assertThat(exception.getOldValue()).hasToString(oldState.name());
            assertThat(exception.getNewValue()).hasToString(newState.name());
        }
    }

    @MethodSource("editAllowedStatusSource")
    @ParameterizedTest
    @DisplayName("Проверка редактирования заявки на поездку.")
    void test_edit_allowed(State oldState, State newState, BiConsumer<TripOrderData, Object> additionalChecks,
        String notificationType, String sumStatus,
        TriConsumer<LimitsProvider, TripOrderData, TripOrderData> verifyLimits) {
        final var organizationId = UUID.randomUUID();
        final var id = UUID.randomUUID();
        final var userId = UUID.randomUUID();
        final var newData = Instancio.of(TestTripOrder.class)
            .set(Select.field(TestTripOrder::getStatus), newState)
            .create();
        final var oldData = Instancio.of(TestTripOrder.class)
            .set(Select.field(TestTripOrder::getStatus), oldState)
            .create();
        final var department = Instancio.create(TestDepartment.class);

        if (notificationType != null) {
            when(departmentsProvider.get(any())).thenReturn(department);
        }
        when(tripOrdersProvider.get(organizationId, id)).thenReturn(Optional.of(oldData));
        if (oldState.equals(CONFIRMED)) {
            doReturn(List.of(Instancio.of(TestTripOrderHistory.class)
                .set(Select.field(TestTripOrderHistory::getModifiedAt),
                    LocalDateTime.now(clock).atOffset(ZoneOffset.UTC))
                .set(Select.field(TestTripOrderHistory::getStatus), ORDER_PAYMENT_FORMATION)
                .create()))
                .when(tripOrderHistoriesProvider).get(any(UUID.class));
            doReturn(new RequestPayoutMessage(
                UUID.randomUUID(), "humanReadableId", "4661", "26511", UUID.randomUUID(),
                BigDecimal.ZERO, UUID.randomUUID(), LocalDateTime.now(clock).atOffset(ZoneOffset.UTC),
                "YANDEX_TAXI", UUID.randomUUID(), null, null, null, null, null, null, null, null,
                null, null, null, null
            )).when(requestMapper).toRequestPayoutMessage(any(), any(), any());
        }

        tripOrdersService.edit(userId, true, organizationId, id, newData,
            Stream.of("=status", sumStatus).filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet()));

        verify(fraudMonitoringSender, never()).send(any(FraudMonitoringMessage.class));

        if (!newState.equals(ORDER_PAYMENT_FORMATION)) {
            final var newDataCaptor = ArgumentCaptor.forClass(TestTripOrder.class);
            final var newMessageCaptor = ArgumentCaptor.forClass(TestTripOrder.class);

            verify(tripOrdersProvider).edit(eq(userId), eq(id), newDataCaptor.capture(),
                eq(Stream.of("=status", sumStatus).filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet())));
            if (notificationType != null) {
                verify(notificationSender).send(any(), eq(notificationType), any());
            }

            assertThat(newDataCaptor.getValue().getStatus().name()).isEqualTo(newState.name());

            verify(sender).send(newMessageCaptor.capture());
            if (verifyLimits != null) {
                verifyLimits.accept(limitsProvider, oldData, newData);
            }

            assertThat(newMessageCaptor.getValue().getStatus().name()).isEqualTo(oldState.name());

            if (additionalChecks != null) {
                additionalChecks.accept(oldData, notificationSender);
            }
        } else {
            var captor = ArgumentCaptor.forClass(RequestPayoutMessage.class);
            verify(requestPayoutSender).send(captor.capture());
            var actual = captor.getValue();
            org.assertj.core.api.Assertions.assertThat(actual)
                .extracting(
                    RequestPayoutMessage::humanReadableId,
                    RequestPayoutMessage::costCenter,
                    RequestPayoutMessage::resource,
                    RequestPayoutMessage::actualCost,
                    RequestPayoutMessage::transportType
                )
                .containsExactly(
                    "humanReadableId", "4661", "26511", BigDecimal.ZERO, "YANDEX_TAXI"
                );
        }
    }

    @Test
    @DisplayName("Проверка согласования заявки на поездку - Согласовано")
    void test_edit_approve_confirmed() {
        final var oldState = CONFIRMATION;
        final var newState = CONFIRMED;
        final var organizationId = UUID.randomUUID();
        final var id = UUID.randomUUID();
        final var userId = UUID.randomUUID();
        final var newData = Instancio.of(TestTripOrder.class)
            .set(Select.field(TestTripOrder::getStatus), newState)
            .create();
        final var oldData = Instancio.of(TestTripOrder.class)
            .set(Select.field(TestTripOrder::getStatus), oldState)
            .create();
        final var oldDataAfterConfirm = Instancio.of(TestTripOrder.class)
            .set(Select.field(TestTripOrder::getStatus), newState)
            .create();
        final var department = Instancio.create(TestDepartment.class);

        when(departmentsProvider.get(any())).thenReturn(department);
        when(tripOrdersProvider.get(any(), any()))
            .thenReturn(Optional.of(oldData))
            .thenReturn(Optional.of(oldDataAfterConfirm));
        doReturn(List.of(Instancio.of(TestTripOrderHistory.class)
            .set(Select.field(TestTripOrderHistory::getModifiedAt), LocalDateTime.now(clock).atOffset(ZoneOffset.UTC))
            .set(Select.field(TestTripOrderHistory::getStatus), ORDER_PAYMENT_FORMATION)
            .create()))
            .when(tripOrderHistoriesProvider).get(any(UUID.class));
        doReturn(new RequestPayoutMessage(
            UUID.randomUUID(), "humanReadableId", "4661", "26511", UUID.randomUUID(),
            BigDecimal.ZERO, UUID.randomUUID(), LocalDateTime.now(clock).atOffset(ZoneOffset.UTC),
            "YANDEX_TAXI", UUID.randomUUID(), null, null, null, null, null, null, null, null,
            null, null, null, null
        )).when(requestMapper).toRequestPayoutMessage(any(), any(), any());

        tripOrdersService.edit(userId, true, organizationId, id, newData, Set.of("=status"));

        final var newDataCaptor = ArgumentCaptor.forClass(TestTripOrder.class);
        final var newMessageCaptor = ArgumentCaptor.forClass(TestTripOrder.class);

        verify(tripOrdersProvider).edit(eq(userId), eq(id), newDataCaptor.capture(), eq(Set.of("=status")));
        verify(notificationSender).send(any(), eq("EXTERNAL_REQUEST_APPROVED"), any());

        assertThat(newDataCaptor.getValue().getStatus().name()).isEqualTo(newState.name());

        verify(sender, times(2)).send(newMessageCaptor.capture());
        verify(limitsProvider).confirm(oldData.getId());

        assertThat(newMessageCaptor.getValue().getStatus().name()).isEqualTo(newState.name());
    }

    @Test
    @DisplayName("Проверка согласования делегатом заявки на поездку - Согласовано")
    void test_edit_approve_by_delegate_confirmed() {
        final var oldState = CONFIRMATION;
        final var newState = CONFIRMED;
        final var organizationId = UUID.randomUUID();
        final var id = UUID.randomUUID();

        final var newData = Instancio.of(TestTripOrder.class)
            .set(Select.field(TestTripOrder::getStatus), newState)
            .create();
        final var oldData = Instancio.of(TestTripOrder.class)
            .set(Select.field(TestTripOrder::getStatus), oldState)
            .create();
        final var oldDataAfterConfirm = Instancio.of(TestTripOrder.class)
            .set(Select.field(TestTripOrder::getStatus), newState)
            .create();
        final var department = Instancio.create(TestDepartment.class);
        final var delegate = Instancio.create(TestEmployee.class);
        final var userId = delegate.getId;

        when(departmentsProvider.get(any())).thenReturn(department);
        when(delegatesProvider.get(any())).thenReturn(List.of(delegate));
        when(tripOrdersProvider.get(any(), any()))
            .thenReturn(Optional.of(oldData))
            .thenReturn(Optional.of(oldDataAfterConfirm));
        doReturn(List.of(Instancio.of(TestTripOrderHistory.class)
            .set(Select.field(TestTripOrderHistory::getModifiedAt), LocalDateTime.now(clock).atOffset(ZoneOffset.UTC))
            .set(Select.field(TestTripOrderHistory::getStatus), ORDER_PAYMENT_FORMATION)
            .create()))
            .when(tripOrderHistoriesProvider).get(any(UUID.class));
        doReturn(new RequestPayoutMessage(
            UUID.randomUUID(), "humanReadableId", "4661", "26511", UUID.randomUUID(),
            BigDecimal.ZERO, UUID.randomUUID(), LocalDateTime.now(clock).atOffset(ZoneOffset.UTC),
            "YANDEX_TAXI", UUID.randomUUID(), null, null, null, null, null, null, null, null,
            null, null, null, null
        )).when(requestMapper).toRequestPayoutMessage(any(), any(), any());

        tripOrdersService.edit(userId, false, organizationId, id, newData, Set.of("=status"));

        final var newDataCaptor = ArgumentCaptor.forClass(TestTripOrder.class);
        final var newMessageCaptor = ArgumentCaptor.forClass(TestTripOrder.class);

        verify(tripOrdersProvider).edit(eq(userId), eq(id), newDataCaptor.capture(), eq(Set.of("=status")));
        verify(notificationSender).send(any(), eq("EXTERNAL_REQUEST_APPROVED"), any());

        assertThat(newDataCaptor.getValue().getStatus().name()).isEqualTo(newState.name());

        verify(sender, times(2)).send(newMessageCaptor.capture());
        verify(limitsProvider).confirm(oldData.getId());

        assertThat(newMessageCaptor.getValue().getStatus().name()).isEqualTo(newState.name());
    }

    @Test
    @DisplayName("Проверка согласования руководителем заявки на поездку - Согласовано")
    void test_edit_approve_by_head_confirmed() {
        final var oldState = CONFIRMATION;
        final var newState = CONFIRMED;
        final var organizationId = UUID.randomUUID();
        final var id = UUID.randomUUID();

        final var newData = Instancio.of(TestTripOrder.class)
            .set(Select.field(TestTripOrder::getStatus), newState)
            .create();
        final var oldData = Instancio.of(TestTripOrder.class)
            .set(Select.field(TestTripOrder::getStatus), oldState)
            .create();
        final var oldDataAfterConfirm = Instancio.of(TestTripOrder.class)
            .set(Select.field(TestTripOrder::getStatus), newState)
            .create();
        final var department = Instancio.create(TestDepartment.class);
        final var userId = department.getHeadId();

        when(departmentsProvider.get(any())).thenReturn(department);
        when(tripOrdersProvider.get(any(), any()))
            .thenReturn(Optional.of(oldData))
            .thenReturn(Optional.of(oldDataAfterConfirm));
        doReturn(List.of(Instancio.of(TestTripOrderHistory.class)
            .set(Select.field(TestTripOrderHistory::getModifiedAt), LocalDateTime.now(clock).atOffset(ZoneOffset.UTC))
            .set(Select.field(TestTripOrderHistory::getStatus), ORDER_PAYMENT_FORMATION)
            .create()))
            .when(tripOrderHistoriesProvider).get(any(UUID.class));
        doReturn(new RequestPayoutMessage(
            UUID.randomUUID(), "humanReadableId", "4661", "26511", UUID.randomUUID(),
            BigDecimal.ZERO, UUID.randomUUID(), LocalDateTime.now(clock).atOffset(ZoneOffset.UTC),
            "YANDEX_TAXI", UUID.randomUUID(), null, null, null, null, null, null, null, null,
            null, null, null, null
        )).when(requestMapper).toRequestPayoutMessage(any(), any(), any());

        tripOrdersService.edit(userId, false, organizationId, id, newData, Set.of("=status"));

        final var newDataCaptor = ArgumentCaptor.forClass(TestTripOrder.class);
        final var newMessageCaptor = ArgumentCaptor.forClass(TestTripOrder.class);

        verify(tripOrdersProvider).edit(eq(userId), eq(id), newDataCaptor.capture(), eq(Set.of("=status")));
        verify(notificationSender).send(any(), eq("EXTERNAL_REQUEST_APPROVED"), any());

        assertThat(newDataCaptor.getValue().getStatus().name()).isEqualTo(newState.name());

        verify(sender, times(2)).send(newMessageCaptor.capture());
        verify(limitsProvider).confirm(oldData.getId());

        assertThat(newMessageCaptor.getValue().getStatus().name()).isEqualTo(newState.name());
    }

    @Test
    @DisplayName("Проверка согласования руководителем (с назначенными делегатами) заявки на поездку - Согласовано")
    void test_edit_approve_by_head_with_delegates_confirmed() {
        final var oldState = CONFIRMATION;
        final var newState = CONFIRMED;
        final var organizationId = UUID.randomUUID();
        final var id = UUID.randomUUID();

        final var newData = Instancio.of(TestTripOrder.class)
            .set(Select.field(TestTripOrder::getStatus), newState)
            .create();
        final var oldData = Instancio.of(TestTripOrder.class)
            .set(Select.field(TestTripOrder::getStatus), oldState)
            .create();
        final var oldDataAfterConfirm = Instancio.of(TestTripOrder.class)
            .set(Select.field(TestTripOrder::getStatus), newState)
            .create();
        final var department = Instancio.create(TestDepartment.class);
        final var userId = department.getHeadId();

        final var delegate = Instancio.create(TestEmployee.class);

        when(delegatesProvider.get(any())).thenReturn(List.of(delegate));
        when(departmentsProvider.get(any())).thenReturn(department);
        when(tripOrdersProvider.get(any(), any()))
            .thenReturn(Optional.of(oldData))
            .thenReturn(Optional.of(oldDataAfterConfirm));
        doReturn(List.of(Instancio.of(TestTripOrderHistory.class)
            .set(Select.field(TestTripOrderHistory::getModifiedAt), LocalDateTime.now(clock).atOffset(ZoneOffset.UTC))
            .set(Select.field(TestTripOrderHistory::getStatus), ORDER_PAYMENT_FORMATION)
            .create()))
            .when(tripOrderHistoriesProvider).get(any(UUID.class));
        doReturn(new RequestPayoutMessage(
            UUID.randomUUID(), "humanReadableId", "4661", "26511", UUID.randomUUID(),
            BigDecimal.ZERO, UUID.randomUUID(), LocalDateTime.now(clock).atOffset(ZoneOffset.UTC),
            "YANDEX_TAXI", UUID.randomUUID(), null, null, null, null, null, null, null, null,
            null, null, null, null
        )).when(requestMapper).toRequestPayoutMessage(any(), any(), any());

        tripOrdersService.edit(userId, false, organizationId, id, newData, Set.of("=status"));

        final var newDataCaptor = ArgumentCaptor.forClass(TestTripOrder.class);
        final var newMessageCaptor = ArgumentCaptor.forClass(TestTripOrder.class);

        verify(tripOrdersProvider).edit(eq(userId), eq(id), newDataCaptor.capture(), eq(Set.of("=status")));
        verify(notificationSender).send(any(), eq("EXTERNAL_REQUEST_APPROVED"), any());

        assertThat(newDataCaptor.getValue().getStatus().name()).isEqualTo(newState.name());

        verify(sender, times(2)).send(newMessageCaptor.capture());
        verify(limitsProvider).confirm(oldData.getId());

        assertThat(newMessageCaptor.getValue().getStatus().name()).isEqualTo(newState.name());
    }

    @Test
    @DisplayName("Проверка согласования заявки на поездку - ")
    void test_edit_approve_confirmed_failed() {
        final var oldState = CONFIRMATION;
        final var newState = CONFIRMED;
        final var organizationId = UUID.randomUUID();
        final var id = UUID.randomUUID();

        final var newData = Instancio.of(TestTripOrder.class)
            .set(Select.field(TestTripOrder::getStatus), newState)
            .create();
        final var oldData = Instancio.of(TestTripOrder.class)
            .set(Select.field(TestTripOrder::getStatus), oldState)
            .create();

        final var department = Instancio.create(TestDepartment.class);
        final var userId = UUID.randomUUID();

        when(departmentsProvider.get(any())).thenReturn(department);
        when(tripOrdersProvider.get(any(), any()))
            .thenReturn(Optional.of(oldData));

        try {
            tripOrdersService.edit(userId, false, organizationId, id, newData, Set.of("=status"));
            fail("StatusSwitchForbiddenException не выброшен");
        } catch (StatusSwitchForbiddenException exception) {
            assertThat(exception).hasFieldOrPropertyWithValue("newState", oldState);
            assertThat(exception).hasFieldOrPropertyWithValue("allowedRole", "APPROVER");
        }
    }

    @Test
    @DisplayName("Проверка согласования заявки на поездку - Отклонено.")
    void test_edit_approve_declined() {
        final var oldState = CONFIRMATION;
        final var newState = DECLINED;
        final var organizationId = UUID.randomUUID();
        final var id = UUID.randomUUID();
        final var userId = UUID.randomUUID();
        final var newData = Instancio.of(TestTripOrder.class)
            .set(Select.field(TestTripOrder::getStatus), newState)
            .create();
        final var oldData = Instancio.of(TestTripOrder.class)
            .set(Select.field(TestTripOrder::getStatus), oldState)
            .create();
        final var department = Instancio.create(TestDepartment.class);

        when(departmentsProvider.get(any())).thenReturn(department);
        when(tripOrdersProvider.get(organizationId, id)).thenReturn(Optional.of(oldData));

        tripOrdersService.edit(userId, true, organizationId, id, newData, Set.of("=status"));

        final var newDataCaptor = ArgumentCaptor.forClass(TestTripOrder.class);
        final var newMessageCaptor = ArgumentCaptor.forClass(TestTripOrder.class);

        verify(tripOrdersProvider).edit(eq(userId), eq(id), newDataCaptor.capture(), eq(Set.of("=status")));
        verify(notificationSender).send(any(), eq("EXTERNAL_REQUEST_DECLINED"), any());

        assertThat(newDataCaptor.getValue().getStatus().name()).isEqualTo(newState.name());

        verify(sender).send(newMessageCaptor.capture());
        verify(limitsProvider).cancel(oldData.getId());

        assertThat(newMessageCaptor.getValue().getStatus().name()).isEqualTo(oldState.name());
    }

    @Test
    @DisplayName("Проверка согласования заявки на поездку - Требуются уточнения.")
    void test_edit_approve_data_needed() {
        final var oldState = CONFIRMATION;
        final var newState = DATA_NEEDED;
        final var organizationId = UUID.randomUUID();
        final var id = UUID.randomUUID();
        final var userId = UUID.randomUUID();
        final var newData = Instancio.of(TestTripOrder.class)
            .set(Select.field(TestTripOrder::getStatus), newState)
            .create();
        final var oldData = Instancio.of(TestTripOrder.class)
            .set(Select.field(TestTripOrder::getStatus), oldState)
            .create();
        final var department = Instancio.create(TestDepartment.class);

        when(departmentsProvider.get(any())).thenReturn(department);
        when(tripOrdersProvider.get(organizationId, id)).thenReturn(Optional.of(oldData));

        tripOrdersService.edit(userId, true, organizationId, id, newData, Set.of("=status"));

        final var newDataCaptor = ArgumentCaptor.forClass(TestTripOrder.class);
        final var newMessageCaptor = ArgumentCaptor.forClass(TestTripOrder.class);

        verify(tripOrdersProvider).edit(eq(userId), eq(id), newDataCaptor.capture(), eq(Set.of("=status")));
        verify(notificationSender).send(any(), eq("EXTERNAL_REQUEST_DATA_NEEDED"), any());

        assertThat(newDataCaptor.getValue().getStatus().name()).isEqualTo(newState.name());

        verify(sender).send(newMessageCaptor.capture());

        assertThat(newMessageCaptor.getValue().getStatus().name()).isEqualTo(oldState.name());
    }

    @Test
    @DisplayName("Проверка инициализации сервиса")
    void test_init() {
        final var days = new AtomicInteger(-100);
        final var orders = Instancio.ofList(TestTripOrder.class)
            .size(150)
            .set(Select.field(TestTripOrder::getStatus), NEW)
            .set(Select.field(TestTripOrder::getActual), Instancio.of(TestTripOrderData.class)
                .set(Select.field(TestTripOrderData::getDuration), Duration.ofMinutes(10)))
            .supply(Select.field(TestTripOrder::getDate),
                () -> OffsetDateTime.now(clock).plusDays(days.incrementAndGet()))
            .create().stream()
            .map(TripOrderData.class::cast).toList();
        final var ordersPage = new TestPage<>(orders, 0, 100);

        when(tripOrdersProvider.get(any(ReminderFilter.class), eq(0), eq(100), eq("date"), eq(true)))
            .thenReturn(ordersPage);
        for (final var item : orders) {
            when(tripOrdersProvider.get(null, item.getId())).thenReturn(Optional.of(item));
        }

        tripOrdersService.remind();

        verify(tripOrdersProvider, times((int) orders.stream().filter(
                it -> Duration.between(OffsetDateTime.now(clock), it.getDate().plus(Duration.ofMinutes(10))).isZero()
                    || Duration.between(OffsetDateTime.now(clock), it.getDate().plus(Duration.ofMinutes(10))).isNegative())
            .count())).edit(eq(null), any(UUID.class), any(), any());
    }

    @Test
    @DisplayName("Прикрепление чека: ошибка прикрепления файла в слое данных")
    void attachFile_attachFile_Error() {
        final var organizationId = UUID.randomUUID();
        final var requestId = UUID.randomUUID();
        final var fileName = UUID.randomUUID().toString();
        final var contentType = UUID.randomUUID().toString();
        final var file = Path.of(UUID.randomUUID().toString());
        final var messageEx = UUID.randomUUID().toString();
        doThrow(new DatabaseLayerException(messageEx)).when(tripOrdersProvider).attachFile(requestId, fileName);

        assertThrows(BusinessException.class, () ->
                tripOrdersService.attachFile(organizationId, requestId, fileName, contentType, file)
        );

        verify(tripOrdersProvider).attachFile(requestId, fileName);
        verify(filesProvider, never()).add(any(), any(), any());
        verify(tripOrdersProvider, never()).getFileDetailed(any());
        verify(receiptMapper, never()).toReceiptMessage(any());
        verify(receiptScannerSender, never()).send(any());
    }

    @Test
    @DisplayName("Прикрепление чека: ошибка отправки файла в s3")
    void attachFile_addFileToS3_Error() {
        final var organizationId = UUID.randomUUID();
        final var requestId = UUID.randomUUID();
        final var fileName = UUID.randomUUID().toString();
        final var contentType = UUID.randomUUID().toString();
        final var file = Path.of(UUID.randomUUID().toString());
        final var messageEx = UUID.randomUUID().toString();
        doNothing().when(tripOrdersProvider).attachFile(requestId, fileName);
        doThrow(new RuntimeException(messageEx)).when(filesProvider).add(requestId, file, contentType);

        assertThrows(BusinessException.class, () ->
                tripOrdersService.attachFile(organizationId, requestId, fileName, contentType, file)
        );

        verify(tripOrdersProvider).attachFile(requestId, fileName);
        verify(filesProvider).add(requestId, file, contentType);
        verify(tripOrdersProvider, never()).getFileDetailed(any());
        verify(receiptMapper, never()).toReceiptMessage(any());
        verify(receiptScannerSender, never()).send(any());
    }

    @Test
    @DisplayName("Проверка получения файла. Заявка не найдена")
    void test_getFile_notFound() {
        final var organizationId = UUID.randomUUID();
        final var requestId = UUID.randomUUID();
        final var from = 0;
        final var to = -1;

        try {
            tripOrdersService.getFile(organizationId, requestId, from, to);
            fail("EntityNotFoundException не выброшен");
        } catch (EntityNotFoundException exception) {
            assertThat(exception)
                .hasFieldOrPropertyWithValue("entityName", TripOrderData.class.getSimpleName())
                .hasFieldOrPropertyWithValue("entityId", requestId);
        }
    }

    @Test
    @DisplayName("Проверка получения файла")
    void test_getFile() {
        final var requestId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();
        final var from = 0;
        final var to = -1;
        final var fileData = new FileData() {

            @Override
            public Path getPath() {
                return Path.of("path");
            }

            @Override
            public long getFrom() {
                return 0;
            }

            @Override
            public long getTo() {
                return 20;
            }

            @Override
            public long getSize() {
                return 20;
            }

            @Override
            public String getContentType() {
                return "text/plain";
            }

            @Override
            public String getFileName() {
                return "file.txt";
            }
        };

        when(tripOrdersProvider.exists(organizationId, requestId)).thenReturn(true);
        when(filesProvider.get(requestId)).thenReturn(fileData);

        final var file = tripOrdersService.getFile(organizationId, requestId, from, to);

        assertThat(file).isEqualTo(fileData);
    }

    @Test
    @DisplayName("Проверка получения файла. Заявка найдена")
    void test_getFilePartially_notFound() {
        final var requestId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();
        final var from = 0;
        final var to = 100;

        try {
            tripOrdersService.getFile(organizationId, requestId, from, to);
            fail("EntityNotFoundException не выброшен");
        } catch (EntityNotFoundException exception) {
            assertThat(exception)
                .hasFieldOrPropertyWithValue("entityName", TripOrderData.class.getSimpleName())
                .hasFieldOrPropertyWithValue("entityId", requestId);
        }
    }

    @Test
    @DisplayName("Проверка получения части файла")
    void test_getFilePartially() {
        final var requestId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();
        final var from = 0;
        final var to = 100;
        final var fileData = new FileData() {

            @Override
            public Path getPath() {
                return Path.of("path");
            }

            @Override
            public long getFrom() {
                return 0;
            }

            @Override
            public long getTo() {
                return 100;
            }

            @Override
            public long getSize() {
                return 200;
            }

            @Override
            public String getContentType() {
                return "text/plain";
            }

            @Override
            public String getFileName() {
                return "file.txt";
            }
        };

        when(tripOrdersProvider.exists(organizationId, requestId)).thenReturn(true);
        when(filesProvider.get(requestId, 0, 100)).thenReturn(fileData);

        final var file = tripOrdersService.getFile(organizationId, requestId, from, to);

        assertThat(file).isEqualTo(fileData);
    }

    @Test
    @DisplayName("Проверка удаления файла. Заявка не найдена")
    void test_deleteFile_notFound() {
        final var requestId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();
        try {
            tripOrdersService.deleteFile(organizationId, requestId);
            fail("EntityNotFoundException не выброшен");
        } catch (EntityNotFoundException exception) {
            assertThat(exception)
                .hasFieldOrPropertyWithValue("entityName", TripOrderData.class.getSimpleName())
                .hasFieldOrPropertyWithValue("entityId", requestId);
        }
    }

    @Test
    @DisplayName("Проверка удаления файла")
    void test_deleteFile() {
        final var requestId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();
        when(tripOrdersProvider.exists(organizationId, requestId)).thenReturn(true);

        tripOrdersService.deleteFile(organizationId, requestId);

        verify(filesProvider).delete(requestId);
        verify(tripOrdersProvider).detachFile(requestId);
    }

    @Test
    @DisplayName("Проверка фрода SINGLE_TRIP_DURATION — длительность превышает порог")
    void test_create_singleTripDurationExceeded() {
        final var waypoints = List.of(Instancio.create(WaypointDTO.class));
        final var tripOrderRq = Instancio.of(TripOrderCreateDTO.class)
            .set(Select.field(TripOrderCreateDTO::waypoints), waypoints)
            .set(Select.field(TripOrderCreateDTO::tariff), ECONOMY)
            .create();
        final var singleTripDuration = Duration.ofHours(9);
        final var price = Instancio.of(TestPriceData.class)
            .set(Select.field(TestPriceData::duration), singleTripDuration)
            .create();
        final var timeZone = "+03:00";
        final var geoZone = Instancio.of(GeoZoneDTO.class)
            .set(Select.field(GeoZoneDTO::timeZone), timeZone)
            .create();
        final var testEmployee = Instancio.create(TestEmployee.class);
        final var userId = UUID.randomUUID();
        final var expected = Instancio.create(TestTripOrder.class);

        when(employeesProvider.get(userId)).thenReturn(testEmployee);
        when(availableClasses.exists(
            testEmployee.getOrganizationId(),
            testEmployee.getPositionId(),
            testEmployee.getDepartmentId()))
            .thenReturn(true);
        when(pricesProvider.get(tripOrderRq.waypoints(), tripOrderRq.tariff())).thenReturn(price);
        when(geoZonesProvider.get(any())).thenReturn(geoZone);
        when(tripOrdersProvider.create(any(), any(), any(), any())).thenReturn(expected);
        when(overrunCheckProvider.checkOverrunLimit(any(), any(), anyInt(), any())).thenReturn(
            new OverrunCheckResult(null, 0));

        tripOrdersService.create(userId, tripOrderRq);

        final var fraudCaptor = ArgumentCaptor.forClass(Fraud.class);
        verify(fraudsProvider).save(fraudCaptor.capture());

        final var fraud = fraudCaptor.getValue();
        assertThat(fraud.getId()).isEqualTo(expected.getId());
        assertThat(fraud.getType()).isEqualTo("SINGLE_TRIP_DURATION");
        assertThat(fraud.getComment()).isEqualTo("Превышен лимит длительности одной заявки");
        verify(fraudMonitoringSender).send(any(FraudMonitoringMessage.class));
    }

    @Test
    @DisplayName("Проверка фрода SINGLE_TRIP_DURATION — длительность в точности равна порогу, не сохраняется")
    void test_create_singleTripDurationEqualToThreshold() {
        final var waypoints = List.of(Instancio.create(WaypointDTO.class));
        final var tripOrderRq = Instancio.of(TripOrderCreateDTO.class)
            .set(Select.field(TripOrderCreateDTO::waypoints), waypoints)
            .set(Select.field(TripOrderCreateDTO::tariff), ECONOMY)
            .create();
        final var singleTripDuration = Duration.ofMillis(28_800_000L);
        final var price = Instancio.of(TestPriceData.class)
            .set(Select.field(TestPriceData::duration), singleTripDuration)
            .create();
        final var timeZone = "+03:00";
        final var geoZone = Instancio.of(GeoZoneDTO.class)
            .set(Select.field(GeoZoneDTO::timeZone), timeZone)
            .create();
        final var testEmployee = Instancio.create(TestEmployee.class);
        final var userId = UUID.randomUUID();
        final var expected = Instancio.create(TestTripOrder.class);

        when(employeesProvider.get(userId)).thenReturn(testEmployee);
        when(availableClasses.exists(
            testEmployee.getOrganizationId(),
            testEmployee.getPositionId(),
            testEmployee.getDepartmentId()))
            .thenReturn(true);
        when(pricesProvider.get(tripOrderRq.waypoints(), tripOrderRq.tariff())).thenReturn(price);
        when(geoZonesProvider.get(any())).thenReturn(geoZone);
        when(tripOrdersProvider.create(any(), any(), any(), any())).thenReturn(expected);
        when(overrunCheckProvider.checkOverrunLimit(any(), any(), anyInt(), any())).thenReturn(
            new OverrunCheckResult(null, 0));

        tripOrdersService.create(userId, tripOrderRq);

        verify(fraudsProvider, never()).save(any());
        verify(fraudMonitoringSender, never()).send(any(FraudMonitoringMessage.class));
    }

    @Test
    @DisplayName("Проверка фрода SINGLE_TRIP_DURATION — длительность меньше порога, не сохраняется")
    void test_create_singleTripDurationBelowThreshold() {
        final var waypoints = List.of(Instancio.create(WaypointDTO.class));
        final var tripOrderRq = Instancio.of(TripOrderCreateDTO.class)
            .set(Select.field(TripOrderCreateDTO::waypoints), waypoints)
            .set(Select.field(TripOrderCreateDTO::tariff), ECONOMY)
            .create();
        final var singleTripDuration = Duration.ofHours(7);
        final var price = Instancio.of(TestPriceData.class)
            .set(Select.field(TestPriceData::duration), singleTripDuration)
            .create();
        final var timeZone = "+03:00";
        final var geoZone = Instancio.of(GeoZoneDTO.class)
            .set(Select.field(GeoZoneDTO::timeZone), timeZone)
            .create();
        final var testEmployee = Instancio.create(TestEmployee.class);
        final var userId = UUID.randomUUID();
        final var expected = Instancio.create(TestTripOrder.class);

        when(employeesProvider.get(userId)).thenReturn(testEmployee);
        when(availableClasses.exists(
            testEmployee.getOrganizationId(),
            testEmployee.getPositionId(),
            testEmployee.getDepartmentId()))
            .thenReturn(true);
        when(pricesProvider.get(tripOrderRq.waypoints(), tripOrderRq.tariff())).thenReturn(price);
        when(geoZonesProvider.get(any())).thenReturn(geoZone);
        when(tripOrdersProvider.create(any(), any(), any(), any())).thenReturn(expected);
        when(overrunCheckProvider.checkOverrunLimit(any(), any(), anyInt(), any())).thenReturn(
            new OverrunCheckResult(null, 0));

        tripOrdersService.create(userId, tripOrderRq);

        verify(fraudsProvider, never()).save(any());
        verify(fraudMonitoringSender, never()).send(any(FraudMonitoringMessage.class));
    }

    @Test
    @DisplayName("Проверка фрода SINGLE_TRIP_DURATION — длительность null, не сохраняется")
    void test_create_singleTripDurationNull() {
        final var waypoints = List.of(Instancio.create(WaypointDTO.class));
        final var tripOrderRq = Instancio.of(TripOrderCreateDTO.class)
            .set(Select.field(TripOrderCreateDTO::waypoints), waypoints)
            .set(Select.field(TripOrderCreateDTO::tariff), ECONOMY)
            .create();
        final var price = Instancio.of(TestPriceData.class)
            .set(Select.field(TestPriceData::duration), null)
            .create();
        final var timeZone = "+03:00";
        final var geoZone = Instancio.of(GeoZoneDTO.class)
            .set(Select.field(GeoZoneDTO::timeZone), timeZone)
            .create();
        final var testEmployee = Instancio.create(TestEmployee.class);
        final var userId = UUID.randomUUID();
        final var expected = Instancio.create(TestTripOrder.class);

        when(employeesProvider.get(userId)).thenReturn(testEmployee);
        when(availableClasses.exists(
            testEmployee.getOrganizationId(),
            testEmployee.getPositionId(),
            testEmployee.getDepartmentId()))
            .thenReturn(true);
        when(pricesProvider.get(tripOrderRq.waypoints(), tripOrderRq.tariff())).thenReturn(price);
        when(geoZonesProvider.get(any())).thenReturn(geoZone);
        when(tripOrdersProvider.create(any(), any(), any(), any())).thenReturn(expected);
        when(overrunCheckProvider.checkOverrunLimit(any(), any(), anyInt(), any())).thenReturn(
            new OverrunCheckResult(null, 0));

        tripOrdersService.create(userId, tripOrderRq);

        verify(fraudsProvider, never()).save(any());
        verify(fraudMonitoringSender, never()).send(any(FraudMonitoringMessage.class));
    }

    private record TestTripOrder(
        UUID getId,
        String getHumanReadableId,
        TestTripOrderData getPlanned,
        TestTripOrderData getActual,
        UUID passengerId,
        OffsetDateTime getDate,
        List<WaypointData> getWaypoints,
        UUID getPurposeId,
        State getStatus,
        String getComment,
        String getReason,
        String getReceipt,
        Tariff getTariff,
        URI getLink,
        TestEmployee getPassenger,
        TestEmployee getApprover,
        TestAssessments getAssessments,
        String getCostCenter,
        TestFraud getFraud,
        String getTimeZone,
        OffsetDateTime getApprovalDate,
        BigDecimal getEconomy,
        BigDecimal getTaxiCost,
        String getReceiptLink
    ) implements TripOrderData {

    }

    private record TestAssessments(TestAssessment getService) implements Assessments {

    }

    private record TestAssessment(String getComment, byte getRating) implements Assessment {

    }

    private record TestTripOrderData(BigDecimal getCost, Duration getDuration, long getDistance) implements OrderData {

    }

    private record TestEmployee(UUID getId, String getFirstName, String getLastName, String getPatronymic,
                                UUID getDepartmentId, UUID getOrganizationId, UUID getPositionId,
                                String getPersonnelNumber, String getCostCenter) implements Employee {

    }

    private record TestPriceData(BigDecimal price, Duration duration, URI link, long distance) implements PriceData {

    }

    public record TestFraud(UUID getId, String getComment, String getType) implements Fraud {

    }

    public record TestWaypointData(UUID getId, String getCountry, String getRegion, String getCity, String getStreet,
                                   String getHouse,
                                   String getBuilding, String getStructure, BigDecimal getLatitude,
                                   BigDecimal getLongitude
    ) implements WaypointData {

    }

    private record TestPage<T>(List<T> content, int pageNumber, int pageSize) implements Page<T> {

        @Override
        public PageData page() {
            return new PageData() {
                @Override
                public int number() {
                    return pageNumber;
                }

                @Override
                public int size() {
                    return pageSize;
                }

                @Override
                public boolean last() {
                    return false;
                }

                @Override
                public boolean first() {
                    return false;
                }

                @Override
                public int total() {
                    return content.size();
                }

                @Override
                public int count() {
                    return 0;
                }
            };
        }

        @Override
        public SortData sort() {
            return new SortData() {
                @Override
                public String field() {
                    return "";
                }

                @Override
                public boolean asc() {
                    return false;
                }
            };
        }

    }

    private record TestDepartment(UUID getId, UUID getHeadId, UUID getParentId, String getName,
                                  DepartmentStatus getStatus, UUID getOrganizationId,
                                  Integer getLevel) implements Department {

    }

    private record TestTripOrderHistory(State getStatus, OffsetDateTime getModifiedAt, String getComment,
                                        String getReason) implements TripOrderHistory {

    }

    @Test
    @DisplayName("Проверка перехода ORDER_PAYMENT_FORMATION")
    void test_edit_status_genai_check_to_order_payment_formation() {
        final var oldState = CONFIRMED;
        final var newState = ORDER_PAYMENT_FORMATION;
        final var organizationId = UUID.randomUUID();
        final var id = UUID.randomUUID();
        final var userId = UUID.randomUUID();
        final var newData = Instancio.of(TestTripOrder.class)
            .set(Select.field(TestTripOrder::getStatus), newState)
            .create();
        final var oldData = Instancio.of(TestTripOrder.class)
            .set(Select.field(TestTripOrder::getStatus), oldState)
            .create();
        final var updatedOldData = Instancio.of(TestTripOrder.class)
            .set(Select.field(TestTripOrder::getStatus), newState)
            .create();

        when(tripOrdersProvider.get(any(), any()))
            .thenReturn(Optional.of(oldData))
            .thenReturn(Optional.of(updatedOldData));

        doReturn(List.of(Instancio.of(TestTripOrderHistory.class)
            .set(Select.field(TestTripOrderHistory::getModifiedAt), LocalDateTime.now(clock).atOffset(ZoneOffset.UTC))
            .set(Select.field(TestTripOrderHistory::getStatus), ORDER_PAYMENT_FORMATION)
            .create()))
            .when(tripOrderHistoriesProvider).get(any(UUID.class));
        doReturn(new RequestPayoutMessage(
            UUID.randomUUID(), "humanReadableId", "4661", "26511", UUID.randomUUID(),
            BigDecimal.ZERO, UUID.randomUUID(), LocalDateTime.now(clock).atOffset(ZoneOffset.UTC),
            "YANDEX_TAXI", UUID.randomUUID(), null, null, null, null, null, null, null, null,
            null, null, null, null
        )).when(requestMapper).toRequestPayoutMessage(any(), any(), any());

        tripOrdersService.edit(userId, true, organizationId, id, newData, Set.of("=status"));

        final var newDataCaptor = ArgumentCaptor.forClass(TestTripOrder.class);
        verify(tripOrdersProvider).edit(eq(userId), eq(id), newDataCaptor.capture(), eq(Set.of("=status")));
        assertThat(newDataCaptor.getValue().getStatus().name()).isEqualTo(newState.name());
    }

}