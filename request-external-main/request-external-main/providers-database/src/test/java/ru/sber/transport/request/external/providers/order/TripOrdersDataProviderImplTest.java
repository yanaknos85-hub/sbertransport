package ru.sber.transport.request.external.providers.order;

import io.qameta.allure.Feature;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.instancio.Instancio;
import org.instancio.Select;
import org.jooq.DSLContext;
import org.jooq.types.DayToSecond;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.database.config.JooqDatabaseConfig;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.business.providers.CurrentUser;
import ru.sber.transport.business.providers.EmployeesProvider;
import ru.sber.transport.business.providers.OrganizationsProvider;
import ru.sber.transport.business.providers.TripOrdersMetaProvider;
import ru.sber.transport.business.providers.TripOrdersProvider;
import ru.sber.transport.database.external_request.Tables;
import ru.sber.transport.database.external_request.enums.AssessmentType;
import ru.sber.transport.database.external_request.enums.OrderState;
import ru.sber.transport.database.external_request.enums.Tariff;
import ru.sber.transport.database.external_request.tables.records.AssessmentsRecord;
import ru.sber.transport.database.external_request.tables.records.EmployeeRecord;
import ru.sber.transport.database.external_request.tables.records.FraudRecord;
import ru.sber.transport.database.external_request.tables.records.OrganizationRecord;
import ru.sber.transport.database.external_request.tables.records.TripOrderHistoryRecord;
import ru.sber.transport.database.external_request.tables.records.TripOrderRecord;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request.external.model.Assessment;
import ru.sber.transport.request.external.model.Assessments;
import ru.sber.transport.request.external.model.BaseTripOrderData;
import ru.sber.transport.request.external.model.EditTripOrderData;
import ru.sber.transport.request.external.model.OrderData;
import ru.sber.transport.request.external.model.PriceData;
import ru.sber.transport.request.external.model.RequestFilter;
import ru.sber.transport.request.external.model.State;
import ru.sber.transport.request.external.model.WaypointData;
import ru.sber.transport.request.external.model.triporder.TripOrderCreateDTO;
import ru.sber.transport.request.external.model.waypoint.WaypointDTO;
import ru.sber.transport.request.external.providers.employees.EmployeeListener;
import ru.sber.transport.request.external.providers.exceptions.DatabaseLayerException;
import ru.sber.transport.request.external.providers.model.TestEmployee;
import ru.sber.transport.request.external.providers.model.TestOrganization;
import ru.sber.transport.request.external.providers.order.history.TripOrderHistoriesDataProviderImpl;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static ru.sber.transport.database.external_request.Tables.ASSESSMENTS;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка провайдера заявок")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JooqDatabaseConfig.class, TripOrdersMetaDataProviderImpl.class, EmployeeListener.class,
    TripOrderListener.class, TripOrderHistoriesDataProviderImpl.class})
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
@MockitoBean(types = CurrentUser.class)
class TripOrdersDataProviderImplTest {

    @MockitoBean
    private OrganizationsProvider organizationsProvider;

    @MockitoBean
    private EmployeesProvider employeesProvider;

    @Autowired
    private DSLContext context;

    @Autowired
    private TripOrdersProvider tripOrdersProvider;

    @Autowired
    private TripOrdersMetaProvider tripOrdersMetaProvider;

    private OrganizationRecord organization;

    @BeforeEach
    void beforeEach() {
        organization = context.insertInto(Tables.ORGANIZATION)
            .set(Tables.ORGANIZATION.ID, UUID.randomUUID())
            .set(Tables.ORGANIZATION.DIGIT_ID, 1)
            .returning().fetchSingle();
    }

    @Test
    @DisplayName("Проверка cоздания заявки")
    void test_create() {
        final var source = Instancio.of(TripOrderCreateDTO.class)
            .set(Select.field(TripOrderCreateDTO::waypoints), Instancio.ofList(WaypointDTO.class)
                .set(Select.field(WaypointDTO::latitude), new BigDecimal("360.00000"))
                .set(Select.field(WaypointDTO::longitude), new BigDecimal("0.123456789")).create())
            .set(Select.field(TripOrderCreateDTO::date),
                OffsetDateTime.now().plusSeconds(Instancio.create(Integer.class)))
            .set(Select.field(TripOrderCreateDTO::tariff),
                Instancio.create(ru.sber.transport.request.external.model.Tariff.class))
            .create();

        final var price = Instancio.create(TestPriceData.class);
        final var testOrganization = Instancio.create(TestOrganization.class);
        final var passenger = Instancio.create(TestEmployee.class);
        final var timeZone = "+03:00";

        when(organizationsProvider.get(passenger.getOrganizationId())).thenReturn(testOrganization);
        when(employeesProvider.get(passenger.getId())).thenReturn(passenger);
        when(employeesProvider.get(passenger.getId())).thenReturn(passenger);

        final var actual = tripOrdersProvider.create(passenger.getId(), source, price, timeZone);

        assertSoftly(it -> {
            it.assertThat(actual).isNotNull();
            it.assertThat(actual.getId()).isNotNull();
            it.assertThat(actual.getHumanReadableId())
                .isEqualTo("YA-%04d-00000001".formatted(testOrganization.getDigitId()));
            it.assertThat(actual.getWaypoints()).hasSameSizeAs(source.waypoints());
            it.assertThat(actual.getDate()).isEqualTo(source.date());
            it.assertThat(actual.getStatus()).isEqualTo(State.NEW);
            it.assertThat(actual.getPassenger().getId()).isEqualTo(passenger.getId());
            it.assertThat(actual.getPlanned().getCost()).isEqualTo(price.price());
            it.assertThat(actual.getPlanned().getDuration()).isEqualTo(price.duration());
            it.assertThat(actual.getActual().getCost()).isNull();
            it.assertThat(actual.getPurposeId()).isEqualTo(source.purposeId());
            it.assertThat(actual.getComment()).isEqualTo(source.comment());
            it.assertThat(actual.getTariff()).isEqualTo(source.tariff());
        });

        for (var i = 0; i < actual.getWaypoints().size(); i++) {
            final var waypoint = actual.getWaypoints().get(i);
            final var sourceWaypoint = source.waypoints().get(i);
            assertSoftly(it -> {
                it.assertThat(waypoint.getId()).isNotNull();
                it.assertThat(waypoint.getCountry()).isEqualTo(sourceWaypoint.country());
                it.assertThat(waypoint.getRegion()).isEqualTo(sourceWaypoint.region());
                it.assertThat(waypoint.getCity()).isEqualTo(sourceWaypoint.city());
                it.assertThat(waypoint.getStreet()).isEqualTo(sourceWaypoint.street());
                it.assertThat(waypoint.getHouse()).isEqualTo(sourceWaypoint.house());
                it.assertThat(waypoint.getBuilding()).isEqualTo(sourceWaypoint.building());
                it.assertThat(waypoint.getStructure()).isEqualTo(sourceWaypoint.structure());
                it.assertThat(waypoint.getLatitude().stripTrailingZeros())
                    .isEqualTo(sourceWaypoint.latitude().stripTrailingZeros());
                it.assertThat(waypoint.getLongitude().stripTrailingZeros())
                    .isEqualTo(sourceWaypoint.longitude().stripTrailingZeros());
            });
        }

        assertThat(context.fetchCount(Tables.TRIP_ORDER)).isEqualTo(1);
        assertThat(context.fetchCount(Tables.WAYPOINT)).isEqualTo(source.waypoints().size());
        assertThat(context.fetchCount(Tables.ORDERED_REQUEST_WAYPOINT)).isEqualTo(source.waypoints().size());
    }

