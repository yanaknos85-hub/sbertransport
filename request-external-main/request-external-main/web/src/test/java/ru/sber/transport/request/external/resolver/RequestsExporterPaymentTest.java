package ru.sber.transport.request.external.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.qameta.allure.Feature;
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
import ru.sber.transport.request.external.resolver.model.TripOrderRegistryPayment;
import ru.sber.transport.web.model.State;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка экспортера реестра поездок (Go)")
class RequestsExporterPaymentTest {
    private static final Integer OFFSET_IN_HOURS = 3;

    private final TripOrdersProvider tripOrdersProvider = mock(TripOrdersProvider.class);

    private final EmployeeOrganizationFunction employeeOrganizationFunction = mock(EmployeeOrganizationFunction.class);

    private final TripOrderHistoriesProvider histories = mock(TripOrderHistoriesProvider.class);

    private final DataExporter<TripOrderRegistryPayment> exporter = new RequestsExporterPayment(tripOrdersProvider, histories, employeeOrganizationFunction);

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
                "startFrom", from,
                "startTo", to,
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
            assertData(actualList.get(actualIndex), firstPageContent.get(i));
        }
        for (var i = 0; i < secondPageContent.size(); i++, actualIndex++) {
            assertData(actualList.get(actualIndex), secondPageContent.get(i));
        }
    }

    @Test
    @DisplayName("Проверка экспорта с текстовым статусом")
    void test_export_with_text_status_in_filter() {
        final var from = Instancio.create(OffsetDateTime.class);
        final var to = Instancio.create(OffsetDateTime.class);
        final var statuses = "NEW";
        final var timeZone = "+" + OFFSET_IN_HOURS;
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
            assertData(actualList.get(actualIndex), firstPageContent.get(i));
        }
        for (var i = 0; i < secondPageContent.size(); i++, actualIndex++) {
            assertData(actualList.get(actualIndex), secondPageContent.get(i));
        }
    }

    private static void assertData(TripOrderRegistryPayment actual, TripOrderData expected) {
        assertSoftly(it -> {
            it.assertThat(actual.getHumanReadableId()).isEqualTo(expected.getHumanReadableId());
            it.assertThat(actual.getPaymentCode()).isEqualTo(4666);
            it.assertThat(actual.getResource()).isEqualTo("31530");
            it.assertThat(actual.getFactCost()).isEqualTo(expected.getActual().getCost());
            it.assertThat(actual.getPersonnelNumber()).isEqualTo(expected.getPassenger().getPersonnelNumber());
            it.assertThat(actual.getFio()).isEqualTo("%s %s %s".formatted(expected.getPassenger().getFirstName(), expected.getPassenger().getPatronymic(), expected.getPassenger().getLastName()));
            it.assertThat(actual.getPeriod()).isEqualTo(getPeriod(expected.getDate()));

        });
    }

    private static Integer getPeriod(OffsetDateTime dateTime) {
        final var offset = ZoneOffset.of("+03:00");
        int day = dateTime.atZoneSameInstant(offset).toLocalDateTime().getDayOfMonth();
        if (day <= 7) {
            return 1;
        } else if (day <= 15) {
            return 2;
        } else if (day <= 23) {
            return 3;
        } else {
            return 4;
        }
    }
}