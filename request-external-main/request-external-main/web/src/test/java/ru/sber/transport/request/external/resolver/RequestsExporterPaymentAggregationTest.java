package ru.sber.transport.request.external.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.qameta.allure.Feature;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
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
import ru.sber.transport.request.external.resolver.model.TripOrderRegistryPaymentAggregation;
import ru.sber.transport.web.model.State;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка экспортера Агрегированного реестра (Go)")
class RequestsExporterPaymentAggregationTest {

    private final TripOrdersProvider tripOrdersProvider = mock(TripOrdersProvider.class);

    private final EmployeeOrganizationFunction employeeOrganizationFunction = mock(EmployeeOrganizationFunction.class);

    private final TripOrderHistoriesProvider histories = mock(TripOrderHistoriesProvider.class);

    private final DataExporter<TripOrderRegistryPaymentAggregation> exporter = new RequestsExporterPaymentAggregation(tripOrdersProvider, histories, employeeOrganizationFunction);

    @Test
    @DisplayName("Проверка экспорта")
    void test_export() {
        final var from = Instancio.create(OffsetDateTime.class);
        final var to = Instancio.create(OffsetDateTime.class);
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
                .create()
                .stream().map(TripOrderData.class::cast).toList();
        final var secondPageContent = Instancio.ofList(TestTripOrder.class)
                .size(new Random().nextInt(1, size - 1))
                .create()
                .stream().map(TripOrderData.class::cast).toList();

        final var combinedList = new ArrayList<>(firstPageContent);
        combinedList.addAll(secondPageContent);
        final var expectedMap = combinedList.stream().collect(Collectors.groupingBy(order -> order.getPassenger().getPersonnelNumber(), Collectors.toList()));
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

        final var actualMap = actualList.stream().collect(Collectors.toMap(TripOrderRegistryPaymentAggregation::getPersonnelNumber, Function.identity()));

        assertThat(exporter.getCaption()).isEqualTo("КОНФИДЕНЦИАЛЬНО");
        assertSoftly(it -> {
            it.assertThat(actualMap).hasSize(expectedMap.size());

            expectedMap.forEach((personnelNumber, orders) -> {
                it.assertThat(actualMap).containsKey(personnelNumber);

                var actualAgg = actualMap.get(personnelNumber);

                it.assertThat(actualAgg.getPersonnelNumber())
                        .isEqualTo(personnelNumber);

                orders.forEach(order -> {
                    it.assertThat(actualAgg.getFactCost()).isEqualTo(order.getActual().getCost());
                    it.assertThat(actualAgg.getFio()).isEqualTo("%s %s %s".formatted(order.getPassenger().getFirstName(), order.getPassenger().getPatronymic(), order.getPassenger().getLastName()));
                    it.assertThat(actualAgg.getPaymentCode()).isEqualTo(4666);
                    it.assertThat(actualAgg.getResource()).isEqualTo("31530");
                });
            });
        });
    }

    @Test
    @DisplayName("Проверка экспорта")
    void test_export_without_organization_id() {
        final var from = Instancio.create(OffsetDateTime.class);
        final var to = Instancio.create(OffsetDateTime.class);
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

        final var combinedList = new ArrayList<>(firstPageContent);
        combinedList.addAll(secondPageContent);
        final var expectedMap = combinedList.stream().collect(Collectors.groupingBy(order -> order.getPassenger().getPersonnelNumber(), Collectors.toList()));
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

        final var actualMap = actualList.stream().collect(Collectors.toMap(TripOrderRegistryPaymentAggregation::getPersonnelNumber, Function.identity()));

        assertThat(exporter.getCaption()).isEqualTo("КОНФИДЕНЦИАЛЬНО");
        assertSoftly(it -> {
            it.assertThat(actualMap).hasSize(expectedMap.size());

            expectedMap.forEach((personnelNumber, orders) -> {
                it.assertThat(actualMap).containsKey(personnelNumber);

                var actualAgg = actualMap.get(personnelNumber);

                it.assertThat(actualAgg.getPersonnelNumber())
                        .isEqualTo(personnelNumber);

                orders.forEach(order -> {
                    it.assertThat(actualAgg.getFactCost()).isEqualTo(order.getActual().getCost());
                    it.assertThat(actualAgg.getFio()).isEqualTo("%s %s %s".formatted(order.getPassenger().getFirstName(), order.getPassenger().getPatronymic(), order.getPassenger().getLastName()));
                    it.assertThat(actualAgg.getPaymentCode()).isEqualTo(4666);
                    it.assertThat(actualAgg.getResource()).isEqualTo("31530");
                });
            });
        });
    }
}