    @Test
    @DisplayName("Проверка получения заявок")
    void test_get_all() {
        final var source = new ArrayList<TripOrderRecord>();
        final var sourceAssessments = new ArrayList<AssessmentsRecord>();
        final var sourceFrauds = new ArrayList<FraudRecord>();
        for (int i = 0; i < 100; i++) {
            final var passenger = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .set(Tables.EMPLOYEE.COST_CENTER, "1111L22222")
                .returning().fetchSingle();
            final var item = context.insertInto(Tables.TRIP_ORDER)
                .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PASSENGER_ID, passenger.getId())
                .set(Tables.TRIP_ORDER.DIGIT_ID, i + 1)
                .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now().plusDays(i))
                .set(Tables.TRIP_ORDER.STATUS, OrderState.values()[i % OrderState.values().length])
                .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.PLANNED_DURATION, DayToSecond.valueOf(Duration.ZERO.plusMinutes(i)))
                .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.COMMENT, "comment %03d".formatted(i))
                .set(Tables.TRIP_ORDER.REASON, "reason %03d".formatted(i))
                .set(Tables.TRIP_ORDER.RECEIPT, "receipt %03d".formatted(i))
                .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
                .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) i)
                .returning()
                .fetchSingle();
            final var assessment = context.insertInto(ASSESSMENTS)
                .set(ASSESSMENTS.ID, UUID.randomUUID())
                .set(ASSESSMENTS.ORDER_ID, item.getId())
                .set(ASSESSMENTS.EMPLOYEE_ID, passenger.getId())
                .set(ASSESSMENTS.TYPE, AssessmentType.SERVICE)
                .set(ASSESSMENTS.COMMENT, Instancio.create(String.class))
                .set(ASSESSMENTS.RATING, (short) Instancio.create(Byte.class))
                .set(ASSESSMENTS.CREATED_AT, OffsetDateTime.now())
                .returning().fetchSingle();
            sourceAssessments.add(assessment);
            final var fraud = context.insertInto(Tables.FRAUD)
                    .set(Tables.FRAUD.ID, item.getId())
                    .set(Tables.FRAUD.COMMENT, "Comment")
                    .set(Tables.FRAUD.TYPE, "RECEIPT")
                    .returning().fetchSingle();
            sourceFrauds.add(fraud);
            final var first = context.insertInto(Tables.WAYPOINT)
                .set(Tables.WAYPOINT.ID, UUID.randomUUID())
                .set(Tables.WAYPOINT.LATITUDE, BigDecimal.valueOf(0))
                .set(Tables.WAYPOINT.LONGITUDE, BigDecimal.valueOf(1))
                .set(Tables.WAYPOINT.COUNTRY, Instancio.create(String.class))
                .set(Tables.WAYPOINT.REGION, Instancio.create(String.class))
                .returning()
                .fetchSingle();
            final var second = context.insertInto(Tables.WAYPOINT)
                .set(Tables.WAYPOINT.ID, UUID.randomUUID())
                .set(Tables.WAYPOINT.LATITUDE, BigDecimal.valueOf(2))
                .set(Tables.WAYPOINT.LONGITUDE, BigDecimal.valueOf(3))
                .set(Tables.WAYPOINT.COUNTRY, Instancio.create(String.class))
                .set(Tables.WAYPOINT.REGION, Instancio.create(String.class))
                .returning()
                .fetchSingle();
            context.insertInto(Tables.ORDERED_REQUEST_WAYPOINT)
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_ID, item.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.WAYPOINT_ID, first.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_NUMBER, 0)
                .execute();
            context.insertInto(Tables.ORDERED_REQUEST_WAYPOINT)
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_ID, item.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.WAYPOINT_ID, second.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_NUMBER, 1)
                .execute();
            source.add(item);
        }

        final var actualList = tripOrdersProvider.get(new TestFilter(), 0, 20, "humanReadableId", true);
        final var expectedList = source.stream().skip(0).limit(20).toList();

        assertThat(actualList).isNotNull();
        assertThat(actualList.content()).hasSameSizeAs(expectedList);

        for (var i = 0; i < actualList.content().size(); i++) {
            final var actual = actualList.content().get(i);
            final var expected = expectedList.get(i);
            final var expectedAssessment = sourceAssessments.get(i);

            assertSoftly(it -> {
                it.assertThat(actual.getId()).isEqualTo(expected.getId());
                it.assertThat(actual.getHumanReadableId()).isEqualTo("YA-0001-%08d".formatted(expected.getDigitId()));
                it.assertThat(actual.getWaypoints()).isNotEmpty();
                it.assertThat(actual.getDate()).isEqualTo(expected.getDate());
                it.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
                it.assertThat(actual.getPassenger().getId()).isEqualTo(expected.getPassengerId());
                it.assertThat(actual.getPlanned().getCost()).isEqualTo(expected.getPlannedCost());
                it.assertThat(actual.getPlanned().getDuration()).isEqualTo(expected.getPlannedDuration().toDuration());
                it.assertThat(actual.getActual().getCost()).isEqualTo(expected.getActualCost());
                it.assertThat(actual.getPurposeId()).isEqualTo(expected.getPurposeId());
                it.assertThat(actual.getComment()).isEqualTo(expected.getComment());
                it.assertThat(actual.getReceipt()).isEqualTo(expected.getReceipt());
                it.assertThat((short) actual.getAssessments().getService().getRating())
                    .isEqualTo(expectedAssessment.getRating());
                it.assertThat(actual.getAssessments().getService().getComment())
                    .isEqualTo(expectedAssessment.getComment());
                it.assertThat(actual.getCostCenter()).isEqualTo("1111L22222");
                it.assertThat(actual.getFraud().getComment()).isEqualTo("Comment");
            });
        }
    }

    @Test
    @DisplayName("Проверка получения количества заявок")
    void test_get_count() {
        final var source = new ArrayList<TripOrderRecord>();
        for (int i = 0; i < 100; i++) {
            final var passenger = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .returning().fetchSingle();
            final var item = context.insertInto(Tables.TRIP_ORDER)
                .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PASSENGER_ID, passenger.getId())
                .set(Tables.TRIP_ORDER.DIGIT_ID, i + 1)
                .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now().plusDays(i))
                .set(Tables.TRIP_ORDER.STATUS, OrderState.values()[i % OrderState.values().length])
                .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.PLANNED_DURATION, DayToSecond.valueOf(Duration.ZERO.plusMinutes(i)))
                .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.COMMENT, "comment %03d".formatted(i))
                .set(Tables.TRIP_ORDER.REASON, "reason %03d".formatted(i))
                .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
                .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) i)
                .returning()
                .fetchSingle();
            final var first = context.insertInto(Tables.WAYPOINT)
                .set(Tables.WAYPOINT.ID, UUID.randomUUID())
                .set(Tables.WAYPOINT.LATITUDE, BigDecimal.valueOf(0))
                .set(Tables.WAYPOINT.LONGITUDE, BigDecimal.valueOf(1))
                .set(Tables.WAYPOINT.COUNTRY, Instancio.create(String.class))
                .set(Tables.WAYPOINT.REGION, Instancio.create(String.class))
                .returning()
                .fetchSingle();
            final var second = context.insertInto(Tables.WAYPOINT)
                .set(Tables.WAYPOINT.ID, UUID.randomUUID())
                .set(Tables.WAYPOINT.LATITUDE, BigDecimal.valueOf(2))
                .set(Tables.WAYPOINT.LONGITUDE, BigDecimal.valueOf(3))
                .set(Tables.WAYPOINT.COUNTRY, Instancio.create(String.class))
                .set(Tables.WAYPOINT.REGION, Instancio.create(String.class))
                .returning()
                .fetchSingle();
            context.insertInto(Tables.ORDERED_REQUEST_WAYPOINT)
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_ID, item.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.WAYPOINT_ID, first.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_NUMBER, 0)
                .execute();
            context.insertInto(Tables.ORDERED_REQUEST_WAYPOINT)
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_ID, item.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.WAYPOINT_ID, second.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_NUMBER, 1)
                .execute();
            source.add(item);
        }

        final var actualList = tripOrdersProvider.get(new TestFilter(), 0, 0, "humanReadableId", true);

        assertThat(actualList).isNotNull();
        assertThat(actualList.content()).isEmpty();
        assertThat(actualList.page().total()).isEqualTo(source.size());
        assertThat(actualList.page().count()).isEqualTo(source.size());
    }

    @Test
    @DisplayName("Проверка получения заявок по статусу")
    void test_get_byStates() {
        final var source = new ArrayList<TripOrderRecord>();
        for (int i = 0; i < 100; i++) {
            final var passenger = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .returning().fetchSingle();
            final var item = context.insertInto(Tables.TRIP_ORDER)
                .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PASSENGER_ID, passenger.getId())
                .set(Tables.TRIP_ORDER.DIGIT_ID, i + 1)
                .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now().plusDays(i))
                .set(Tables.TRIP_ORDER.STATUS, OrderState.values()[i % (OrderState.values().length - 4)])
                .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.PLANNED_DURATION, DayToSecond.valueOf(Duration.ZERO.plusMinutes(i)))
                .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.COMMENT, "comment %03d".formatted(i))
                .set(Tables.TRIP_ORDER.REASON, "reason %03d".formatted(i))
                .set(Tables.TRIP_ORDER.RECEIPT, "receipt %03d".formatted(i))
                .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
                .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) i)
                .returning()
                .fetchSingle();
            final var first = context.insertInto(Tables.WAYPOINT)
                .set(Tables.WAYPOINT.ID, UUID.randomUUID())
                .set(Tables.WAYPOINT.LATITUDE, BigDecimal.valueOf(0))
                .set(Tables.WAYPOINT.LONGITUDE, BigDecimal.valueOf(1))
                .set(Tables.WAYPOINT.COUNTRY, Instancio.create(String.class))
                .set(Tables.WAYPOINT.REGION, Instancio.create(String.class))
                .returning()
                .fetchSingle();
            final var second = context.insertInto(Tables.WAYPOINT)
                .set(Tables.WAYPOINT.ID, UUID.randomUUID())
                .set(Tables.WAYPOINT.LATITUDE, BigDecimal.valueOf(2))
                .set(Tables.WAYPOINT.LONGITUDE, BigDecimal.valueOf(3))
                .set(Tables.WAYPOINT.COUNTRY, Instancio.create(String.class))
                .set(Tables.WAYPOINT.REGION, Instancio.create(String.class))
                .returning()
                .fetchSingle();
            context.insertInto(Tables.ORDERED_REQUEST_WAYPOINT)
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_ID, item.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.WAYPOINT_ID, first.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_NUMBER, 0)
                .execute();
            context.insertInto(Tables.ORDERED_REQUEST_WAYPOINT)
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_ID, item.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.WAYPOINT_ID, second.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_NUMBER, 1)
                .execute();
            if (item.getStatus() == OrderState.NEW || item.getStatus() == OrderState.CANCELLED) {
                source.add(item);
            }
        }

        final var actualList = tripOrdersProvider.get(new TestFilter(State.NEW, State.CANCELLED), 0, 20,
            "humanReadableId", true);
        final var expectedList = source.stream().skip(0).limit(20).toList();

        assertThat(actualList).isNotNull();
        assertThat(actualList.content()).hasSameSizeAs(expectedList);

        for (var i = 0; i < actualList.content().size(); i++) {
            final var actual = actualList.content().get(i);
            final var expected = expectedList.get(i);

            assertSoftly(it -> {
                it.assertThat(actual.getId()).isEqualTo(expected.getId());
                it.assertThat(actual.getHumanReadableId()).isEqualTo("YA-0001-%08d".formatted(expected.getDigitId()));
                it.assertThat(actual.getWaypoints()).isNotEmpty();
                it.assertThat(actual.getDate()).isEqualTo(expected.getDate());
                it.assertThat(actual.getStatus()).isEqualTo(State.values()[expected.getStatus().ordinal()]);
                it.assertThat(actual.getPassenger().getId()).isEqualTo(expected.getPassengerId());
                it.assertThat(actual.getPlanned().getCost()).isEqualTo(expected.getPlannedCost());
                it.assertThat(actual.getPlanned().getDuration()).isEqualTo(expected.getPlannedDuration().toDuration());
                it.assertThat(actual.getActual().getCost()).isEqualTo(expected.getActualCost());
                it.assertThat(actual.getPurposeId()).isEqualTo(expected.getPurposeId());
                it.assertThat(actual.getComment()).isEqualTo(expected.getComment());
                it.assertThat(actual.getReason()).isEqualTo(expected.getReason());
                it.assertThat(actual.getReceipt()).isEqualTo(expected.getReceipt());
                it.assertThat(actual.getTariff().name()).isEqualTo(expected.getTariff().name());
            });
        }
    }

    @Test
    @DisplayName("Проверка получения заявок по пассажиру")
    void test_get_byPassengerId() {
        final var passengers = new ArrayList<EmployeeRecord>();
        for (var i = 0; i < 10; i++) {
            final var passenger = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .returning().fetchSingle();
            passengers.add(passenger);
        }
        final var source = new ArrayList<TripOrderRecord>();
        for (int i = 0; i < 100; i++) {
            final var currentPassenger = passengers.get(i % passengers.size());
            final var item = context.insertInto(Tables.TRIP_ORDER)
                .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PASSENGER_ID, currentPassenger.getId())
                .set(Tables.TRIP_ORDER.DIGIT_ID, i + 1)
                .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now().plusDays(i))
                .set(Tables.TRIP_ORDER.STATUS, OrderState.values()[i % OrderState.values().length])
                .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.PLANNED_DURATION, DayToSecond.valueOf(Duration.ZERO.plusMinutes(i)))
                .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.COMMENT, "comment %03d".formatted(i))
                .set(Tables.TRIP_ORDER.REASON, "reason %03d".formatted(i))
                .set(Tables.TRIP_ORDER.RECEIPT, "receipt %03d".formatted(i))
                .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
                .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) i)
                .returning()
                .fetchSingle();
            final var first = context.insertInto(Tables.WAYPOINT)
                .set(Tables.WAYPOINT.ID, UUID.randomUUID())
                .set(Tables.WAYPOINT.LATITUDE, BigDecimal.valueOf(0))
                .set(Tables.WAYPOINT.LONGITUDE, BigDecimal.valueOf(1))
                .set(Tables.WAYPOINT.COUNTRY, Instancio.create(String.class))
                .set(Tables.WAYPOINT.REGION, Instancio.create(String.class))
                .returning()
                .fetchSingle();
            final var second = context.insertInto(Tables.WAYPOINT)
                .set(Tables.WAYPOINT.ID, UUID.randomUUID())
                .set(Tables.WAYPOINT.LATITUDE, BigDecimal.valueOf(2))
                .set(Tables.WAYPOINT.LONGITUDE, BigDecimal.valueOf(3))
                .set(Tables.WAYPOINT.COUNTRY, Instancio.create(String.class))
                .set(Tables.WAYPOINT.REGION, Instancio.create(String.class))
                .returning()
                .fetchSingle();
            context.insertInto(Tables.ORDERED_REQUEST_WAYPOINT)
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_ID, item.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.WAYPOINT_ID, first.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_NUMBER, 0)
                .execute();
            context.insertInto(Tables.ORDERED_REQUEST_WAYPOINT)
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_ID, item.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.WAYPOINT_ID, second.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_NUMBER, 1)
                .execute();
            if (passengers.stream().limit(5).toList().contains(currentPassenger)) {
                source.add(item);
            }
        }

        final var actualList = tripOrdersProvider.get(
            new TestFilter(passengers.stream().map(EmployeeRecord::getId).limit(5).toList(), null), 0, 20,
            "humanReadableId", true);
        final var expectedList = source.stream().skip(0).limit(20).toList();

        assertThat(actualList).isNotNull();
        assertThat(actualList.content()).hasSameSizeAs(expectedList);

        for (var i = 0; i < actualList.content().size(); i++) {
            final var actual = actualList.content().get(i);
            final var expected = expectedList.get(i);

            assertSoftly(it -> {
                it.assertThat(actual.getId()).isEqualTo(expected.getId());
                it.assertThat(actual.getHumanReadableId()).isEqualTo("YA-0001-%08d".formatted(expected.getDigitId()));
                it.assertThat(actual.getWaypoints()).isNotEmpty();
                it.assertThat(actual.getDate()).isEqualTo(expected.getDate());
                it.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
                it.assertThat(actual.getPassenger().getId()).isEqualTo(expected.getPassengerId());
                it.assertThat(actual.getPlanned().getCost()).isEqualTo(expected.getPlannedCost());
                it.assertThat(actual.getPlanned().getDuration()).isEqualTo(expected.getPlannedDuration().toDuration());
                it.assertThat(actual.getActual().getCost()).isEqualTo(expected.getActualCost());
                it.assertThat(actual.getPurposeId()).isEqualTo(expected.getPurposeId());
                it.assertThat(actual.getComment()).isEqualTo(expected.getComment());
                it.assertThat(actual.getReceipt()).isEqualTo(expected.getReceipt());
            });
        }
    }

    @Test
    @DisplayName("Проверка получения заявок по согласующему")
    void test_get_byApproverId() {
        final var approverId = Instancio.ofList(UUID.class)
            .size(10)
            .create();
        final var approvers = new HashMap<UUID, EmployeeRecord>();
        final var source = new ArrayList<TripOrderRecord>();
        for (int i = 0; i < 100; i++) {
            final var id = approverId.get(i % approverId.size());
            final var currentApprover = approvers.getOrDefault(id, context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, id)
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .set(Tables.EMPLOYEE.LAST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.FIRST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.PATRONYMIC, Instancio.create(String.class))
                .onConflict().doNothing().returning().fetchOne());
            approvers.put(id, currentApprover);
            final var department = context.insertInto(Tables.DEPARTMENT)
                .set(Tables.DEPARTMENT.ID, UUID.randomUUID())
                .set(Tables.DEPARTMENT.HEAD_ID, currentApprover.getId())
                .returning().fetchSingle();
            final var passenger = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, department.getId())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .set(Tables.EMPLOYEE.LAST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.FIRST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.PATRONYMIC, Instancio.create(String.class))
                .returning().fetchSingle();
            final var item = context.insertInto(Tables.TRIP_ORDER)
                .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PASSENGER_ID, passenger.getId())
                .set(Tables.TRIP_ORDER.DIGIT_ID, i + 1)
                .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now().plusDays(i))
                .set(Tables.TRIP_ORDER.STATUS, OrderState.values()[i % OrderState.values().length])
                .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) i)
                .set(Tables.TRIP_ORDER.PLANNED_DURATION, DayToSecond.valueOf(Duration.ZERO.plusMinutes(i)))
                .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.COMMENT, "comment %03d".formatted(i))
                .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
                .set(Tables.TRIP_ORDER.REASON, "reason %03d".formatted(i))
                .set(Tables.TRIP_ORDER.RECEIPT, "receipt %03d".formatted(i))
                .set(Tables.TRIP_ORDER.APPROVER_ID, currentApprover.getId())
                .returning()
                .fetchSingle();
            final var first = context.insertInto(Tables.WAYPOINT)
                .set(Tables.WAYPOINT.ID, UUID.randomUUID())
                .set(Tables.WAYPOINT.LATITUDE, BigDecimal.valueOf(0))
                .set(Tables.WAYPOINT.LONGITUDE, BigDecimal.valueOf(1))
                .set(Tables.WAYPOINT.COUNTRY, Instancio.create(String.class))
                .set(Tables.WAYPOINT.REGION, Instancio.create(String.class))
                .returning()
                .fetchSingle();
            final var second = context.insertInto(Tables.WAYPOINT)
                .set(Tables.WAYPOINT.ID, UUID.randomUUID())
                .set(Tables.WAYPOINT.LATITUDE, BigDecimal.valueOf(2))
                .set(Tables.WAYPOINT.LONGITUDE, BigDecimal.valueOf(3))
                .set(Tables.WAYPOINT.COUNTRY, Instancio.create(String.class))
                .set(Tables.WAYPOINT.REGION, Instancio.create(String.class))
                .returning()
                .fetchSingle();
            context.insertInto(Tables.ORDERED_REQUEST_WAYPOINT)
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_ID, item.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.WAYPOINT_ID, first.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_NUMBER, 0)
                .execute();
            context.insertInto(Tables.ORDERED_REQUEST_WAYPOINT)
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_ID, item.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.WAYPOINT_ID, second.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_NUMBER, 1)
                .execute();
            if (approverId.stream().limit(5).toList().contains(currentApprover.getId())) {
                source.add(item);
            }
        }

        final var actualList = tripOrdersProvider.get(new TestFilter(null, approverId.stream().limit(5).toList()), 0,
            20, "humanReadableId", true);
        final var expectedList = source.stream().skip(0).limit(20).toList();

        assertThat(actualList).isNotNull();
        assertThat(actualList.content()).hasSameSizeAs(expectedList);

        for (var i = 0; i < actualList.content().size(); i++) {
            final var actual = actualList.content().get(i);
            final var expected = expectedList.get(i);

            assertSoftly(it -> {
                it.assertThat(actual.getId()).isEqualTo(expected.getId());
                it.assertThat(actual.getHumanReadableId()).isEqualTo("YA-0001-%08d".formatted(expected.getDigitId()));
                it.assertThat(actual.getWaypoints()).isNotEmpty();
                it.assertThat(actual.getDate()).isEqualTo(expected.getDate());
                it.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
                it.assertThat(actual.getPassenger().getId()).isEqualTo(expected.getPassengerId());
                it.assertThat(actual.getPlanned().getCost()).isEqualTo(expected.getPlannedCost());
                it.assertThat(actual.getPlanned().getDuration()).isEqualTo(expected.getPlannedDuration().toDuration());
                it.assertThat(actual.getActual().getCost()).isEqualTo(expected.getActualCost());
                it.assertThat(actual.getPurposeId()).isEqualTo(expected.getPurposeId());
                it.assertThat(actual.getComment()).isEqualTo(expected.getComment());
                it.assertThat(actual.getReceipt()).isEqualTo(expected.getReceipt());
                it.assertThat(actual.getTariff().name()).isEqualTo(expected.getTariff().name());
            });
        }
    }

    @Test
    @DisplayName("Проверка получения заявок по дате от")
    void test_get_byDateFrom() {
        final var source = new ArrayList<TripOrderRecord>();
        for (int i = 0; i < 100; i++) {
            final var passenger = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .set(Tables.EMPLOYEE.LAST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.FIRST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.PATRONYMIC, Instancio.create(String.class))
                .returning().fetchSingle();
            final var item = context.insertInto(Tables.TRIP_ORDER)
                .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PASSENGER_ID, passenger.getId())
                .set(Tables.TRIP_ORDER.DIGIT_ID, i + 1)
                .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now().plusDays(i))
                .set(Tables.TRIP_ORDER.STATUS, OrderState.values()[i % OrderState.values().length])
                .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.PLANNED_DURATION, DayToSecond.valueOf(Duration.ZERO.plusMinutes(i)))
                .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.COMMENT, "comment %03d".formatted(i))
                .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
                .set(Tables.TRIP_ORDER.REASON, "reason %03d".formatted(i))
                .set(Tables.TRIP_ORDER.RECEIPT, "receipt %03d".formatted(i))
                .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) i)
                .returning()
                .fetchSingle();
            final var first = context.insertInto(Tables.WAYPOINT)
                .set(Tables.WAYPOINT.ID, UUID.randomUUID())
                .set(Tables.WAYPOINT.LATITUDE, BigDecimal.valueOf(0))
                .set(Tables.WAYPOINT.LONGITUDE, BigDecimal.valueOf(1))
                .set(Tables.WAYPOINT.COUNTRY, Instancio.create(String.class))
                .set(Tables.WAYPOINT.REGION, Instancio.create(String.class))
                .returning()
                .fetchSingle();
            final var second = context.insertInto(Tables.WAYPOINT)
                .set(Tables.WAYPOINT.ID, UUID.randomUUID())
                .set(Tables.WAYPOINT.LATITUDE, BigDecimal.valueOf(2))
                .set(Tables.WAYPOINT.LONGITUDE, BigDecimal.valueOf(3))
                .set(Tables.WAYPOINT.COUNTRY, Instancio.create(String.class))
                .set(Tables.WAYPOINT.REGION, Instancio.create(String.class))
                .returning()
                .fetchSingle();
            context.insertInto(Tables.ORDERED_REQUEST_WAYPOINT)
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_ID, item.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.WAYPOINT_ID, first.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_NUMBER, 0)
                .execute();
            context.insertInto(Tables.ORDERED_REQUEST_WAYPOINT)
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_ID, item.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.WAYPOINT_ID, second.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_NUMBER, 1)
                .execute();
            source.add(item);
        }

        final var actualList = tripOrdersProvider.get(new TestFilter(OffsetDateTime.now().plusDays(30), null), 0, 20,
            "humanReadableId", true);
        final var expectedList = source.stream().skip(31).limit(20).toList();

        assertThat(actualList).isNotNull();
        assertThat(actualList.content()).hasSameSizeAs(expectedList);

        for (var i = 0; i < actualList.content().size(); i++) {
            final var actual = actualList.content().get(i);
            final var expected = expectedList.get(i);

            assertSoftly(it -> {
                it.assertThat(actual.getId()).isEqualTo(expected.getId());
                it.assertThat(actual.getHumanReadableId()).isEqualTo("YA-0001-%08d".formatted(expected.getDigitId()));
                it.assertThat(actual.getWaypoints()).isNotEmpty();
                it.assertThat(actual.getDate()).isEqualTo(expected.getDate());
                it.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
                it.assertThat(actual.getPassenger().getId()).isEqualTo(expected.getPassengerId());
                it.assertThat(actual.getPlanned().getCost()).isEqualTo(expected.getPlannedCost());
                it.assertThat(actual.getPlanned().getDuration()).isEqualTo(expected.getPlannedDuration().toDuration());
                it.assertThat(actual.getActual().getCost()).isEqualTo(expected.getActualCost());
                it.assertThat(actual.getPurposeId()).isEqualTo(expected.getPurposeId());
                it.assertThat(actual.getComment()).isEqualTo(expected.getComment());
                it.assertThat(actual.getReceipt()).isEqualTo(expected.getReceipt());
                it.assertThat(actual.getTariff().name()).isEqualTo(expected.getTariff().name());
            });
        }
    }

    @Test
    @DisplayName("Проверка получения заявок по дате до. Реестр")
    void test_get_byDateTo_registry() {
        final var source = new ArrayList<TripOrderRecord>();
        final var approvers = new HashMap<UUID, EmployeeRecord>();
        final var employees = new HashMap<UUID, EmployeeRecord>();
        for (int i = 0; i < 100; i++) {
            final var head = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.LAST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.FIRST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.PATRONYMIC, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, UUID.randomUUID())
                .returning().fetchSingle();
            final var department = context.insertInto(Tables.DEPARTMENT)
                .set(Tables.DEPARTMENT.ID, UUID.randomUUID())
                .set(Tables.DEPARTMENT.HEAD_ID, head.getId())
                .returning().fetchSingle();
            final var passenger = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, department.getId())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .set(Tables.EMPLOYEE.LAST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.FIRST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.PATRONYMIC, Instancio.create(String.class))
                .returning().fetchSingle();
            approvers.put(passenger.getId(), head);
            employees.put(passenger.getId(), passenger);
            final var item = context.insertInto(Tables.TRIP_ORDER)
                .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PASSENGER_ID, passenger.getId())
                .set(Tables.TRIP_ORDER.DIGIT_ID, i + 1)
                .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now().plusDays(i))
                .set(Tables.TRIP_ORDER.STATUS, OrderState.values()[i % OrderState.values().length])
                .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.PLANNED_DURATION, DayToSecond.valueOf(Duration.ZERO.plusMinutes(i)))
                .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.COMMENT, "comment %03d".formatted(i))
                .set(Tables.TRIP_ORDER.RECEIPT, "receipt %03d".formatted(i))
                .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
                .set(Tables.TRIP_ORDER.REASON, "reason %03d".formatted(i))
                .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) i)
                .set(Tables.TRIP_ORDER.APPROVER_ID, head.getId())
                .returning()
                .fetchSingle();
            final var first = context.insertInto(Tables.WAYPOINT)
                .set(Tables.WAYPOINT.ID, UUID.randomUUID())
                .set(Tables.WAYPOINT.LATITUDE, BigDecimal.valueOf(0))
                .set(Tables.WAYPOINT.LONGITUDE, BigDecimal.valueOf(1))
                .set(Tables.WAYPOINT.COUNTRY, Instancio.create(String.class))
                .set(Tables.WAYPOINT.REGION, Instancio.create(String.class))
                .returning()
                .fetchSingle();
            final var second = context.insertInto(Tables.WAYPOINT)
                .set(Tables.WAYPOINT.ID, UUID.randomUUID())
                .set(Tables.WAYPOINT.LATITUDE, BigDecimal.valueOf(2))
                .set(Tables.WAYPOINT.LONGITUDE, BigDecimal.valueOf(3))
                .set(Tables.WAYPOINT.COUNTRY, Instancio.create(String.class))
                .set(Tables.WAYPOINT.REGION, Instancio.create(String.class))
                .returning()
                .fetchSingle();
            context.insertInto(Tables.ORDERED_REQUEST_WAYPOINT)
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_ID, item.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.WAYPOINT_ID, first.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_NUMBER, 0)
                .execute();
            context.insertInto(Tables.ORDERED_REQUEST_WAYPOINT)
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_ID, item.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.WAYPOINT_ID, second.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_NUMBER, 1)
                .execute();
            source.add(item);
        }

        final var actualList = tripOrdersProvider.getRegistry(new TestFilter(null, OffsetDateTime.now().plusDays(9)), 0,
            20, "humanReadableId", true);
        final var expectedList = source.stream().skip(0).limit(10).toList();

        assertThat(actualList).isNotNull();
        assertThat(actualList.content()).hasSameSizeAs(expectedList);

        for (var i = 0; i < actualList.content().size(); i++) {
            final var actual = actualList.content().get(i);
            final var expected = expectedList.get(i);

            assertSoftly(it -> {
                it.assertThat(actual.getId()).isEqualTo(expected.getId());
                it.assertThat(actual.getHumanReadableId()).isEqualTo("YA-0001-%08d".formatted(expected.getDigitId()));
                it.assertThat(actual.getWaypoints()).isNotEmpty();
                it.assertThat(actual.getDate()).isEqualTo(expected.getDate());
                it.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
                it.assertThat(actual.getPassenger().getId()).isEqualTo(expected.getPassengerId());
                it.assertThat(actual.getPlanned().getCost()).isEqualTo(expected.getPlannedCost());
                it.assertThat(actual.getPlanned().getDuration()).isEqualTo(expected.getPlannedDuration().toDuration());
                it.assertThat(actual.getActual().getCost()).isEqualTo(expected.getActualCost());
                it.assertThat(actual.getPurposeId()).isEqualTo(expected.getPurposeId());
                it.assertThat(actual.getComment()).isEqualTo(expected.getComment());
                it.assertThat(actual.getReceipt()).isEqualTo(expected.getReceipt());
                it.assertThat(actual.getApprover().getId()).isEqualTo(approvers.get(expected.getPassengerId()).getId());
                it.assertThat(actual.getApprover().getLastName())
                    .isEqualTo(approvers.get(expected.getPassengerId()).getLastName());
                it.assertThat(actual.getApprover().getFirstName())
                    .isEqualTo(approvers.get(expected.getPassengerId()).getFirstName());
                it.assertThat(actual.getApprover().getPatronymic())
                    .isEqualTo(approvers.get(expected.getPassengerId()).getPatronymic());
                it.assertThat(actual.getPassenger().getId())
                    .isEqualTo(employees.get(expected.getPassengerId()).getId());
                it.assertThat(actual.getPassenger().getLastName())
                    .isEqualTo(employees.get(expected.getPassengerId()).getLastName());
                it.assertThat(actual.getPassenger().getFirstName())
                    .isEqualTo(employees.get(expected.getPassengerId()).getFirstName());
                it.assertThat(actual.getPassenger().getPatronymic())
                    .isEqualTo(employees.get(expected.getPassengerId()).getPatronymic());
                it.assertThat(actual.getTariff().name()).isEqualTo(expected.getTariff().name());
            });
        }
    }

    @Test
    @DisplayName("Проверка получения заявок по согласующему")
    void test_get_byApprover() {
        final var source = new ArrayList<TripOrderRecord>();
        final var approvers = new HashMap<UUID, EmployeeRecord>();
        final var employees = new HashMap<UUID, EmployeeRecord>();
        for (int i = 0; i < 100; i++) {
            final var head = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.LAST_NAME, "Согласовантов%03d".formatted(i))
                .set(Tables.EMPLOYEE.FIRST_NAME, "Согласовант%03d".formatted(i))
                .set(Tables.EMPLOYEE.PATRONYMIC, "Согласовантович%03d".formatted(i))
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, UUID.randomUUID())
                .returning().fetchSingle();
            final var department = context.insertInto(Tables.DEPARTMENT)
                .set(Tables.DEPARTMENT.ID, UUID.randomUUID())
                .set(Tables.DEPARTMENT.HEAD_ID, head.getId())
                .returning().fetchSingle();
            final var passenger = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, department.getId())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .set(Tables.EMPLOYEE.LAST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.FIRST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.PATRONYMIC, Instancio.create(String.class))
                .returning().fetchSingle();
            approvers.put(passenger.getId(), head);
            employees.put(passenger.getId(), passenger);
            final var item = context.insertInto(Tables.TRIP_ORDER)
                .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PASSENGER_ID, passenger.getId())
                .set(Tables.TRIP_ORDER.DIGIT_ID, i + 1)
                .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now().plusDays(i))
                .set(Tables.TRIP_ORDER.STATUS, OrderState.values()[i % OrderState.values().length])
                .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.PLANNED_DURATION, DayToSecond.valueOf(Duration.ZERO.plusMinutes(i)))
                .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.COMMENT, "comment %03d".formatted(i))
                .set(Tables.TRIP_ORDER.RECEIPT, "receipt %03d".formatted(i))
                .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
                .set(Tables.TRIP_ORDER.REASON, "reason %03d".formatted(i))
                .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) i)
                .set(Tables.TRIP_ORDER.APPROVER_ID, head.getId())
                .set(Tables.TRIP_ORDER.ECONOMY, BigDecimal.valueOf(561.60))
                .returning()
                .fetchSingle();
            final var first = context.insertInto(Tables.WAYPOINT)
                .set(Tables.WAYPOINT.ID, UUID.randomUUID())
                .set(Tables.WAYPOINT.LATITUDE, BigDecimal.valueOf(0))
                .set(Tables.WAYPOINT.LONGITUDE, BigDecimal.valueOf(1))
                .set(Tables.WAYPOINT.COUNTRY, Instancio.create(String.class))
                .set(Tables.WAYPOINT.REGION, Instancio.create(String.class))
                .returning()
                .fetchSingle();
            final var second = context.insertInto(Tables.WAYPOINT)
                .set(Tables.WAYPOINT.ID, UUID.randomUUID())
                .set(Tables.WAYPOINT.LATITUDE, BigDecimal.valueOf(2))
                .set(Tables.WAYPOINT.LONGITUDE, BigDecimal.valueOf(3))
                .set(Tables.WAYPOINT.COUNTRY, Instancio.create(String.class))
                .set(Tables.WAYPOINT.REGION, Instancio.create(String.class))
                .returning()
                .fetchSingle();
            context.insertInto(Tables.ORDERED_REQUEST_WAYPOINT)
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_ID, item.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.WAYPOINT_ID, first.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_NUMBER, 0)
                .execute();
            context.insertInto(Tables.ORDERED_REQUEST_WAYPOINT)
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_ID, item.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.WAYPOINT_ID, second.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_NUMBER, 1)
                .execute();
            source.add(item);
        }

        final var actualList = tripOrdersProvider.getRegistry(new TestFilter("Согласовант0", null), 0, 20,
            "humanReadableId", true);
        final var expectedList = source.stream().skip(0).limit(20).toList();

        assertThat(actualList).isNotNull();
        assertThat(actualList.content()).hasSameSizeAs(expectedList);

        for (var i = 0; i < actualList.content().size(); i++) {
            final var actual = actualList.content().get(i);
            final var expected = expectedList.get(i);

            assertSoftly(it -> {
                it.assertThat(actual.getId()).isEqualTo(expected.getId());
                it.assertThat(actual.getHumanReadableId()).isEqualTo("YA-0001-%08d".formatted(expected.getDigitId()));
                it.assertThat(actual.getWaypoints()).isNotEmpty();
                it.assertThat(actual.getDate()).isEqualTo(expected.getDate());
                it.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
                it.assertThat(actual.getPassenger().getId()).isEqualTo(expected.getPassengerId());
                it.assertThat(actual.getPlanned().getCost()).isEqualTo(expected.getPlannedCost());
                it.assertThat(actual.getPlanned().getDuration()).isEqualTo(expected.getPlannedDuration().toDuration());
                it.assertThat(actual.getActual().getCost()).isEqualTo(expected.getActualCost());
                it.assertThat(actual.getPurposeId()).isEqualTo(expected.getPurposeId());
                it.assertThat(actual.getComment()).isEqualTo(expected.getComment());
                it.assertThat(actual.getReceipt()).isEqualTo(expected.getReceipt());
                it.assertThat(actual.getApprover().getId()).isEqualTo(approvers.get(expected.getPassengerId()).getId());
                it.assertThat(actual.getApprover().getLastName())
                    .isEqualTo(approvers.get(expected.getPassengerId()).getLastName());
                it.assertThat(actual.getApprover().getFirstName())
                    .isEqualTo(approvers.get(expected.getPassengerId()).getFirstName());
                it.assertThat(actual.getApprover().getPatronymic())
                    .isEqualTo(approvers.get(expected.getPassengerId()).getPatronymic());
                it.assertThat(actual.getPassenger().getId())
                    .isEqualTo(employees.get(expected.getPassengerId()).getId());
                it.assertThat(actual.getPassenger().getLastName())
                    .isEqualTo(employees.get(expected.getPassengerId()).getLastName());
                it.assertThat(actual.getPassenger().getFirstName())
                    .isEqualTo(employees.get(expected.getPassengerId()).getFirstName());
                it.assertThat(actual.getPassenger().getPatronymic())
                    .isEqualTo(employees.get(expected.getPassengerId()).getPatronymic());
                it.assertThat(actual.getTariff().name()).isEqualTo(expected.getTariff().name());
            });
        }
    }

    @Test
    @DisplayName("Проверка получения заявок по дате от - до. Полный набор данных")
    void test_get_byDateFrom_To_full() {
        final var source = new ArrayList<TripOrderRecord>();
        final var passengers = new HashMap<UUID, EmployeeRecord>();
        for (int i = 0; i < 100; i++) {
            final var passenger = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .set(Tables.EMPLOYEE.LAST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.FIRST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.PATRONYMIC, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, UUID.randomUUID())
                .returning().fetchSingle();
            passengers.put(passenger.getId(), passenger);
            final var item = context.insertInto(Tables.TRIP_ORDER)
                .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PASSENGER_ID, passenger.getId())
                .set(Tables.TRIP_ORDER.DIGIT_ID, i + 1)
                .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now().plusDays(i))
                .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.PLANNED_DURATION, DayToSecond.valueOf(Duration.ZERO.plusMinutes(i)))
                .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.COMMENT, "comment %03d".formatted(i))
                .set(Tables.TRIP_ORDER.RECEIPT, "receipt %03d".formatted(i))
                .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
                .set(Tables.TRIP_ORDER.REASON, "reason %03d".formatted(i))
                .set(Tables.TRIP_ORDER.STATUS, OrderState.values()[i % OrderState.values().length])
                .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) i)
                .returning()
                .fetchSingle();
            final var first = context.insertInto(Tables.WAYPOINT)
                .set(Tables.WAYPOINT.ID, UUID.randomUUID())
                .set(Tables.WAYPOINT.LATITUDE, BigDecimal.valueOf(0))
                .set(Tables.WAYPOINT.LONGITUDE, BigDecimal.valueOf(1))
                .set(Tables.WAYPOINT.COUNTRY, Instancio.create(String.class))
                .set(Tables.WAYPOINT.REGION, Instancio.create(String.class))
                .returning()
                .fetchSingle();
            final var second = context.insertInto(Tables.WAYPOINT)
                .set(Tables.WAYPOINT.ID, UUID.randomUUID())
                .set(Tables.WAYPOINT.LATITUDE, BigDecimal.valueOf(2))
                .set(Tables.WAYPOINT.LONGITUDE, BigDecimal.valueOf(3))
                .set(Tables.WAYPOINT.COUNTRY, Instancio.create(String.class))
                .set(Tables.WAYPOINT.REGION, Instancio.create(String.class))
                .returning()
                .fetchSingle();
            context.insertInto(Tables.ORDERED_REQUEST_WAYPOINT)
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_ID, item.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.WAYPOINT_ID, first.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_NUMBER, 0)
                .execute();
            context.insertInto(Tables.ORDERED_REQUEST_WAYPOINT)
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_ID, item.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.WAYPOINT_ID, second.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_NUMBER, 1)
                .execute();
            source.add(item);
        }

        final var actualList = tripOrdersProvider.getFull(
            new TestFilter(OffsetDateTime.now().plusDays(10), OffsetDateTime.now().plusDays(15)), 0, 20,
            "humanReadableId", true);
        final var expectedList = source.stream().skip(11).limit(5).toList();

        assertThat(actualList).isNotNull();
        assertThat(actualList.content()).hasSameSizeAs(expectedList);

        for (var i = 0; i < actualList.content().size(); i++) {
            final var actual = actualList.content().get(i);
            final var expected = expectedList.get(i);

            assertSoftly(it -> {
                it.assertThat(actual.getId()).isEqualTo(expected.getId());
                it.assertThat(actual.getHumanReadableId()).isEqualTo("YA-0001-%08d".formatted(expected.getDigitId()));
                it.assertThat(actual.getWaypoints()).isNotEmpty();
                it.assertThat(actual.getDate()).isEqualTo(expected.getDate());
                it.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
                it.assertThat(actual.getPassenger().getId()).isEqualTo(expected.getPassengerId());
                it.assertThat(actual.getPassenger().getLastName())
                    .isEqualTo(passengers.get(expected.getPassengerId()).getLastName());
                it.assertThat(actual.getPassenger().getFirstName())
                    .isEqualTo(passengers.get(expected.getPassengerId()).getFirstName());
                it.assertThat(actual.getPassenger().getPatronymic())
                    .isEqualTo(passengers.get(expected.getPassengerId()).getPatronymic());
                it.assertThat(actual.getPlanned().getCost()).isEqualTo(expected.getPlannedCost());
                it.assertThat(actual.getPlanned().getDuration()).isEqualTo(expected.getPlannedDuration().toDuration());
                it.assertThat(actual.getActual().getCost()).isEqualTo(expected.getActualCost());
                it.assertThat(actual.getPurposeId()).isEqualTo(expected.getPurposeId());
                it.assertThat(actual.getComment()).isEqualTo(expected.getComment());
                it.assertThat(actual.getReceipt()).isEqualTo(expected.getReceipt());
                it.assertThat(actual.getTariff().name()).isEqualTo(expected.getTariff().name());
            });
        }
    }

    @Test
    @DisplayName("Проверка получения заявок по имени пассажира")
    void test_get_byPassengerName() {
        final var source = new ArrayList<TripOrderRecord>();
        final var passengers = new HashMap<UUID, EmployeeRecord>();
        for (int i = 0; i < 100; i++) {
            final var passenger = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .set(Tables.EMPLOYEE.LAST_NAME, "Пассажиров%03d".formatted(i))
                .set(Tables.EMPLOYEE.FIRST_NAME, "Пассажир%03d".formatted(i))
                .set(Tables.EMPLOYEE.PATRONYMIC, "Пассажирович%03d".formatted(i))
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, UUID.randomUUID())
                .returning().fetchSingle();
            passengers.put(passenger.getId(), passenger);
            final var item = context.insertInto(Tables.TRIP_ORDER)
                .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PASSENGER_ID, passenger.getId())
                .set(Tables.TRIP_ORDER.DIGIT_ID, i + 1)
                .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now().plusDays(i))
                .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.PLANNED_DURATION, DayToSecond.valueOf(Duration.ZERO.plusMinutes(i)))
                .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.COMMENT, "comment %03d".formatted(i))
                .set(Tables.TRIP_ORDER.RECEIPT, "receipt %03d".formatted(i))
                .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
                .set(Tables.TRIP_ORDER.REASON, "reason %03d".formatted(i))
                .set(Tables.TRIP_ORDER.STATUS, OrderState.values()[i % OrderState.values().length])
                .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) i)
                .returning()
                .fetchSingle();
            final var first = context.insertInto(Tables.WAYPOINT)
                .set(Tables.WAYPOINT.ID, UUID.randomUUID())
                .set(Tables.WAYPOINT.LATITUDE, BigDecimal.valueOf(0))
                .set(Tables.WAYPOINT.LONGITUDE, BigDecimal.valueOf(1))
                .set(Tables.WAYPOINT.COUNTRY, Instancio.create(String.class))
                .set(Tables.WAYPOINT.REGION, Instancio.create(String.class))
                .returning()
                .fetchSingle();
            final var second = context.insertInto(Tables.WAYPOINT)
                .set(Tables.WAYPOINT.ID, UUID.randomUUID())
                .set(Tables.WAYPOINT.LATITUDE, BigDecimal.valueOf(2))
                .set(Tables.WAYPOINT.LONGITUDE, BigDecimal.valueOf(3))
                .set(Tables.WAYPOINT.COUNTRY, Instancio.create(String.class))
                .set(Tables.WAYPOINT.REGION, Instancio.create(String.class))
                .returning()
                .fetchSingle();
            context.insertInto(Tables.ORDERED_REQUEST_WAYPOINT)
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_ID, item.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.WAYPOINT_ID, first.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_NUMBER, 0)
                .execute();
            context.insertInto(Tables.ORDERED_REQUEST_WAYPOINT)
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_ID, item.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.WAYPOINT_ID, second.getId())
                .set(Tables.ORDERED_REQUEST_WAYPOINT.ORDER_NUMBER, 1)
                .execute();
            source.add(item);
        }

        final var actualList = tripOrdersProvider.getFull(new TestFilter(null, "Пасса"), 0, 20, "humanReadableId",
            true);
        final var expectedList = source.stream().skip(0).limit(20).toList();

        assertThat(actualList).isNotNull();
        assertThat(actualList.content()).hasSameSizeAs(expectedList);

        for (var i = 0; i < actualList.content().size(); i++) {
            final var actual = actualList.content().get(i);
            final var expected = expectedList.get(i);

            assertSoftly(it -> {
                it.assertThat(actual.getId()).isEqualTo(expected.getId());
                it.assertThat(actual.getHumanReadableId()).isEqualTo("YA-0001-%08d".formatted(expected.getDigitId()));
                it.assertThat(actual.getWaypoints()).isNotEmpty();
                it.assertThat(actual.getDate()).isEqualTo(expected.getDate());
                it.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
                it.assertThat(actual.getPassenger().getId()).isEqualTo(expected.getPassengerId());
                it.assertThat(actual.getPassenger().getLastName())
                    .isEqualTo(passengers.get(expected.getPassengerId()).getLastName());
                it.assertThat(actual.getPassenger().getFirstName())
                    .isEqualTo(passengers.get(expected.getPassengerId()).getFirstName());
                it.assertThat(actual.getPassenger().getPatronymic())
                    .isEqualTo(passengers.get(expected.getPassengerId()).getPatronymic());
                it.assertThat(actual.getPlanned().getCost()).isEqualTo(expected.getPlannedCost());
                it.assertThat(actual.getPlanned().getDuration()).isEqualTo(expected.getPlannedDuration().toDuration());
                it.assertThat(actual.getActual().getCost()).isEqualTo(expected.getActualCost());
                it.assertThat(actual.getPurposeId()).isEqualTo(expected.getPurposeId());
                it.assertThat(actual.getComment()).isEqualTo(expected.getComment());
                it.assertThat(actual.getReceipt()).isEqualTo(expected.getReceipt());
                it.assertThat(actual.getTariff().name()).isEqualTo(expected.getTariff().name());
            });
        }
    }

    @Test
    @DisplayName("Проверка получения заявок по дате от - до. Перепутаны даты")
    void test_get_byDateFrom_To_shuffled() {
        for (int i = 0; i < 100; i++) {
            final var passenger = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .set(Tables.EMPLOYEE.LAST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.FIRST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.PATRONYMIC, Instancio.create(String.class))
                .returning().fetchSingle();
            context.insertInto(Tables.TRIP_ORDER)
                .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PASSENGER_ID, passenger.getId())
                .set(Tables.TRIP_ORDER.DIGIT_ID, i + 1)
                .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now().plusDays(i))
                .set(Tables.TRIP_ORDER.STATUS, OrderState.values()[i % OrderState.values().length])
                .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.PLANNED_DURATION, DayToSecond.valueOf(Duration.ZERO.plusMinutes(i)))
                .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.COMMENT, "comment %03d".formatted(i))
                .set(Tables.TRIP_ORDER.RECEIPT, "receipt %03d".formatted(i))
                .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
                .set(Tables.TRIP_ORDER.REASON, "reason %03d".formatted(i))
                .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) i)
                .returning()
                .fetchSingle();
        }

        final var actualList = tripOrdersProvider.get(
            new TestFilter(OffsetDateTime.now().plusDays(15), OffsetDateTime.now().plusDays(10)), 0, 20,
            "humanReadableId", true);

        assertThat(actualList).isNotNull();
        assertThat(actualList.content()).isEmpty();
    }

    @Test
    @DisplayName("Проверка получения заявок по идентификатору")
    void test_get_byId() {
        final var source = new ArrayList<TripOrderRecord>();
        final var sourceFraud = new ArrayList<FraudRecord>();
        final var sourceHistory = new ArrayList<TripOrderHistoryRecord>();
        final var modifiedAt = OffsetDateTime.now();
        final var department = context.insertInto(Tables.DEPARTMENT)
            .set(Tables.DEPARTMENT.ID, UUID.randomUUID())
            .returning().fetchSingle();
        final var passenger = context.insertInto(Tables.EMPLOYEE)
            .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
            .set(Tables.EMPLOYEE.DEPARTMENT_ID, department.getId())
            .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
            .set(Tables.EMPLOYEE.LAST_NAME, Instancio.create(String.class))
            .set(Tables.EMPLOYEE.FIRST_NAME, Instancio.create(String.class))
            .set(Tables.EMPLOYEE.PATRONYMIC, Instancio.create(String.class))
            .returning().fetchSingle();
        final var approver = context.insertInto(Tables.EMPLOYEE)
            .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
            .set(Tables.EMPLOYEE.DEPARTMENT_ID, department.getId())
            .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
            .set(Tables.EMPLOYEE.LAST_NAME, Instancio.create(String.class))
            .set(Tables.EMPLOYEE.FIRST_NAME, Instancio.create(String.class))
            .set(Tables.EMPLOYEE.PATRONYMIC, Instancio.create(String.class))
            .returning().fetchSingle();
        final var item = context.insertInto(Tables.TRIP_ORDER)
            .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
            .set(Tables.TRIP_ORDER.PASSENGER_ID, passenger.getId())
            .set(Tables.TRIP_ORDER.APPROVER_ID, approver.getId())
            .set(Tables.TRIP_ORDER.DIGIT_ID, 1)
            .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now())
            .set(Tables.TRIP_ORDER.STATUS, OrderState.values()[0])
            .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
            .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.valueOf(100))
            .set(Tables.TRIP_ORDER.PLANNED_DURATION, DayToSecond.valueOf(Duration.ZERO.plusMinutes(1)))
            .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.valueOf(100))
            .set(Tables.TRIP_ORDER.COMMENT, "comment %03d".formatted(0))
            .set(Tables.TRIP_ORDER.RECEIPT, "receipt %03d".formatted(0))
            .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
            .set(Tables.TRIP_ORDER.REASON, "reason %03d".formatted(0))
            .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, 100L)
            .returning()
            .fetchSingle();
        source.add(item);
        final var fraud = context.insertInto(Tables.FRAUD)
            .set(Tables.FRAUD.ID, item.getId())
            .set(Tables.FRAUD.COMMENT, "fraud")
            .set(Tables.FRAUD.TYPE, "RECEIPT")
            .returning()
            .fetchSingle();
        sourceFraud.add(fraud);

        final var history = context.insertInto(Tables.TRIP_ORDER_HISTORY)
            .set(Tables.TRIP_ORDER_HISTORY.ORDER_ID, item.getId())
            .set(Tables.TRIP_ORDER_HISTORY.STATUS, OrderState.CONFIRMED)
            .set(Tables.TRIP_ORDER_HISTORY.MODIFIED_AT, modifiedAt)
            .returning()
            .fetchSingle();
        sourceHistory.add(history);

        final var actualOpt = tripOrdersProvider.get(organization.getId(), source.get(0).getId());

        assertThat(actualOpt).isPresent();

        final var actual = actualOpt.get();
        final var expected = source.get(0);
        final var expectedFraud = sourceFraud.get(0);

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(expected.getId());
            it.assertThat(actual.getHumanReadableId()).isEqualTo("YA-0001-%08d".formatted(expected.getDigitId()));
            it.assertThat(actual.getWaypoints()).hasSize(0);
            it.assertThat(actual.getDate()).isEqualTo(expected.getDate());
            it.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
            it.assertThat(actual.getPassenger().getId()).isEqualTo(expected.getPassengerId());
            it.assertThat(actual.getPlanned().getCost()).isEqualTo(expected.getPlannedCost());
            it.assertThat(actual.getPlanned().getDuration()).isEqualTo(expected.getPlannedDuration().toDuration());
            it.assertThat(actual.getActual().getCost()).isEqualTo(expected.getActualCost());
            it.assertThat(actual.getPurposeId()).isEqualTo(expected.getPurposeId());
            it.assertThat(actual.getComment()).isEqualTo(expected.getComment());
            it.assertThat(actual.getReceipt()).isEqualTo(expected.getReceipt());
            it.assertThat(actual.getTariff().name()).isEqualTo(expected.getTariff().name());
            it.assertThat(actual.getFraud()).isNotNull();
            it.assertThat(actual.getFraud().getComment()).isEqualTo(expectedFraud.getComment());
            it.assertThat(actual.getApprovalDate()).isEqualTo(truncateToMicros(modifiedAt));
            it.assertThat(actual.getApprover().getId()).isEqualTo(approver.getId());
        });

        final var actualOpt2 = tripOrdersProvider.get(organization.getId(), UUID.randomUUID());
        assertThat(actualOpt2).isEmpty();
    }

    @Test
    @DisplayName("Проверка получения заявки по идентификатору без организации")
    void test_get_byId_only() {
        final var department = context.insertInto(Tables.DEPARTMENT)
            .set(Tables.DEPARTMENT.ID, UUID.randomUUID())
            .returning().fetchSingle();
        final var passenger = context.insertInto(Tables.EMPLOYEE)
            .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
            .set(Tables.EMPLOYEE.DEPARTMENT_ID, department.getId())
            .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
            .set(Tables.EMPLOYEE.LAST_NAME, Instancio.create(String.class))
            .set(Tables.EMPLOYEE.FIRST_NAME, Instancio.create(String.class))
            .set(Tables.EMPLOYEE.PATRONYMIC, Instancio.create(String.class))
            .returning().fetchSingle();
        final var item = context.insertInto(Tables.TRIP_ORDER)
            .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
            .set(Tables.TRIP_ORDER.PASSENGER_ID, passenger.getId())
            .set(Tables.TRIP_ORDER.DIGIT_ID, 1)
            .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now())
            .set(Tables.TRIP_ORDER.STATUS, OrderState.NEW)
            .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
            .set(Tables.TRIP_ORDER.MODIFIED_AT, OffsetDateTime.now())
            .set(Tables.TRIP_ORDER.HASH, "hash")
            .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.TEN)
            .set(Tables.TRIP_ORDER.PLANNED_DURATION,
                DayToSecond.valueOf(Duration.ZERO.plusSeconds(Instancio.create(Integer.class))))
            .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.ZERO)
            .set(Tables.TRIP_ORDER.COMMENT, "comment")
            .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
            .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) 200)
            .returning()
            .fetchSingle();

        final var actual = tripOrdersProvider.get(item.getId());

        assertThat(actual).isPresent();
        assertThat(actual.get().getId()).isEqualTo(item.getId());
    }

    @Test
    @DisplayName("Проверка получения заявок по человекочитаемому идентификатору")
    void test_get_byHumanReadableId() {
        final var source = new ArrayList<TripOrderRecord>();
        for (int i = 0; i < 100; i++) {
            final var department = context.insertInto(Tables.DEPARTMENT)
                .set(Tables.DEPARTMENT.ID, UUID.randomUUID())
                .returning().fetchSingle();
            final var passenger = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, department.getId())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .set(Tables.EMPLOYEE.LAST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.FIRST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.PATRONYMIC, Instancio.create(String.class))
                .returning().fetchSingle();
            final var item = context.insertInto(Tables.TRIP_ORDER)
                .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PASSENGER_ID, passenger.getId())
                .set(Tables.TRIP_ORDER.DIGIT_ID, i + 1)
                .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now().plusDays(i))
                .set(Tables.TRIP_ORDER.STATUS, OrderState.values()[i % OrderState.values().length])
                .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.PLANNED_DURATION, DayToSecond.valueOf(Duration.ZERO.plusMinutes(i)))
                .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.COMMENT, "comment %03d".formatted(i))
                .set(Tables.TRIP_ORDER.RECEIPT, "receipt %03d".formatted(i))
                .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
                .set(Tables.TRIP_ORDER.REASON, "reason %03d".formatted(i))
                .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) i)
                .returning()
                .fetchSingle();
            source.add(item);
        }

        final var actualList = tripOrdersProvider.get(new TestFilter("YA-0001-0000000"), 0, 20, "humanReadableId",
            true);
        final var expectedList = source.stream().skip(0).limit(9).toList();

        assertThat(actualList).isNotNull();
        assertThat(actualList.content()).hasSameSizeAs(expectedList);

        for (var i = 0; i < actualList.content().size(); i++) {
            final var actual = actualList.content().get(i);
            final var expected = expectedList.get(i);

            assertSoftly(it -> {
                it.assertThat(actual.getId()).isEqualTo(expected.getId());
                it.assertThat(actual.getHumanReadableId()).isEqualTo("YA-0001-%08d".formatted(expected.getDigitId()));
                it.assertThat(actual.getWaypoints()).isEmpty();
                it.assertThat(actual.getDate()).isEqualTo(expected.getDate());
                it.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
                it.assertThat(actual.getPassenger().getId()).isEqualTo(expected.getPassengerId());
                it.assertThat(actual.getPlanned().getCost()).isEqualTo(expected.getPlannedCost());
                it.assertThat(actual.getPlanned().getDuration()).isEqualTo(expected.getPlannedDuration().toDuration());
                it.assertThat(actual.getActual().getCost()).isEqualTo(expected.getActualCost());
                it.assertThat(actual.getPurposeId()).isEqualTo(expected.getPurposeId());
                it.assertThat(actual.getComment()).isEqualTo(expected.getComment());
                it.assertThat(actual.getReceipt()).isEqualTo(expected.getReceipt());
                it.assertThat(actual.getTariff().name()).isEqualTo(expected.getTariff().name());
            });
        }
    }

    @Test
    @DisplayName("Проверка получения заявок по БЕ (бизнес единица)")
    void test_get_byBalanceUnitSet() {
        final var source = new ArrayList<TripOrderRecord>();
        for (int i = 0; i < 100; i++) {
            final var department = context.insertInto(Tables.DEPARTMENT)
                .set(Tables.DEPARTMENT.ID, UUID.randomUUID())
                .returning().fetchSingle();
            final var passenger = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, department.getId())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .set(Tables.EMPLOYEE.LAST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.FIRST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.PATRONYMIC, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.COST_CENTER, i < 10 ? "1111L22222" : null)
                .returning().fetchSingle();
            final var item = context.insertInto(Tables.TRIP_ORDER)
                .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PASSENGER_ID, passenger.getId())
                .set(Tables.TRIP_ORDER.DIGIT_ID, i + 1)
                .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now().plusDays(i))
                .set(Tables.TRIP_ORDER.STATUS, OrderState.values()[i % OrderState.values().length])
                .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.PLANNED_DURATION, DayToSecond.valueOf(Duration.ZERO.plusMinutes(i)))
                .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.COMMENT, "comment %03d".formatted(i))
                .set(Tables.TRIP_ORDER.RECEIPT, "receipt %03d".formatted(i))
                .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
                .set(Tables.TRIP_ORDER.REASON, "reason %03d".formatted(i))
                .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) i)
                .returning()
                .fetchSingle();
            source.add(item);
        }

        final var actualList = tripOrdersProvider.get(new TestFilter(List.of(1111)), 0, 20, "humanReadableId", true);
        final var expectedList = source.stream().limit(10).toList();

        assertThat(actualList).isNotNull();
        assertThat(actualList.content()).hasSameSizeAs(expectedList);

        for (var i = 0; i < actualList.content().size(); i++) {
            final var actual = actualList.content().get(i);
            final var expected = expectedList.get(i);

            assertSoftly(it -> {
                it.assertThat(actual.getId()).isEqualTo(expected.getId());
                it.assertThat(actual.getHumanReadableId()).isEqualTo("YA-0001-%08d".formatted(expected.getDigitId()));
                it.assertThat(actual.getWaypoints()).isEmpty();
                it.assertThat(actual.getDate()).isEqualTo(expected.getDate());
                it.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
                it.assertThat(actual.getPassenger().getId()).isEqualTo(expected.getPassengerId());
                it.assertThat(actual.getPlanned().getCost()).isEqualTo(expected.getPlannedCost());
                it.assertThat(actual.getPlanned().getDuration()).isEqualTo(expected.getPlannedDuration().toDuration());
                it.assertThat(actual.getActual().getCost()).isEqualTo(expected.getActualCost());
                it.assertThat(actual.getPurposeId()).isEqualTo(expected.getPurposeId());
                it.assertThat(actual.getComment()).isEqualTo(expected.getComment());
                it.assertThat(actual.getReceipt()).isEqualTo(expected.getReceipt());
                it.assertThat(actual.getTariff().name()).isEqualTo(expected.getTariff().name());
            });
        }
    }

    @Test
    @DisplayName("Проверка существования заявок по идентификатору c учетом организации")
    void test_exists_byIdForOrganization() {
        final var source = new ArrayList<TripOrderRecord>();
        for (int i = 0; i < 100; i++) {
            final var passenger = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .set(Tables.EMPLOYEE.LAST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.FIRST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.PATRONYMIC, Instancio.create(String.class))
                .returning().fetchSingle();
            final var item = context.insertInto(Tables.TRIP_ORDER)
                .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PASSENGER_ID, passenger.getId())
                .set(Tables.TRIP_ORDER.DIGIT_ID, i + 1)
                .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now().plusDays(i))
                .set(Tables.TRIP_ORDER.STATUS, OrderState.values()[i % OrderState.values().length])
                .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.PLANNED_DURATION, DayToSecond.valueOf(Duration.ZERO.plusMinutes(i)))
                .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.COMMENT, "comment %03d".formatted(i))
                .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
                .set(Tables.TRIP_ORDER.REASON, "reason %03d".formatted(i))
                .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) i)
                .returning()
                .fetchSingle();
            source.add(item);
        }

        final var actual = tripOrdersProvider.exists(organization.getId(), source.get(30).getId());

        assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("Проверка существования заявок по идентификатору")
    void test_exists_byId() {
        final var source = new ArrayList<TripOrderRecord>();
        for (int i = 0; i < 100; i++) {
            final var passenger = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .set(Tables.EMPLOYEE.LAST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.FIRST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.PATRONYMIC, Instancio.create(String.class))
                .returning().fetchSingle();
            final var item = context.insertInto(Tables.TRIP_ORDER)
                .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PASSENGER_ID, passenger.getId())
                .set(Tables.TRIP_ORDER.DIGIT_ID, i + 1)
                .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now().plusDays(i))
                .set(Tables.TRIP_ORDER.STATUS, OrderState.values()[i % OrderState.values().length])
                .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.PLANNED_DURATION, DayToSecond.valueOf(Duration.ZERO.plusMinutes(i)))
                .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.COMMENT, "comment %03d".formatted(i))
                .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
                .set(Tables.TRIP_ORDER.REASON, "reason %03d".formatted(i))
                .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) i)
                .returning()
                .fetchSingle();
            source.add(item);
        }

        final var actual = tripOrdersProvider.exists(source.get(30).getId());

        assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("Проверка получения заявок по МВЗ")
    void test_get_byCostCenter() {
        final var source = new ArrayList<TripOrderRecord>();
        for (int i = 0; i < 100; i++) {
            final var department = context.insertInto(Tables.DEPARTMENT)
                .set(Tables.DEPARTMENT.ID, UUID.randomUUID())
                .returning().fetchSingle();
            final var passenger = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, department.getId())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .set(Tables.EMPLOYEE.LAST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.FIRST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.PATRONYMIC, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.COST_CENTER, i < 10 ? "1111L22222" : "2222L11111")
                .returning().fetchSingle();
            final var item = context.insertInto(Tables.TRIP_ORDER)
                .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PASSENGER_ID, passenger.getId())
                .set(Tables.TRIP_ORDER.DIGIT_ID, i + 1)
                .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now().plusDays(i))
                .set(Tables.TRIP_ORDER.STATUS, OrderState.values()[i % OrderState.values().length])
                .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.PLANNED_DURATION, DayToSecond.valueOf(Duration.ZERO.plusMinutes(i)))
                .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.valueOf(i))
                .set(Tables.TRIP_ORDER.COMMENT, "comment %03d".formatted(i))
                .set(Tables.TRIP_ORDER.RECEIPT, "receipt %03d".formatted(i))
                .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
                .set(Tables.TRIP_ORDER.REASON, "reason %03d".formatted(i))
                .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) i)
                .returning()
                .fetchSingle();
            source.add(item);
        }

        final var actualList = tripOrdersProvider.get(new TestFilter("1111L22222", true), 0, 20, "humanReadableId",
            true);
        final var expectedList = source.stream().limit(10).toList();

        assertThat(actualList).isNotNull();
        assertThat(actualList.content()).hasSameSizeAs(expectedList);

        for (var i = 0; i < actualList.content().size(); i++) {
            final var actual = actualList.content().get(i);
            final var expected = expectedList.get(i);

            assertSoftly(it -> {
                it.assertThat(actual.getId()).isEqualTo(expected.getId());
                it.assertThat(actual.getHumanReadableId()).isEqualTo("YA-0001-%08d".formatted(expected.getDigitId()));
                it.assertThat(actual.getWaypoints()).isEmpty();
                it.assertThat(actual.getDate()).isEqualTo(expected.getDate());
                it.assertThat(actual.getStatus().name()).isEqualTo(expected.getStatus().name());
                it.assertThat(actual.getPassenger().getId()).isEqualTo(expected.getPassengerId());
                it.assertThat(actual.getPlanned().getCost()).isEqualTo(expected.getPlannedCost());
                it.assertThat(actual.getPlanned().getDuration()).isEqualTo(expected.getPlannedDuration().toDuration());
                it.assertThat(actual.getActual().getCost()).isEqualTo(expected.getActualCost());
                it.assertThat(actual.getPurposeId()).isEqualTo(expected.getPurposeId());
                it.assertThat(actual.getComment()).isEqualTo(expected.getComment());
                it.assertThat(actual.getReceipt()).isEqualTo(expected.getReceipt());
                it.assertThat(actual.getTariff().name()).isEqualTo(expected.getTariff().name());
            });
        }
    }

    @Test
    @DisplayName("Проверка удаления")
    void test_delete() {
        final var passenger = context.insertInto(Tables.EMPLOYEE)
            .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
            .set(Tables.EMPLOYEE.DEPARTMENT_ID, UUID.randomUUID())
            .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
            .returning().fetchSingle();
        final var item = context.insertInto(Tables.TRIP_ORDER)
            .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
            .set(Tables.TRIP_ORDER.PASSENGER_ID, passenger.getId())
            .set(Tables.TRIP_ORDER.DIGIT_ID, 1)
            .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now())
            .set(Tables.TRIP_ORDER.STATUS, OrderState.NEW)
            .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
            .set(Tables.TRIP_ORDER.MODIFIED_AT, OffsetDateTime.now())
            .set(Tables.TRIP_ORDER.HASH, "hash")
            .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.TEN)
            .set(Tables.TRIP_ORDER.PLANNED_DURATION,
                DayToSecond.valueOf(Duration.ZERO.plusSeconds(Instancio.create(Integer.class))))
            .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.ZERO)
            .set(Tables.TRIP_ORDER.COMMENT, "comment")
            .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
            .set(Tables.TRIP_ORDER.REASON, "reason %03d".formatted(1))
            .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) 10)
            .returning()
            .fetchSingle();

        tripOrdersProvider.delete(organization.getId(), item.getId());

        assertThat(context.selectFrom(Tables.TRIP_ORDER).where(Tables.TRIP_ORDER.ID.eq(item.getId())).fetchSingle()
            .getStatus()).isEqualTo(OrderState.CANCELLED);
    }

    @Test
    @DisplayName("Проверка изменения. Подмена")
    void test_edit_replace() {
        final var item = context.insertInto(Tables.TRIP_ORDER)
            .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
            .set(Tables.TRIP_ORDER.PASSENGER_ID, UUID.randomUUID())
            .set(Tables.TRIP_ORDER.DIGIT_ID, 1)
            .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now())
            .set(Tables.TRIP_ORDER.STATUS, OrderState.NEW)
            .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
            .set(Tables.TRIP_ORDER.MODIFIED_AT, OffsetDateTime.now())
            .set(Tables.TRIP_ORDER.HASH, "hash")
            .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.TEN)
            .set(Tables.TRIP_ORDER.PLANNED_DURATION,
                DayToSecond.valueOf(Duration.ZERO.plusSeconds(Instancio.create(Integer.class))))
            .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.ZERO)
            .set(Tables.TRIP_ORDER.COMMENT, "comment")
            .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
            .set(Tables.TRIP_ORDER.REASON, "reason")
            .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) 20)
            .returning()
            .fetchSingle();

        final var newData = Instancio.of(TestTripOrderSource.class)
            .set(Select.field(TestTripOrderSource::getReason), "mme")
            .set(Select.field(TestTripOrderSource::getStatus), State.CONFIRMED)
            .create();

        tripOrdersProvider.edit(null, item.getId(), newData, Set.of("=status", "=factCost", "=reason"));

        final var actual = context.selectFrom(Tables.TRIP_ORDER).fetchSingle();

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(item.getId());
            it.assertThat(actual.getStatus().name()).isEqualTo(newData.getStatus().name());
            it.assertThat(actual.getActualCost()).isEqualTo(newData.getActual().getCost);
            it.assertThat(actual.getReason()).isEqualTo(newData.getReason());
        });
    }

    @Test
    @DisplayName("Проверка изменения. Добавление")
    void test_edit_add() {
        final var item = context.insertInto(Tables.TRIP_ORDER)
            .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
            .set(Tables.TRIP_ORDER.PASSENGER_ID, UUID.randomUUID())
            .set(Tables.TRIP_ORDER.DIGIT_ID, 1)
            .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now())
            .set(Tables.TRIP_ORDER.STATUS, OrderState.NEW)
            .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
            .set(Tables.TRIP_ORDER.MODIFIED_AT, OffsetDateTime.now())
            .set(Tables.TRIP_ORDER.HASH, "hash")
            .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.TEN)
            .set(Tables.TRIP_ORDER.PLANNED_DURATION,
                DayToSecond.valueOf(Duration.ZERO.plusSeconds(Instancio.create(Integer.class))))
            .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.ZERO)
            .set(Tables.TRIP_ORDER.COMMENT, "comment")
            .set(Tables.TRIP_ORDER.REASON, "reason")
            .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
            .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) 10)
            .returning()
            .fetchSingle();

        final var newData = Instancio.of(TestTripOrderSource.class)
            .set(Select.field(TestTripOrderSource::getReason), "mme")
            .set(Select.field(TestTripOrderSource::getStatus), State.CONFIRMED)
            .create();

        tripOrdersProvider.edit(null, item.getId(), newData, Set.of("+status", "+factCost", "+reason", "=receiptLink"));

        final var actual = context.selectFrom(Tables.TRIP_ORDER).fetchSingle();

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(item.getId());
            it.assertThat(actual.getStatus().name()).isEqualTo(newData.getStatus().name());
            it.assertThat(actual.getActualCost()).isEqualTo(newData.getActual().getCost.add(item.getActualCost()));
            it.assertThat(actual.getReason()).isEqualTo(item.getReason() + newData.getReason());
            it.assertThat(actual.getReceiptLink()).isNotNull();
        });
    }

    @Test
    @DisplayName("Проверка изменения. Оценка")
    void test_edit_assessment() {
        final var employee = context.insertInto(Tables.EMPLOYEE)
            .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
            .set(Tables.EMPLOYEE.DEPARTMENT_ID, UUID.randomUUID())
            .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
            .returning().fetchSingle();

        final var item = context.insertInto(Tables.TRIP_ORDER)
            .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
            .set(Tables.TRIP_ORDER.PASSENGER_ID, UUID.randomUUID())
            .set(Tables.TRIP_ORDER.DIGIT_ID, 1)
            .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now())
            .set(Tables.TRIP_ORDER.STATUS, OrderState.NEW)
            .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
            .set(Tables.TRIP_ORDER.MODIFIED_AT, OffsetDateTime.now())
            .set(Tables.TRIP_ORDER.HASH, "hash")
            .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.TEN)
            .set(Tables.TRIP_ORDER.PLANNED_DURATION,
                DayToSecond.valueOf(Duration.ZERO.plusSeconds(Instancio.create(Integer.class))))
            .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.ZERO)
            .set(Tables.TRIP_ORDER.COMMENT, "comment")
            .set(Tables.TRIP_ORDER.REASON, "reason")
            .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
            .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) 10)
            .returning()
            .fetchSingle();

        final var newData = Instancio.of(TestTripOrderSource.class)
            .set(Select.field(TestTripOrderSource::getReason), "mme")
            .create();

        tripOrdersProvider.edit(employee.getId(), item.getId(), newData,
            Set.of("+assessments.service.rating", "+assessments.service.comment"));

        final var actual = context.selectFrom(ASSESSMENTS).fetchSingle();

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isNotNull();
            it.assertThat(actual.getOrderId()).isEqualTo(item.getId());
            it.assertThat(actual.getEmployeeId()).isEqualTo(employee.getId());
            it.assertThat(actual.getCreatedAt()).isNotNull();
            it.assertThat(actual.getComment()).isEqualTo(newData.getAssessments().getService().getComment());
            it.assertThat(actual.getRating()).isEqualTo(newData.getAssessments().getService().getRating());
        });
    }

    @Test
    @DisplayName("Проверка изменения. Уменьшение")
    void test_edit_decrease() {
        final var item = context.insertInto(Tables.TRIP_ORDER)
            .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
            .set(Tables.TRIP_ORDER.PASSENGER_ID, UUID.randomUUID())
            .set(Tables.TRIP_ORDER.DIGIT_ID, 1)
            .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now())
            .set(Tables.TRIP_ORDER.STATUS, OrderState.NEW)
            .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
            .set(Tables.TRIP_ORDER.MODIFIED_AT, OffsetDateTime.now())
            .set(Tables.TRIP_ORDER.HASH, "hash")
            .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.TEN)
            .set(Tables.TRIP_ORDER.PLANNED_DURATION,
                DayToSecond.valueOf(Duration.ZERO.plusSeconds(Instancio.create(Integer.class))))
            .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.ZERO)
            .set(Tables.TRIP_ORDER.COMMENT, "comment")
            .set(Tables.TRIP_ORDER.REASON, "reason")
            .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
            .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) 200)
            .returning()
            .fetchSingle();

        final var newData = Instancio.of(TestTripOrderSource.class)
            .set(Select.field(TestTripOrderSource::getReason), "eas")
            .create();

        tripOrdersProvider.edit(null, item.getId(), newData, Set.of("-status", "-factCost", "-reason"));

        final var actual = context.selectFrom(Tables.TRIP_ORDER).fetchSingle();

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(item.getId());
            it.assertThat(actual.getStatus().name()).isEqualTo(item.getStatus().name());
            it.assertThat(actual.getActualCost())
                .isEqualTo(item.getActualCost().subtract(newData.getActual().getCost()));
            it.assertThat(actual.getReason()).isEqualTo(item.getReason().replace(newData.getReason(), ""));
        });
    }

    @Test
    @DisplayName("Получение мета")
    void test_edit_meta() {
        final var passenger = context.insertInto(Tables.EMPLOYEE)
            .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
            .set(Tables.EMPLOYEE.DEPARTMENT_ID, UUID.randomUUID())
            .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
            .returning().fetchSingle();
        final var item = context.insertInto(Tables.TRIP_ORDER)
            .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
            .set(Tables.TRIP_ORDER.PASSENGER_ID, passenger.getId())
            .set(Tables.TRIP_ORDER.DIGIT_ID, 1)
            .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now())
            .set(Tables.TRIP_ORDER.STATUS, OrderState.NEW)
            .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
            .set(Tables.TRIP_ORDER.MODIFIED_AT, OffsetDateTime.now())
            .set(Tables.TRIP_ORDER.HASH, "hash")
            .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.TEN)
            .set(Tables.TRIP_ORDER.PLANNED_DURATION,
                DayToSecond.valueOf(Duration.ZERO.plusSeconds(Instancio.create(Integer.class))))
            .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.ZERO)
            .set(Tables.TRIP_ORDER.COMMENT, "comment")
            .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
            .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) 200)
            .returning()
            .fetchSingle();

        final var actual = tripOrdersMetaProvider.meta(organization.getId(), item.getId());

        assertSoftly(it -> {
            it.assertThat(actual.hash()).isEqualTo(item.getHash());
            it.assertThat(actual.modifiedAt()).isEqualTo(item.getModifiedAt());
        });
    }

    @Test
    @DisplayName("Получение мета. Исключение при отсутствии заявки")
    void test_meta_notFound() {
        final var nonExistentId = UUID.randomUUID();

        assertSoftly(it -> {
            it.assertThatThrownBy(() -> tripOrdersMetaProvider.meta(organization.getId(), nonExistentId))
                .isInstanceOf(EntityNotFoundException.class);
            it.assertThatThrownBy(() -> tripOrdersMetaProvider.meta(null, nonExistentId))
                .isInstanceOf(EntityNotFoundException.class);
        });
    }

    @Test
    @DisplayName("Получение имени файла")
    void test_fileName() {
        final var item = context.insertInto(Tables.TRIP_ORDER)
            .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
            .set(Tables.TRIP_ORDER.PASSENGER_ID, UUID.randomUUID())
            .set(Tables.TRIP_ORDER.DIGIT_ID, 1)
            .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now())
            .set(Tables.TRIP_ORDER.STATUS, OrderState.NEW)
            .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
            .set(Tables.TRIP_ORDER.MODIFIED_AT, OffsetDateTime.now())
            .set(Tables.TRIP_ORDER.HASH, "hash")
            .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.TEN)
            .set(Tables.TRIP_ORDER.PLANNED_DURATION,
                DayToSecond.valueOf(Duration.ZERO.plusSeconds(Instancio.create(Integer.class))))
            .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.ZERO)
            .set(Tables.TRIP_ORDER.COMMENT, "comment")
            .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
            .set(Tables.TRIP_ORDER.RECEIPT, Instancio.create(String.class))
            .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) 100)
            .returning()
            .fetchSingle();

        final var actual = tripOrdersProvider.getFile(item.getId());

        assertSoftly(it -> it.assertThat(actual).isEqualTo(item.getReceipt()));
    }

    @Test
    @DisplayName("Успешное прикрепление чека к заявке")
    void attachFile_success() {
        final var requestId = UUID.randomUUID();
        final var fileName = UUID.randomUUID().toString();
        final var passengerId = UUID.randomUUID();
        final var purposeId = UUID.randomUUID();
        final var digitId = Instancio.create(Integer.class);
        context.insertInto(Tables.TRIP_ORDER)
                .set(Tables.TRIP_ORDER.ID, requestId)
                .set(Tables.TRIP_ORDER.PASSENGER_ID, passengerId)
                .set(Tables.TRIP_ORDER.DIGIT_ID, digitId)
                .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now())
                .set(Tables.TRIP_ORDER.STATUS, OrderState.NEW)
                .set(Tables.TRIP_ORDER.PURPOSE_ID, purposeId)
                .set(Tables.TRIP_ORDER.MODIFIED_AT, OffsetDateTime.now())
                .set(Tables.TRIP_ORDER.HASH, UUID.randomUUID().toString())
                .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.TEN)
                .set(Tables.TRIP_ORDER.PLANNED_DURATION,
                        DayToSecond.valueOf(Duration.ZERO.plusSeconds(Instancio.create(Integer.class))))
                .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.ZERO)
                .set(Tables.TRIP_ORDER.COMMENT, UUID.randomUUID().toString())
                .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
                .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, Instancio.create(Long.class))
                .execute();

        tripOrdersProvider.attachFile(requestId, fileName);

        final var expectedReceipt = context.select(Tables.TRIP_ORDER.RECEIPT)
                .from(Tables.TRIP_ORDER)
                .where(Tables.TRIP_ORDER.ID.eq(requestId))
                .fetchSingle()
                .get(Tables.TRIP_ORDER.RECEIPT);
        assertThat(expectedReceipt).isEqualTo(fileName);
    }

    @Test
    @DisplayName("Прикрепление чека к заявке: заявка не найдена")
    void attachFile_requestNotFound() {
        final var requestId = UUID.randomUUID();
        final var fileName = UUID.randomUUID().toString();

        assertThrows(DatabaseLayerException.class, () -> tripOrdersProvider.attachFile(requestId, fileName));
    }

    @Test
    @DisplayName("Прикрепление чека к заявке: повторное прикрепление")
    void attachFile_duplicateAttachment() {
        final var requestId = UUID.randomUUID();
        final var fileName = UUID.randomUUID().toString();
        final var newFileName = UUID.randomUUID().toString();
        final var passengerId = UUID.randomUUID();
        final var purposeId = UUID.randomUUID();
        final var digitId = Instancio.create(Integer.class);

        context.insertInto(Tables.TRIP_ORDER)
                .set(Tables.TRIP_ORDER.ID, requestId)
                .set(Tables.TRIP_ORDER.PASSENGER_ID, passengerId)
                .set(Tables.TRIP_ORDER.DIGIT_ID, digitId)
                .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now())
                .set(Tables.TRIP_ORDER.STATUS, OrderState.NEW)
                .set(Tables.TRIP_ORDER.PURPOSE_ID, purposeId)
                .set(Tables.TRIP_ORDER.MODIFIED_AT, OffsetDateTime.now())
                .set(Tables.TRIP_ORDER.HASH, UUID.randomUUID().toString())
                .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.TEN)
                .set(Tables.TRIP_ORDER.PLANNED_DURATION,
                        DayToSecond.valueOf(Duration.ZERO.plusSeconds(Instancio.create(Integer.class))))
                .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.ZERO)
                .set(Tables.TRIP_ORDER.COMMENT, UUID.randomUUID().toString())
                .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
                .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, Instancio.create(Long.class))
                .execute();

        tripOrdersProvider.attachFile(requestId, fileName);
        tripOrdersProvider.attachFile(requestId, newFileName);

        final var receiptAfter = context.select(Tables.TRIP_ORDER.RECEIPT)
                .from(Tables.TRIP_ORDER)
                .where(Tables.TRIP_ORDER.ID.eq(requestId))
                .fetchSingle()
                .get(Tables.TRIP_ORDER.RECEIPT);
        assertThat(receiptAfter).isEqualTo(newFileName);
    }

    @Test
    @DisplayName("Получение детальной информации о чеке по заявке")
    void getFileDetailed_success() {
        final var requestId = UUID.randomUUID();
        final var fileName = UUID.randomUUID().toString();
        final var passengerId = UUID.randomUUID();
        final var purposeId = UUID.randomUUID();
        final var digitId = Instancio.create(Integer.class);
        final var actualCost = Instancio.create(BigDecimal.class);
        final var date = OffsetDateTime.now();

        context.insertInto(Tables.TRIP_ORDER)
                .set(Tables.TRIP_ORDER.ID, requestId)
                .set(Tables.TRIP_ORDER.PASSENGER_ID, passengerId)
                .set(Tables.TRIP_ORDER.DIGIT_ID, digitId)
                .set(Tables.TRIP_ORDER.DATE, date)
                .set(Tables.TRIP_ORDER.STATUS, OrderState.NEW)
                .set(Tables.TRIP_ORDER.PURPOSE_ID, purposeId)
                .set(Tables.TRIP_ORDER.MODIFIED_AT, OffsetDateTime.now())
                .set(Tables.TRIP_ORDER.HASH, UUID.randomUUID().toString())
                .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.TEN)
                .set(Tables.TRIP_ORDER.PLANNED_DURATION,
                        DayToSecond.valueOf(Duration.ZERO.plusSeconds(Instancio.create(Integer.class))))
                .set(Tables.TRIP_ORDER.ACTUAL_COST, actualCost)
                .set(Tables.TRIP_ORDER.RECEIPT, fileName)
                .set(Tables.TRIP_ORDER.COMMENT, UUID.randomUUID().toString())
                .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
                .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, Instancio.create(Long.class))
                .execute();

        final var result = tripOrdersProvider.getFileDetailed(requestId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(requestId);
        assertThat(result.getDate()).isCloseTo(date, within(1, ChronoUnit.MILLIS));
        assertThat(result.getActual().getCost()).isEqualTo(actualCost);
        assertThat(result.getReceipt()).isEqualTo(fileName);
    }

    @Test
    @DisplayName("Получение детальной информации о чеке: заявка не найдена")
    void getFileDetailed_requestNotFound() {
        final var requestId = UUID.randomUUID();

        assertThrows(DatabaseLayerException.class, () -> tripOrdersProvider.getFileDetailed(requestId));
    }

    @Test
    @DisplayName("Получение детальной информации о чеке: заявка без чека")
    void getFileDetailed_withoutReceipt() {
        final var requestId = UUID.randomUUID();
        final var passengerId = UUID.randomUUID();
        final var purposeId = UUID.randomUUID();
        final var digitId = Instancio.create(Integer.class);
        final var actualCost = Instancio.create(BigDecimal.class);
        final var date = OffsetDateTime.now();

        context.insertInto(Tables.TRIP_ORDER)
                .set(Tables.TRIP_ORDER.ID, requestId)
                .set(Tables.TRIP_ORDER.PASSENGER_ID, passengerId)
                .set(Tables.TRIP_ORDER.DIGIT_ID, digitId)
                .set(Tables.TRIP_ORDER.DATE, date)
                .set(Tables.TRIP_ORDER.STATUS, OrderState.NEW)
                .set(Tables.TRIP_ORDER.PURPOSE_ID, purposeId)
                .set(Tables.TRIP_ORDER.MODIFIED_AT, OffsetDateTime.now())
                .set(Tables.TRIP_ORDER.HASH, UUID.randomUUID().toString())
                .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.TEN)
                .set(Tables.TRIP_ORDER.PLANNED_DURATION,
                        DayToSecond.valueOf(Duration.ZERO.plusSeconds(Instancio.create(Integer.class))))
                .set(Tables.TRIP_ORDER.ACTUAL_COST, actualCost)
                .set(Tables.TRIP_ORDER.COMMENT, UUID.randomUUID().toString())
                .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
                .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, Instancio.create(Long.class))
                .execute();

        final var result = tripOrdersProvider.getFileDetailed(requestId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(requestId);
        assertThat(result.getDate()).isCloseTo(date, within(1, ChronoUnit.MILLIS));
        assertThat(result.getActual().getCost()).isEqualTo(actualCost);
        assertThat(result.getReceipt()).isNull();
    }

    @Test
    @DisplayName("Открепление файла")
    void test_detachFile() {
        final var item = context.insertInto(Tables.TRIP_ORDER)
            .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
            .set(Tables.TRIP_ORDER.PASSENGER_ID, UUID.randomUUID())
            .set(Tables.TRIP_ORDER.DIGIT_ID, 1)
            .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now())
            .set(Tables.TRIP_ORDER.STATUS, OrderState.NEW)
            .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
            .set(Tables.TRIP_ORDER.MODIFIED_AT, OffsetDateTime.now())
            .set(Tables.TRIP_ORDER.HASH, "hash")
            .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.TEN)
            .set(Tables.TRIP_ORDER.PLANNED_DURATION,
                DayToSecond.valueOf(Duration.ZERO.plusSeconds(Instancio.create(Integer.class))))
            .set(Tables.TRIP_ORDER.ACTUAL_COST, BigDecimal.ZERO)
            .set(Tables.TRIP_ORDER.COMMENT, "comment")
            .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
            .set(Tables.TRIP_ORDER.RECEIPT, Instancio.create(String.class))
            .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) 200)
            .returning()
            .fetchSingle();

        assertThat(
            context.fetch(context.select(Tables.TRIP_ORDER.RECEIPT).from(Tables.TRIP_ORDER)).getFirst()
                .get(0)).isEqualTo(
            item.getReceipt());

        tripOrdersProvider.detachFile(item.getId());

        assertThat(
            context.fetch(context.select(Tables.TRIP_ORDER.RECEIPT).from(Tables.TRIP_ORDER)).getFirst()
                .get(0)).isNull();
    }

    private record TestTripOrderSource(
        UUID getId,
        String getHumanReadableId,
        TestTripOrderData getPlanned,
        TestTripOrderData getActual,
        OffsetDateTime getDate,
        UUID getPassengerId,
        State getStatus,
        String getComment,
        String getReason,
        ru.sber.transport.request.external.model.Tariff getTariff,
        UUID getPurposeId,
        List<WaypointData> getWaypointData,
        URI getLink,
        TestAssessments getAssessments,
        String getReceiptLink
    ) implements EditTripOrderData {

    }

    private record TestAssessments(TestAssessment getService) implements Assessments {

    }

    private record TestAssessment(String getComment, byte getRating) implements Assessment {

    }

    private record TestTripOrderData(BigDecimal getCost, Duration getDuration, long getDistance) implements OrderData {

    }

    @Getter
    private static class TestBaseTripOrderSource implements BaseTripOrderData {

        OffsetDateTime date;

        List<WaypointData> waypoints;

        UUID purposeId;

        ru.sber.transport.request.external.model.Tariff tariff;

        String comment;

        BigDecimal taxiCost;

    }

    private record TestWaypointData(
        UUID getId,
        String getCountry,
        String getRegion,
        String getCity,
        String getStreet,
        String getHouse,
        String getBuilding,
        String getStructure,
        BigDecimal getLatitude,
        BigDecimal getLongitude
    ) implements WaypointData {

    }

    private record TestPriceData(BigDecimal price, Duration duration, URI link, long distance) implements PriceData {

    }

    private OffsetDateTime truncateToMicros(OffsetDateTime dt) {
        long nanos = dt.getNano();
        long roundedNanos = Math.round(nanos / 1_000.0) * 1_000;

        if (roundedNanos >= 1_000_000_000L) {
            return dt.truncatedTo(ChronoUnit.SECONDS).plusSeconds(1);
        } else {
            return dt.withNano((int) roundedNanos);
        }
    }

    private record TestFilter(UUID organizationId, String approverName, String passengerName, List<UUID> approver,
                              Boolean isStrictlyApprover,
                              List<UUID> passenger, OffsetDateTime startTimeFrom, OffsetDateTime startTimeTo,
                              List<State> states, String humanReadableId, List<Integer> balanceUnitSet,
                              String costCenter, Pair<OffsetDateTime, OffsetDateTime> orderPaymentFormationStartRange,
                              Set<UUID> departments) implements RequestFilter {

        TestFilter(State... states) {
            this(null, null, null, null, null, null, null, null, List.of(states), null, null, null, null, null);
        }

        public TestFilter(List<UUID> passengers, List<UUID> approver) {
            this(null, null, null, approver, false, passengers, null, null, null, null, null, null, null, null);
        }

        public TestFilter(String approverName, String passengerName) {
            this(null, approverName, passengerName, null, null, null, null, null, null, null, null, null, null, null);
        }

        public TestFilter(OffsetDateTime startTimeFrom, OffsetDateTime startTimeTo) {
            this(null, null, null, null, null, null, startTimeFrom, startTimeTo, null, null, null, null, null, null);
        }

        public TestFilter(String humanReadableId) {
            this(null, null, null, null, null, null, null, null, null, humanReadableId, null, null, null, null);
        }

        public TestFilter(String costCenter, boolean isForCostCenter) {
            this(null, null, null, null, null, null, null, null, null, null, null, costCenter, null, null);
        }

        public TestFilter(List<Integer> balanceUnitSet) {
            this(null, null, null, null, null, null, null, null, null, null, balanceUnitSet, null, null, null);
        }

    }

}