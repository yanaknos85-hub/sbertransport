package ru.sber.transport.request.external.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static ru.sber.transport.request.external.model.State.GENAI_CHECK;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.qameta.allure.Feature;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.business.providers.TripOrderHistoriesProvider;
import ru.sber.transport.business.providers.TripOrdersProvider;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sber.transport.request.external.model.TestPageData;
import ru.sber.transport.request.external.model.TestSortData;
import ru.sber.transport.request.external.model.TestTripOrder;
import ru.sber.transport.request.external.model.TestTripOrderPage;
import ru.sber.transport.request.external.model.TripOrderData;
import ru.sber.transport.request.external.model.WaypointData;
import ru.sber.transport.request.external.resolver.model.TripOrderRegistry;
import ru.sber.transport.web.model.State;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка экспортера заявок")
class RequestsExporterTest {
    private static final Integer OFFSET_IN_HOURS = 3;
    private static final Integer UTC_OFFSET_IN_HOURS = 0;

    private final TripOrdersProvider tripOrdersProvider = mock(TripOrdersProvider.class);

    private final EmployeeOrganizationFunction employeeOrganizationFunction = mock(EmployeeOrganizationFunction.class);

    private final TripOrderHistoriesProvider histories = mock(TripOrderHistoriesProvider.class);

    private final DataExporter<TripOrderRegistry> exporter = new RequestsExporter(tripOrdersProvider, histories, employeeOrganizationFunction);

