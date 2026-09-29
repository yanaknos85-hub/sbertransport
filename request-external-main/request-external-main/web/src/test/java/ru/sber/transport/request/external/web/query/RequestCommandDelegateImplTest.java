package ru.sber.transport.request.external.web.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.instancio.Select.field;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.qameta.allure.Feature;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.assertj.core.api.Java6StandardSoftAssertionsProvider;
import org.assertj.core.api.SoftAssertions;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.business.providers.DepartmentsProvider;
import ru.sber.transport.business.providers.TripOrderHistoriesProvider;
import ru.sber.transport.business.providers.TripOrdersProvider;
import ru.sber.transport.business.providers.TripOrdersMetaProvider;
import ru.sber.transport.request.external.model.Employee;
import ru.sber.transport.request.external.model.Modifiable;
import ru.sber.transport.request.external.model.TestPageData;
import ru.sber.transport.request.external.model.TestSortData;
import ru.sber.transport.request.external.model.TestTripOrder;
import ru.sber.transport.request.external.model.TestTripOrderHistory;
import ru.sber.transport.request.external.model.TestTripOrderPage;
import ru.sber.transport.request.external.model.TripOrderData;
import ru.sber.transport.request.external.model.TripOrderHistory;
import ru.sber.transport.request.external.model.WaypointData;
import ru.sber.transport.request.external.web.model.WebRequestFilter;
import ru.sber.transport.web.api.ExternalRequestQueryApi;
import ru.sber.transport.web.model.Assessments;
import ru.sber.transport.web.model.Format;
import ru.sber.transport.web.model.FraudComment;
import ru.sber.transport.web.model.FullExternalRequest;
import ru.sber.transport.web.model.GetReportRequest;
import ru.sber.transport.web.model.ListExternalRequest;
import ru.sber.transport.web.model.OrderKind;
import ru.sber.transport.web.model.RegistryExternalRequest;
import ru.sber.transport.web.model.RegistryFilterRq;
import ru.sber.transport.web.model.Sort;
import ru.sber.transport.web.model.SortDirection;
import ru.sber.transport.web.model.State;
import ru.sber.transport.web.model.TripOrderStatusHistory;
import ru.sber.transport.web.model.Waypoint;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка делегата заявок")
class RequestCommandDelegateImplTest {

    private final DepartmentsProvider departmentsProvider = mock(DepartmentsProvider.class);

    private final TripOrdersProvider tripOrdersProvider = mock(TripOrdersProvider.class);

    private final TripOrderHistoriesProvider tripOrderHistoriesProvider = mock(TripOrderHistoriesProvider.class);

    private final TripOrdersMetaProvider tripOrdersMetaProvider = mock(TripOrdersMetaProvider.class);

    private final EmployeeOrganizationFunction employeeOrganizationFunction = mock(EmployeeOrganizationFunction.class);

    private final ExternalRequestQueryApi controller = new RequestQueryDelegateImpl(departmentsProvider,
        tripOrdersProvider,
        tripOrderHistoriesProvider, tripOrdersMetaProvider, employeeOrganizationFunction);