    @Test
    @DisplayName("Проверка экспорта")
    void test_export() {
        final var from = "2025-06-03T20:59:59.999Z";
        final var to = "2025-06-03T20:59:59.999Z";
        final var timeZone = "+" + OFFSET_IN_HOURS;
        final var statuses = Instancio.createList(State.class);
        final var approvers = Instancio.createList(UUID.class);
        final var passegers = Instancio.createList(UUID.class);
        final var approverName = Instancio.create(String.class);
        final var passengerName = Instancio.create(String.class);
        final var parameters = Map.of(
                "startTimeFrom", from,
                "startTimeTo", to,
                "status", statuses,
                "approver", approvers,
                "passenger", passegers,
                "approverName", approverName,
                "passengerName", passengerName,
                "organizationId", UUID.randomUUID().toString()
        );
        final int size = 20;
        final var firstPageContent = Instancio.ofList(TestTripOrder.class)
                .size(size)
                .set(field(TestTripOrder::getTimeZone), timeZone)
                .create()
                .stream().map(TripOrderData.class::cast).toList();
        final var secondPageContent = Instancio.ofList(TestTripOrder.class)
                .size(new Random().nextInt(1, size - 1))
                .set(field(TestTripOrder::getTimeZone), timeZone)
                .create()
                .stream().map(TripOrderData.class::cast).toList();
        final var fistPage = new TestPageData(0, size, false, true,
                firstPageContent.size() + secondPageContent.size(), (firstPageContent.size() + secondPageContent.size()) / size);
        final var secondPage = new TestPageData(1, size, true, false,
                firstPageContent.size() + secondPageContent.size(), (firstPageContent.size() + secondPageContent.size()) / size);
        final var sort = new TestSortData("humanReadableId", true);

        when(tripOrdersProvider.getRegistry(any(), eq(0), eq(size), eq("humanReadableId"), eq(true)))
                .thenReturn(new TestTripOrderPage(firstPageContent, fistPage, sort));

        when(tripOrdersProvider.getRegistry(any(), eq(1), eq(size), eq("humanReadableId"), eq(true)))
                .thenReturn(new TestTripOrderPage(secondPageContent, secondPage, sort));

        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        final var token = new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).claim("data_master", false).build());

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);

        final var actualList = exporter.exportData(parameters, token);

        assertThat(exporter.getCaption()).isEqualTo("КОНФИДЕНЦИАЛЬНО");
        assertThat(actualList).hasSize(firstPageContent.size() + secondPageContent.size());
        var actualIndex = 0;
        for (var i = 0; i < firstPageContent.size(); i++, actualIndex++) {
            assertData(actualList.get(actualIndex), firstPageContent.get(i), OFFSET_IN_HOURS);
        }
        for (var i = 0; i < secondPageContent.size(); i++, actualIndex++) {
            assertData(actualList.get(actualIndex), secondPageContent.get(i), OFFSET_IN_HOURS);
        }
    }

    @Test
    @DisplayName("Проверка экспорта с текстовым статусом")
    void test_export_with_text_status_in_filter() {
        final var from = Instancio.create(OffsetDateTime.class);
        final var to = Instancio.create(OffsetDateTime.class);
        final var timeZone = "+" + OFFSET_IN_HOURS;
        final var statuses = "NEW";
        final var approvers = Instancio.createList(UUID.class);
        final var passegers = Instancio.createList(UUID.class);
        final var approverName = Instancio.create(String.class);
        final var passengerName = Instancio.create(String.class);
        final var parameters = Map.of(
                "startFrom", from,
                "startTo", to,
                "status", statuses,
                "approver", approvers,
                "passenger", passegers,
                "approverName", approverName,
                "passengerName", passengerName
        );
        final int size = 20;
        final var firstPageContent = Instancio.ofList(TestTripOrder.class)
                .size(size)
                .set(field(TestTripOrder::getTimeZone), timeZone)
                .create()
                .stream().map(TripOrderData.class::cast).toList();
        final var secondPageContent = Instancio.ofList(TestTripOrder.class)
                .size(new Random().nextInt(1, size - 1))
                .set(field(TestTripOrder::getTimeZone), timeZone)
                .create()
                .stream().map(TripOrderData.class::cast).toList();
        final var fistPage = new TestPageData(0, size, false, true,
                firstPageContent.size() + secondPageContent.size(), (firstPageContent.size() + secondPageContent.size()) / size);
        final var secondPage = new TestPageData(1, size, true, false,
                firstPageContent.size() + secondPageContent.size(), (firstPageContent.size() + secondPageContent.size()) / size);
        final var sort = new TestSortData("humanReadableId", true);

        when(tripOrdersProvider.getRegistry(any(), eq(0), eq(size), eq("humanReadableId"), eq(true)))
                .thenReturn(new TestTripOrderPage(firstPageContent, fistPage, sort));

        when(tripOrdersProvider.getRegistry(any(), eq(1), eq(size), eq("humanReadableId"), eq(true)))
                .thenReturn(new TestTripOrderPage(secondPageContent, secondPage, sort));

        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        final var token = new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).claim("data_master", false).build());

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);

        final var actualList = exporter.exportData(parameters, token);

        assertThat(exporter.getCaption()).isEqualTo("КОНФИДЕНЦИАЛЬНО");
        assertThat(actualList).hasSize(firstPageContent.size() + secondPageContent.size());
        var actualIndex = 0;
        for (var i = 0; i < firstPageContent.size(); i++, actualIndex++) {
            assertData(actualList.get(actualIndex), firstPageContent.get(i), OFFSET_IN_HOURS);
        }
        for (var i = 0; i < secondPageContent.size(); i++, actualIndex++) {
            assertData(actualList.get(actualIndex), secondPageContent.get(i), OFFSET_IN_HOURS);
        }
    }

    @Test
    @DisplayName("Проверка экспорта с текстовым статусом для заявок без информации о таймзоне")
    void test_export_with_text_status_in_filter_without_time_zone() {
        final var from = Instancio.create(OffsetDateTime.class);
        final var to = Instancio.create(OffsetDateTime.class);
        final var statuses = "NEW";
        final var approvers = Instancio.createList(UUID.class);
        final var passegers = Instancio.createList(UUID.class);
        final var approverName = Instancio.create(String.class);
        final var passengerName = Instancio.create(String.class);
        final var parameters = Map.of(
                "startFrom", from,
                "startTo", to,
                "status", statuses,
                "approver", approvers,
                "passenger", passegers,
                "approverName", approverName,
                "passengerName", passengerName
        );
        final int size = 20;
        final var firstPageContent = Instancio.ofList(TestTripOrder.class)
                .size(size)
                .create()
                .stream().map(TripOrderData.class::cast).toList();
        final var secondPageContent = Instancio.ofList(TestTripOrder.class)
                .size(new Random().nextInt(1, size - 1))
                .create()
                .stream().map(TripOrderData.class::cast).toList();
        final var fistPage = new TestPageData(0, size, false, true,
                firstPageContent.size() + secondPageContent.size(), (firstPageContent.size() + secondPageContent.size()) / size);
        final var secondPage = new TestPageData(1, size, true, false,
                firstPageContent.size() + secondPageContent.size(), (firstPageContent.size() + secondPageContent.size()) / size);
        final var sort = new TestSortData("humanReadableId", true);

        when(tripOrdersProvider.getRegistry(any(), eq(0), eq(size), eq("humanReadableId"), eq(true)))
                .thenReturn(new TestTripOrderPage(firstPageContent, fistPage, sort));

        when(tripOrdersProvider.getRegistry(any(), eq(1), eq(size), eq("humanReadableId"), eq(true)))
                .thenReturn(new TestTripOrderPage(secondPageContent, secondPage, sort));

        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        final var token = new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).claim("data_master", false).build());

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);

        final var actualList = exporter.exportData(parameters, token);

        assertThat(exporter.getCaption()).isEqualTo("КОНФИДЕНЦИАЛЬНО");
        assertThat(actualList).hasSize(firstPageContent.size() + secondPageContent.size());
        var actualIndex = 0;
        for (var i = 0; i < firstPageContent.size(); i++, actualIndex++) {
            assertData(actualList.get(actualIndex), firstPageContent.get(i), UTC_OFFSET_IN_HOURS);
        }
        for (var i = 0; i < secondPageContent.size(); i++, actualIndex++) {
            assertData(actualList.get(actualIndex), secondPageContent.get(i), UTC_OFFSET_IN_HOURS);
        }
    }

    private static void assertData(TripOrderRegistry actual, TripOrderData expected, Integer offsetInHours) {
        assertSoftly(it -> {
            it.assertThat(actual.getApprovalDate()).isNull();
            it.assertThat(actual.getApprover()).isEqualTo("%s %s %s".formatted(expected.getApprover().getFirstName(), expected.getApprover().getPatronymic(), expected.getApprover().getLastName()));
            it.assertThat(actual.getFactCost()).isEqualTo(expected.getActual().getCost());
            it.assertThat(actual.getNumber()).isEqualTo(expected.getHumanReadableId());
            it.assertThat(actual.getPassenger()).isEqualTo("%s %s %s".formatted(expected.getPassenger().getFirstName(), expected.getPassenger().getPatronymic(), expected.getPassenger().getLastName()));
            it.assertThat(actual.getPlannedCost()).isEqualTo(expected.getPlanned().getCost());
            it.assertThat(actual.getTripDate()).isEqualTo(expected.getDate().atZoneSameInstant(ZoneOffset.ofHours(offsetInHours)).toLocalDateTime());
            it.assertThat(actual.getStartPoint()).isEqualTo(getAddress(expected.getWaypoints().get(0)));
            it.assertThat(actual.getEndPoint()).isEqualTo(getAddress(expected.getWaypoints().get(expected.getWaypoints().size() - 1)));
            it.assertThat(actual.getDistance()).isEqualTo(metersToKilometers(expected.getPlanned().getDistance()));
            it.assertThat(actual.getStatus()).isEqualTo(switch (expected.getStatus()) {
                case NEW -> "Создана";
                case CONFIRMATION_NEEDED -> "Требуется подтверждение завершения";
                case CONFIRMATION -> "Требуется утверждение руководителем";
                case CONFIRMED, DECLINED -> "Завершена";
                case GENAI_CHECK -> "Проверка GenAI";
                case DATA_NEEDED -> "Требуются сведения";
                case CANCELLED -> "Отменена";
                case ORDER_PAYMENT_FORMATION -> "Формирование приказа на выплату";
                case PAYMENT_AWAITING -> "Ожидание выплаты";
                case PAYMENT_DONE -> "Выплата произведена";
                case PAYMENT_NOT_DONE -> "Выплата не произведена";
            });
        });
    }

    private static String getAddress(WaypointData waypointData) {
        return "%s, %s, %s, %s, %s, корп. %s, стр. %s".formatted(waypointData.getCountry(), waypointData.getRegion(), waypointData.getCity(), waypointData.getStreet(), waypointData.getHouse(), waypointData.getBuilding(), waypointData.getStructure());
    }

    private static BigDecimal metersToKilometers(long meters) {
        return  BigDecimal.valueOf(meters)
                        .divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
    }
}