    @Test
    @DisplayName("Проверка получения заявки на поездку с определенной датой изменения. Не менялось")
    void test_get_not_modified_defined() throws ExecutionException, InterruptedException {
        final var requestId = UUID.randomUUID();
        final var meta = Instancio.of(TestModifiable.class)
            .set(field(TestModifiable::modifiedAt), OffsetDateTime.parse("2020-02-20T00:00:00+03:00"))
            .create();
        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(
            Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);

        when(tripOrdersMetaProvider.meta(organizationId, requestId)).thenReturn(meta);

        final var response = controller.get(requestId, Optional.of(OffsetDateTime.parse("2020-02-22T00:00:00+03:00")))
            .get();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_MODIFIED);
        assertThat(response.getHeaders().getETag()).isEqualTo("\"%s\"".formatted(meta.hash));
        assertThat(response.getHeaders().getLastModified()).isEqualTo(meta.modifiedAt.toInstant().toEpochMilli());
    }

    @Test
    @DisplayName("Проверка получения заявки на поездку с определенной датой изменения. Менялось")
    void test_get_modified_defined() throws ExecutionException, InterruptedException {
        final var requestId = UUID.randomUUID();
        final var meta = Instancio.of(TestModifiable.class)
            .set(field(TestModifiable::modifiedAt), OffsetDateTime.parse("2020-02-22T00:00:00+03:00"))
            .create();
        final var tripOrder = Instancio.of(TestTripOrder.class)
            .set(field(TestTripOrder::getStatus), ru.sber.transport.request.external.model.State.NEW)
            .create();
        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(
            Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);

        when(tripOrdersMetaProvider.meta(organizationId, requestId)).thenReturn(meta);
        when(tripOrdersProvider.get(organizationId, requestId)).thenReturn(Optional.of(tripOrder));

        final var response = controller.get(requestId, Optional.of(OffsetDateTime.parse("2020-02-20T00:00:00+03:00")))
            .get();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getETag()).isEqualTo("\"%s\"".formatted(meta.hash));
        assertThat(response.getHeaders().getLastModified()).isEqualTo(meta.modifiedAt.toInstant().toEpochMilli());

        final var body = response.getBody();
        assertThat(body).isNotNull();

        assertData(body, tripOrder);
    }

    @Test
    @DisplayName("Проверка получения заявки на поездку.")
    void test_get_modified_not_defined() throws ExecutionException, InterruptedException {
        final var userId = UUID.randomUUID();
        final var organizationId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(
            Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        when(employeeOrganizationFunction.apply(userId)).thenReturn(organizationId);
        final var requestId = UUID.randomUUID();
        final var meta = Instancio.of(TestModifiable.class)
            .set(field(TestModifiable::modifiedAt), OffsetDateTime.parse("2020-02-22T00:00:00+03:00"))
            .create();
        final var tripOrder = Instancio.of(TestTripOrder.class)
            .set(field(TestTripOrder::getStatus), ru.sber.transport.request.external.model.State.NEW)
            .create();

        when(tripOrdersMetaProvider.meta(organizationId, requestId)).thenReturn(meta);
        when(tripOrdersProvider.get(organizationId, requestId)).thenReturn(Optional.of(tripOrder));

        final var response = controller.get(requestId, Optional.empty()).get();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getETag()).isEqualTo("\"%s\"".formatted(meta.hash));
        assertThat(response.getHeaders().getLastModified()).isEqualTo(meta.modifiedAt.toInstant().toEpochMilli());

        final var body = response.getBody();
        assertThat(body).isNotNull();

        assertSoftly(it -> {
            it.assertThat(body.getId()).isEqualTo(tripOrder.getId());
            it.assertThat(body.getPassengerId()).isEqualTo(tripOrder.getPassenger().getId());
            it.assertThat(body.getHumanReadableId()).isEqualTo(tripOrder.getHumanReadableId());
            it.assertThat(body.getTripDate()).isEqualTo(tripOrder.getDate());
            it.assertThat(body.getPurposeId()).isEqualTo(tripOrder.getPurposeId());
            it.assertThat(body.getStatus().name()).isEqualTo(tripOrder.getStatus().name());
            it.assertThat(body.getComment()).isEqualTo(tripOrder.getComment());
            it.assertThat(body.getWaypoints()).hasSameSizeAs(tripOrder.getWaypoints());
            it.assertThat(body.getPlannedCost()).isEqualTo(tripOrder.getPlanned().getCost());
            it.assertThat(body.getPlannedDuration()).isEqualTo(tripOrder.getPlanned().getDuration().toString());
            it.assertThat(body.getFactCost()).isEqualTo(tripOrder.getActual().getCost());
            it.assertThat(body.getReceipt()).isEqualTo(tripOrder.getReceipt());
            assertData(it, body.getAssessments(), tripOrder.getAssessments());

            final var actualWaypoints = body.getWaypoints();
            final var expectedWaypoints = tripOrder.getWaypoints();
            for (var i = 0; i < expectedWaypoints.size(); i++) {
                final var actualWaypoint = actualWaypoints.get(i);
                final var expectedWaypoint = expectedWaypoints.get(i);

                assertSoftly(soft -> assertData(soft, actualWaypoint, expectedWaypoint));
            }
        });
    }

    @Test
    @DisplayName("Проверка получения всех заявок на поездку.")
    void test_getAll() throws ExecutionException, InterruptedException {
        final var states = List.of(ru.sber.transport.web.model.State.CONFIRMED, ru.sber.transport.web.model.State.NEW);
        final var userId = UUID.randomUUID();
        final var startTimeFrom = Instancio.create(OffsetDateTime.class);
        final var startTimeTo = Instancio.create(OffsetDateTime.class);
        final var page = Instancio.create(Integer.class);
        final var size = Instancio.create(Integer.class);
        final var sort = Instancio.create(String.class);
        final var asc = Instancio.create(Boolean.class);
        final var tripOrdersList = Instancio.ofList(TestTripOrder.class)
            .size(Instancio.create(Integer.class) % 10 + 1)
            .set(field(TestTripOrder::getStatus), ru.sber.transport.request.external.model.State.CONFIRMED)
            .create()
            .stream().map(TripOrderData.class::cast)
            .toList();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(
            Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        final var tripOrdersPage = new TestTripOrderPage(tripOrdersList,
            new TestPageData(page, size, Instancio.create(Boolean.class), Instancio.create(Boolean.class),
                Instancio.create(Integer.class), Instancio.create(Integer.class)), new TestSortData(sort, asc));
        when(tripOrdersProvider.get(
            WebRequestFilter.builder().balanceUnitSet(List.of()).status(states).passenger(List.of(userId))
                .approver(List.of()).startTimeFrom(startTimeFrom).startTimeTo(startTimeTo).build(), page, size, sort,
            asc))
            .thenReturn(tripOrdersPage);

        final var response = controller.getAll(
            Optional.of(OrderKind.PASSENGER),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.of(states.stream().map(Enum::name).map(ru.sber.transport.web.model.State::valueOf).toList()),
            Optional.empty(),
            Optional.empty(),
            Optional.of(startTimeFrom),
            Optional.of(startTimeTo),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.of(page),
            Optional.of(size),
            Optional.of(sort),
            Optional.of(asc ? SortDirection.ASC : SortDirection.DESC)
        ).get();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        final var body = response.getBody();
        assertThat(body).isNotNull();

        final var content = body.getContent();
        assertThat(content).hasSameSizeAs(tripOrdersList);
        for (var i = 0; i < content.size(); i++) {
            final var rawActual = content.get(i);
            assertThat(rawActual).isInstanceOf(ListExternalRequest.class);
            final var actual = (ListExternalRequest) rawActual;
            final var expected = tripOrdersList.get(i);

            assertSoftly(soft -> {
                soft.assertThat(actual.getId()).isEqualTo(expected.getId());
                soft.assertThat(actual.getPassengerId()).isEqualTo(expected.getPassenger().getId());
                soft.assertThat(actual.getHumanReadableId()).isEqualTo(expected.getHumanReadableId());
                soft.assertThat(actual.getTripDate()).isEqualTo(expected.getDate());
                soft.assertThat(actual.getPurposeId()).isEqualTo(expected.getPurposeId());
                soft.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
                soft.assertThat(actual.getComment()).isEqualTo(expected.getComment());
                soft.assertThat(actual.getWaypoints()).hasSameSizeAs(expected.getWaypoints());
                soft.assertThat(actual.getReceipt()).hasSameSizeAs(expected.getReceipt());
                assertData(soft, actual.getAssessments(), expected.getAssessments());
            });
        }

        final var pageable = body.getPage();
        assertSoftly(it -> {
            it.assertThat(pageable.getNumber()).isEqualTo(page);
            it.assertThat(pageable.getSize()).isEqualTo(size);
        });

        final var sortData = body.getSort();
        assertSoftly(it -> {
            it.assertThat(sortData.getField()).isEqualTo(sort);
            it.assertThat(sortData.getDirection()).isEqualTo(asc ? Sort.DirectionEnum.ASC : Sort.DirectionEnum.DESC);
        });
    }

    @Test
    @DisplayName("Проверка получения всех заявок на поездку. СМД")
    void test_getAll_smd() throws ExecutionException, InterruptedException {
        final var states = List.of(ru.sber.transport.web.model.State.CONFIRMED, ru.sber.transport.web.model.State.NEW);
        final var passengerIds = Instancio.createList(UUID.class);
        final var approverIds = Instancio.createList(UUID.class);
        final var startTimeFrom = Instancio.create(OffsetDateTime.class);
        final var startTimeTo = Instancio.create(OffsetDateTime.class);
        final var page = Instancio.create(Integer.class);
        final var size = Instancio.create(Integer.class);
        final var sort = Instancio.create(String.class);
        final var asc = Instancio.create(Boolean.class);
        final var tripOrdersList = Instancio.ofList(TestTripOrder.class)
            .size(Instancio.create(Integer.class) % 10 + 1)
            .set(field(TestTripOrder::getStatus), ru.sber.transport.request.external.model.State.CONFIRMED)
            .create()
            .stream().map(TripOrderData.class::cast)
            .toList();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(
            Jwt.withTokenValue("token").header("algo", "none").jti(passengerIds.getFirst().toString())
                .claim("data_master", true).build()));

        final var tripOrdersPage = new TestTripOrderPage(tripOrdersList,
            new TestPageData(page, size, Instancio.create(Boolean.class), Instancio.create(Boolean.class),
                Instancio.create(Integer.class), Instancio.create(Integer.class)), new TestSortData(sort, asc));
        when(tripOrdersProvider.get(
            WebRequestFilter.builder().balanceUnitSet(List.of()).status(states).passenger(passengerIds)
                .approver(approverIds).startTimeFrom(startTimeFrom).startTimeTo(startTimeTo).build(), page, size, sort,
            asc))
            .thenReturn(tripOrdersPage);

        final var response = controller.getAll(
            Optional.of(OrderKind.PASSENGER),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.of(states.stream().map(Enum::name).map(ru.sber.transport.web.model.State::valueOf).toList()),
            Optional.of(passengerIds),
            Optional.of(approverIds),
            Optional.of(startTimeFrom),
            Optional.of(startTimeTo),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.of(page),
            Optional.of(size),
            Optional.of(sort),
            Optional.of(asc ? SortDirection.ASC : SortDirection.DESC)
        ).get();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        final var body = response.getBody();
        assertThat(body).isNotNull();

        final var content = body.getContent();
        assertThat(content).hasSameSizeAs(tripOrdersList);
        for (var i = 0; i < content.size(); i++) {
            final var rawActual = content.get(i);
            assertThat(rawActual).isInstanceOf(ListExternalRequest.class);
            final var actual = (ListExternalRequest) rawActual;
            final var expected = tripOrdersList.get(i);

            assertSoftly(soft -> {
                soft.assertThat(actual.getId()).isEqualTo(expected.getId());
                soft.assertThat(actual.getPassengerId()).isEqualTo(expected.getPassenger().getId());
                soft.assertThat(actual.getHumanReadableId()).isEqualTo(expected.getHumanReadableId());
                soft.assertThat(actual.getTripDate()).isEqualTo(expected.getDate());
                soft.assertThat(actual.getPurposeId()).isEqualTo(expected.getPurposeId());
                soft.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
                soft.assertThat(actual.getComment()).isEqualTo(expected.getComment());
                soft.assertThat(actual.getWaypoints()).hasSameSizeAs(expected.getWaypoints());
                soft.assertThat(actual.getReceipt()).hasSameSizeAs(expected.getReceipt());
                soft.assertThat(actual.getFraudComment())
                    .isNotNull()
                    .isNotEmpty()
                    .first()
                    .extracting(FraudComment::getText)
                    .isEqualTo(expected.getFraud().getComment());
                assertData(soft, actual.getAssessments(), expected.getAssessments());
            });
        }

        final var pageable = body.getPage();
        assertSoftly(it -> {
            it.assertThat(pageable.getNumber()).isEqualTo(page);
            it.assertThat(pageable.getSize()).isEqualTo(size);
        });

        final var sortData = body.getSort();
        assertSoftly(it -> {
            it.assertThat(sortData.getField()).isEqualTo(sort);
            it.assertThat(sortData.getDirection()).isEqualTo(asc ? Sort.DirectionEnum.ASC : Sort.DirectionEnum.DESC);
        });
    }

    @Test
    @Disabled("TRANSPORT-43457: будет исправлено после синхронизации transport-core")
    @DisplayName("Проверка получения всех заявок на поездку. Full")
    void test_getAll_full() throws ExecutionException, InterruptedException {
        final var states = List.of(ru.sber.transport.web.model.State.CONFIRMED, ru.sber.transport.web.model.State.NEW);
        final var userId = UUID.randomUUID();
        final var startTimeFrom = Instancio.create(OffsetDateTime.class);
        final var startTimeTo = Instancio.create(OffsetDateTime.class);
        final var page = Instancio.create(Integer.class);
        final var size = Instancio.create(Integer.class);
        final var sort = Instancio.create(String.class);
        final var asc = Instancio.create(Boolean.class);
        final var tripOrdersList = Instancio.ofList(TestTripOrder.class)
            .size(Instancio.create(Integer.class) % 10 + 1)
            .set(field(TestTripOrder::getStatus), ru.sber.transport.request.external.model.State.CONFIRMED)
            .create()
            .stream().map(TripOrderData.class::cast)
            .toList();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(
            Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        final var tripOrdersPage = new TestTripOrderPage(tripOrdersList,
            new TestPageData(page, size, Instancio.create(Boolean.class), Instancio.create(Boolean.class),
                Instancio.create(Integer.class), Instancio.create(Integer.class)), new TestSortData(sort, asc));
        when(tripOrdersProvider.getFull(
            WebRequestFilter.builder().balanceUnitSet(List.of()).status(states).passenger(List.of())
                .approver(List.of(userId)).startTimeFrom(startTimeFrom).startTimeTo(startTimeTo).build(), page, size,
            sort, asc))
            .thenReturn(tripOrdersPage);

        final var response = controller.getAll(
            Optional.of(OrderKind.APPROVAL),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.of(states.stream().map(Enum::name).map(ru.sber.transport.web.model.State::valueOf).toList()),
            Optional.empty(),
            Optional.empty(),
            Optional.of(startTimeFrom),
            Optional.of(startTimeTo),
            Optional.empty(),
            Optional.empty(),
            Optional.of(Format.FULL),
            Optional.of(page),
            Optional.of(size),
            Optional.of(sort),
            Optional.of(asc ? SortDirection.ASC : SortDirection.DESC)
        ).get();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        final var body = response.getBody();
        assertThat(body).isNotNull();

        final var content = body.getContent();
        assertThat(content).hasSameSizeAs(tripOrdersList);
        for (var i = 0; i < content.size(); i++) {
            final var rawActual = content.get(i);
            assertThat(rawActual).isInstanceOf(FullExternalRequest.class);
            final var actual = (FullExternalRequest) rawActual;
            final var expected = tripOrdersList.get(i);

            assertData(actual, expected);
            assertSoftly(soft -> {
                soft.assertThat(actual.getId()).isEqualTo(expected.getId());
                soft.assertThat(actual.getTariff().name()).isEqualTo(expected.getTariff().name());
                assertData(soft, actual.getPassenger(), expected.getPassenger());
                soft.assertThat(actual.getHumanReadableId()).isEqualTo(expected.getHumanReadableId());
                soft.assertThat(actual.getTripDate()).isEqualTo(expected.getDate());
                soft.assertThat(actual.getWaypoints()).hasSameSizeAs(expected.getWaypoints());
                soft.assertThat(actual.getPlannedCost()).isEqualTo(expected.getPlanned().getCost());
                soft.assertThat(actual.getFactCost()).isEqualTo(expected.getActual().getCost());
                soft.assertThat(actual.getPlannedDuration()).isEqualTo(expected.getPlanned().getDuration().toString());
                soft.assertThat(actual.getPurposeId()).isEqualTo(expected.getPurposeId());
                soft.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
                soft.assertThat(actual.getComment()).isEqualTo(expected.getComment());
                soft.assertThat(actual.getReceipt()).hasSameSizeAs(expected.getReceipt());
                soft.assertThat(actual.getReason()).hasSameSizeAs(expected.getReason());
                soft.assertThat(actual.getLink()).hasSameSizeAs(expected.getLink().toString());
                assertData(soft, actual.getAssessments(), expected.getAssessments());
                soft.assertThat(actual.getFraudComment())
                    .isNotNull()
                    .isNotEmpty()
                    .first()
                    .extracting(FraudComment::getText)
                    .isEqualTo(expected.getFraud().getComment());
            });
        }

        final var pageable = body.getPage();
        assertSoftly(it -> {
            it.assertThat(pageable.getNumber()).isEqualTo(page);
            it.assertThat(pageable.getSize()).isEqualTo(size);
        });

        final var sortData = body.getSort();
        assertSoftly(it -> {
            it.assertThat(sortData.getField()).isEqualTo(sort);
            it.assertThat(sortData.getDirection()).isEqualTo(asc ? Sort.DirectionEnum.ASC : Sort.DirectionEnum.DESC);
        });
    }

    @Test
    @Disabled("TRANSPORT-43457: будет исправлено после синхронизации transport-core")
    @DisplayName("Проверка получения всех заявок на поездку. Registry")
    void test_getAll_registry() throws ExecutionException, InterruptedException {
        final var states = List.of(ru.sber.transport.web.model.State.CONFIRMED, ru.sber.transport.web.model.State.NEW);
        final var userId = Instancio.create(UUID.class);
        final var startTimeFrom = Instancio.create(OffsetDateTime.class);
        final var startTimeTo = Instancio.create(OffsetDateTime.class);
        final var page = Instancio.create(Integer.class);
        final var size = Instancio.create(Integer.class);
        final var sort = Instancio.create(String.class);
        final var asc = Instancio.create(Boolean.class);
        final var tripOrdersList = Instancio.ofList(TestTripOrder.class)
            .size(Instancio.create(Integer.class) % 10 + 1)
            .set(field(TestTripOrder::getStatus), ru.sber.transport.request.external.model.State.CONFIRMED)
            .create()
            .stream().map(TripOrderData.class::cast)
            .toList();
        final var tripOrdersPage = new TestTripOrderPage(tripOrdersList,
            new TestPageData(page, size, Instancio.create(Boolean.class), Instancio.create(Boolean.class),
                Instancio.create(Integer.class), Instancio.create(Integer.class)), new TestSortData(sort, asc));

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(
            Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        WebRequestFilter filter = WebRequestFilter.builder()
            .humanReadableId("HRI")
            .balanceUnitSet(List.of())
            .status(states)
            .passenger(List.of(userId))
            .approver(List.of())
            .isStrictlyApprover(true)
            .startTimeFrom(startTimeFrom)
            .startTimeTo(startTimeTo)
            .build();
        when(tripOrdersProvider.getRegistry(filter, page, size, sort, asc))
            .thenReturn(tripOrdersPage);

        final var response = controller.getAll(
            Optional.empty(),
            Optional.of("HRI"),
            Optional.empty(),
            Optional.empty(),
            Optional.of(states.stream().map(Enum::name).map(ru.sber.transport.web.model.State::valueOf).toList()),
            Optional.of(List.of(userId)),
            Optional.of(List.of()),
            Optional.of(startTimeFrom),
            Optional.of(startTimeTo),
            Optional.empty(),
            Optional.empty(),
            Optional.of(Format.REGISTRY),
            Optional.of(page),
            Optional.of(size),
            Optional.of(sort),
            Optional.of(asc ? SortDirection.ASC : SortDirection.DESC)
        ).get();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        final var body = response.getBody();
        assertThat(body).isNotNull();

        final var content = body.getContent();
        assertThat(content).hasSameSizeAs(tripOrdersList);
        for (var i = 0; i < content.size(); i++) {
            final var rawActual = content.get(i);
            assertThat(rawActual).isInstanceOf(RegistryExternalRequest.class);
            final var actual = (RegistryExternalRequest) rawActual;
            final var expected = tripOrdersList.get(i);

            assertOrders(actual, expected);
        }

        final var pageable = body.getPage();
        assertSoftly(it -> {
            it.assertThat(pageable.getNumber()).isEqualTo(page);
            it.assertThat(pageable.getSize()).isEqualTo(size);
        });

        final var sortData = body.getSort();
        assertSoftly(it -> {
            it.assertThat(sortData.getField()).isEqualTo(sort);
            it.assertThat(sortData.getDirection()).isEqualTo(asc ? Sort.DirectionEnum.ASC : Sort.DirectionEnum.DESC);
        });
    }

    @Test
    @Disabled("TRANSPORT-43457: будет исправлено после синхронизации transport-core")
    @DisplayName("Проверка получения отчета по заявкам на поездку.")
    void test_getReport() throws ExecutionException, InterruptedException {
        final var userId = UUID.randomUUID();
        final var page = Instancio.create(Integer.class);
        final var size = Instancio.create(Integer.class);
        final var sort = Instancio.create(String.class);
        final var asc = Instancio.create(Boolean.class);
        final var tripOrdersList = Instancio.ofList(TestTripOrder.class)
            .size(Instancio.create(Integer.class) % 10 + 1)
            .set(field(TestTripOrder::getStatus), ru.sber.transport.request.external.model.State.CONFIRMED)
            .create()
            .stream().map(TripOrderData.class::cast)
            .toList();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(
            Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        final var tripOrdersPage = new TestTripOrderPage(tripOrdersList,
            new TestPageData(page, size, Instancio.create(Boolean.class), Instancio.create(Boolean.class),
                Instancio.create(Integer.class), Instancio.create(Integer.class)), new TestSortData(sort, asc));
        when(tripOrdersProvider.getRegistry(any(), anyInt(), anyInt(), any(), anyBoolean())).thenReturn(tripOrdersPage);

        final var registryFilterRq = Instancio.create(RegistryFilterRq.class);
        final var getAllRq = new GetReportRequest();
        getAllRq.setFilter(registryFilterRq);

        final var response = controller.getReport(getAllRq).get();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        final var body = response.getBody();
        assertThat(body).isNotNull();

        final var content = body.getContent();
        assertThat(content).hasSameSizeAs(tripOrdersList);
        for (var i = 0; i < content.size(); i++) {
            final var rawActual = content.get(i);
            assertThat(rawActual).isInstanceOf(RegistryExternalRequest.class);
            final var actual = (RegistryExternalRequest) rawActual;
            final var expected = tripOrdersList.get(i);

            assertSoftly(soft -> {
                soft.assertThat(actual.getId()).isEqualTo(expected.getId());
                soft.assertThat(actual.getHumanReadableId()).isEqualTo(expected.getHumanReadableId());
                soft.assertThat(actual.getTripDate()).isEqualTo(expected.getDate());
                soft.assertThat(actual.getPurposeId()).isEqualTo(expected.getPurposeId());
                soft.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
                soft.assertThat(actual.getComment()).isEqualTo(expected.getComment());
                soft.assertThat(actual.getWaypoints()).hasSameSizeAs(expected.getWaypoints());
                soft.assertThat(actual.getReceipt()).hasSameSizeAs(expected.getReceipt());
                assertData(soft, actual.getAssessments(), expected.getAssessments());
                soft.assertThat(actual.getFraudComment())
                    .isNotNull()
                    .isNotEmpty()
                    .first()
                    .extracting(FraudComment::getText)
                    .isEqualTo(expected.getFraud().getComment());
            });
        }

        final var pageable = body.getPage();
        assertSoftly(it -> {
            it.assertThat(pageable.getNumber()).isEqualTo(page);
            it.assertThat(pageable.getSize()).isEqualTo(size);
        });

        final var sortData = body.getSort();
        assertSoftly(it -> {
            it.assertThat(sortData.getField()).isEqualTo(sort);
            it.assertThat(sortData.getDirection()).isEqualTo(asc ? Sort.DirectionEnum.ASC : Sort.DirectionEnum.DESC);
        });
    }

    @Test
    @Disabled("TRANSPORT-43457: будет исправлено после синхронизации transport-core")
    @DisplayName("Проверка получения истории статусов заявки на поездку.")
    void test_getStatusHistory() throws ExecutionException, InterruptedException {
        final var requestId = UUID.randomUUID();
        final var userId = Instancio.create(UUID.class);

        final var tripOrdersHistoryList = Instancio.ofList(TestTripOrderHistory.class)
            .size(State.values().length)
            .withUnique(field(TestTripOrderHistory::getStatus))
            .create()
            .stream()
            .map(TripOrderHistory.class::cast)
            .sorted(Comparator.comparing(TripOrderHistory::getModifiedAt))
            .toList();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(
            Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        when(tripOrderHistoriesProvider.get(any(UUID.class))).thenReturn(tripOrdersHistoryList);

        final var response = controller.getStatusHistory(requestId).get();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        final var content = response.getBody();

        assertThat(content)
            .isNotNull()
            .hasSameSizeAs(tripOrdersHistoryList)
            .allSatisfy(element -> assertThat(element).isInstanceOf(TripOrderStatusHistory.class))
            .satisfies(list -> {
                for (int i = 0; i < list.size(); i++) {
                    assertOrderHistories(
                        list.get(i),
                        tripOrdersHistoryList.get(i)
                    );
                }
            });
    }

    private static void assertOrders(RegistryExternalRequest actual, TripOrderData expected) {
        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(expected.getId());
            it.assertThat(actual.getTariff().name()).isEqualTo(expected.getTariff().name());
            assertEmployees(it, actual.getPassenger(), expected.getPassenger());
            assertEmployees(it, actual.getApprover(), expected.getApprover());
            it.assertThat(actual.getHumanReadableId()).isEqualTo(expected.getHumanReadableId());
            it.assertThat(actual.getTripDate()).isEqualTo(expected.getDate());
            it.assertThat(actual.getWaypoints()).hasSameSizeAs(expected.getWaypoints());
            it.assertThat(actual.getPlannedCost()).isEqualTo(expected.getPlanned().getCost());
            it.assertThat(actual.getFactCost()).isEqualTo(expected.getActual().getCost());
            it.assertThat(actual.getPlannedDuration()).isEqualTo(expected.getPlanned().getDuration().toString());
            it.assertThat(actual.getPurposeId()).isEqualTo(expected.getPurposeId());
            it.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
            it.assertThat(actual.getComment()).isEqualTo(expected.getComment());
            it.assertThat(actual.getReceipt()).hasSameSizeAs(expected.getReceipt());
            it.assertThat(actual.getReason()).hasSameSizeAs(expected.getReason());
            it.assertThat(actual.getLink()).hasSameSizeAs(expected.getLink().toString());
            assertData(it, actual.getAssessments(), expected.getAssessments());
        });
    }

    private static void assertEmployees(SoftAssertions it, ru.sber.transport.web.model.Employee actual,
        Employee expected) {
        it.assertThat(actual.getId()).isEqualTo(expected.getId());
        it.assertThat(actual.getLastName()).isEqualTo(expected.getLastName());
        it.assertThat(actual.getFirstName()).isEqualTo(expected.getFirstName());
        it.assertThat(actual.getPatronymic()).isEqualTo(expected.getPatronymic());
    }

    private void assertData(Java6StandardSoftAssertionsProvider it, ru.sber.transport.web.model.Employee actual,
        Employee expected) {
        it.assertThat(actual.getId()).isEqualTo(expected.getId());
        it.assertThat(actual.getFirstName()).isEqualTo(expected.getFirstName());
        it.assertThat(actual.getLastName()).isEqualTo(expected.getLastName());
        it.assertThat(actual.getPatronymic()).isEqualTo(expected.getPatronymic());
    }

    private void assertData(FullExternalRequest actual, TripOrderData expected) {
        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(expected.getId());
            assertData(it, actual.getPassenger(), expected.getPassenger());
            it.assertThat(actual.getHumanReadableId()).isEqualTo(expected.getHumanReadableId());
            it.assertThat(actual.getTripDate()).isEqualTo(expected.getDate());
            it.assertThat(actual.getPurposeId()).isEqualTo(expected.getPurposeId());
            it.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
            it.assertThat(actual.getComment()).isEqualTo(expected.getComment());
            it.assertThat(actual.getWaypoints()).hasSameSizeAs(expected.getWaypoints());
            it.assertThat(actual.getPlannedCost()).isEqualTo(expected.getPlanned().getCost());
            it.assertThat(actual.getPlannedDuration()).isEqualTo(expected.getPlanned().getDuration().toString());
            it.assertThat(actual.getFactCost()).isEqualTo(expected.getActual().getCost());
            assertData(it, actual.getAssessments(), expected.getAssessments());

            final var actualWaypoints = actual.getWaypoints();
            final var expectedWaypoints = expected.getWaypoints();
            for (var i = 0; i < expectedWaypoints.size(); i++) {
                int finalI = i;
                assertSoftly(soft -> assertData(soft, actualWaypoints.get(finalI), expectedWaypoints.get(finalI)));
            }
        });
    }

    private void assertData(SoftAssertions soft, Waypoint actual, WaypointData expected) {
        soft.assertThat(actual.getCountry()).isEqualTo(expected.getCountry());
        soft.assertThat(actual.getRegion()).isEqualTo(expected.getRegion());
        soft.assertThat(actual.getCity()).isEqualTo(expected.getCity());
        soft.assertThat(actual.getStreet()).isEqualTo(expected.getStreet());
        soft.assertThat(actual.getHouse()).isEqualTo(expected.getHouse());
        soft.assertThat(actual.getBuilding()).isEqualTo(expected.getBuilding());
        soft.assertThat(actual.getStructure()).isEqualTo(expected.getStructure());
        soft.assertThat(actual.getLongitude()).isEqualTo(expected.getLongitude());
        soft.assertThat(actual.getLatitude()).isEqualTo(expected.getLatitude());
    }

    private void assertData(ListExternalRequest actual, TestTripOrder expected) {
        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(expected.getId());
            it.assertThat(actual.getPassengerId()).isEqualTo(expected.getPassenger().getId());
            it.assertThat(actual.getHumanReadableId()).isEqualTo(expected.getHumanReadableId());
            it.assertThat(actual.getTripDate()).isEqualTo(expected.getDate());
            it.assertThat(actual.getPurposeId()).isEqualTo(expected.getPurposeId());
            it.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
            it.assertThat(actual.getComment()).isEqualTo(expected.getComment());
            it.assertThat(actual.getWaypoints()).hasSameSizeAs(expected.getWaypoints());
            it.assertThat(actual.getPlannedCost()).isEqualTo(expected.getPlanned().getCost());
            it.assertThat(actual.getPlannedDuration()).isEqualTo(expected.getPlanned().getDuration().toString());
            it.assertThat(actual.getFactCost()).isEqualTo(expected.getActual().getCost());
            assertData(it, actual.getAssessments(), expected.getAssessments());

            final var actualWaypoints = actual.getWaypoints();
            final var expectedWaypoints = expected.getWaypoints();
            for (var i = 0; i < expectedWaypoints.size(); i++) {
                int finalI = i;
                assertSoftly(soft -> assertData(soft, actualWaypoints.get(finalI), expectedWaypoints.get(finalI)));
            }
        });
    }

    private static void assertData(SoftAssertions it, Assessments actual,
        ru.sber.transport.request.external.model.Assessments expected) {
        it.assertThat(actual.getService().getRating()).isEqualTo(expected.getService().getRating());
        it.assertThat(actual.getService().getComment()).isEqualTo(expected.getService().getComment());
    }

    private static void assertOrderHistories(TripOrderStatusHistory actual, TripOrderHistory expected) {
        assertSoftly(it -> {
            it.assertThat(actual.getComment()).isEqualTo(expected.getComment());
            it.assertThat(actual.getReason()).isEqualTo(expected.getReason());
            it.assertThat(actual.getModifiedAt()).isEqualTo(expected.getModifiedAt());
            it.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
        });
    }

    private record TestModifiable(String hash, OffsetDateTime modifiedAt) implements Modifiable {

    }

}
