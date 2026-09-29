package ru.sber.transport.request.external.providers.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.qameta.allure.Feature;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.instancio.Instancio;
import org.jooq.DSLContext;
import org.jooq.types.DayToSecond;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import ru.sber.database.config.JooqDatabaseConfig;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.database.external_request.Tables;
import ru.sber.transport.database.external_request.enums.OrderState;
import ru.sber.transport.database.external_request.enums.Tariff;
import ru.sber.transport.postgres.EmbeddedPostgres;
import static org.instancio.Select.field;
import ru.sber.transport.request.external.model.State;
import ru.sber.transport.request.external.model.TripOrderHistory;
import ru.sber.transport.request.external.providers.order.history.TripOrderHistoriesDataProviderImpl;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка провайдера истории заявок")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JooqDatabaseConfig.class})
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
class TripOrderHistoriesDataProviderImplTest {

    @Autowired
    DSLContext context;

    private final TripOrderHistoriesDataProviderImpl tripOrderHistories = new TripOrderHistoriesDataProviderImpl() {
        @Override
        public DSLContext context() {
            return context;
        }
    };

    @Test
    @DisplayName("Проверка сохранения истории изменения заявки")
    void test_save() {
        final var testTripOrderHistory = Instancio.of(TestTripOrderHistory.class)
                .set(field(TestTripOrderHistory::getStatus), State.CONFIRMED)
                .create();

        final var passenger = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.COST_CENTER, "1111L22222")
                .returning().fetchSingle();
        final var tripOrderRecord = context.insertInto(Tables.TRIP_ORDER)
                .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PASSENGER_ID, passenger.getId())
                .set(Tables.TRIP_ORDER.DIGIT_ID, 1)
                .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now().plusDays(1))
                .set(Tables.TRIP_ORDER.STATUS, OrderState.values()[1 % OrderState.values().length])
                .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.valueOf(1))
                .set(Tables.TRIP_ORDER.PLANNED_DURATION, DayToSecond.valueOf(Duration.ZERO.plusMinutes(1)))
                .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
                .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) 1)
                .set(Tables.TRIP_ORDER.HASH, "hash")
                .set(Tables.TRIP_ORDER.MODIFIED_AT, OffsetDateTime.now())
                .returning()
                .fetchSingle();

        final var tripOrderHistoryRecord = context.insertInto(Tables.TRIP_ORDER_HISTORY)
                .set(Tables.TRIP_ORDER_HISTORY.ORDER_ID, tripOrderRecord.getId())
                .set(Tables.TRIP_ORDER_HISTORY.STATUS, OrderState.valueOf(testTripOrderHistory.getStatus().name()))
                .set(Tables.TRIP_ORDER_HISTORY.COMMENT, testTripOrderHistory.getComment())
                .set(Tables.TRIP_ORDER_HISTORY.MODIFIED_AT, testTripOrderHistory.getModifiedAt())
                .set(Tables.TRIP_ORDER_HISTORY.REASON, testTripOrderHistory.getReason())
                .returning().fetchSingle();

        tripOrderHistories.save(tripOrderHistoryRecord);

        final var actual = context.fetchOne(Tables.TRIP_ORDER_HISTORY);

        assertThat(actual).isNotNull();
        assertSoftly(it -> {
            it.assertThat(actual.getValue(Tables.TRIP_ORDER_HISTORY.STATUS)).isEqualTo(tripOrderHistoryRecord.getStatus());
            it.assertThat(actual.getValue(Tables.TRIP_ORDER_HISTORY.COMMENT)).isEqualTo(tripOrderHistoryRecord.getComment());
            it.assertThat(actual.getValue(Tables.TRIP_ORDER_HISTORY.REASON)).isEqualTo(tripOrderHistoryRecord.getReason());
            it.assertThat(actual.getValue(Tables.TRIP_ORDER_HISTORY.MODIFIED_AT)).isEqualTo(tripOrderHistoryRecord.getModifiedAt());
        });
    }

    @Test
    @DisplayName("Проверка получения истории изменения заявки")
    void test_get() {
        final var testTripOrderHistory = Instancio.of(TestTripOrderHistory.class)
                .set(field(TestTripOrderHistory::getStatus), State.CONFIRMED)
                .create();

        final var passenger = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.COST_CENTER, "1111L22222")
                .returning().fetchSingle();
        final var tripOrderRecord = context.insertInto(Tables.TRIP_ORDER)
                .set(Tables.TRIP_ORDER.ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PASSENGER_ID, passenger.getId())
                .set(Tables.TRIP_ORDER.DIGIT_ID, 1)
                .set(Tables.TRIP_ORDER.DATE, OffsetDateTime.now().plusDays(1))
                .set(Tables.TRIP_ORDER.STATUS, OrderState.values()[1 % OrderState.values().length])
                .set(Tables.TRIP_ORDER.PURPOSE_ID, UUID.randomUUID())
                .set(Tables.TRIP_ORDER.PLANNED_COST, BigDecimal.valueOf(1))
                .set(Tables.TRIP_ORDER.PLANNED_DURATION, DayToSecond.valueOf(Duration.ZERO.plusMinutes(1)))
                .set(Tables.TRIP_ORDER.TARIFF, Instancio.create(Tariff.class))
                .set(Tables.TRIP_ORDER.PLANNED_DISTANCE, (long) 1)
                .set(Tables.TRIP_ORDER.HASH, "hash")
                .set(Tables.TRIP_ORDER.MODIFIED_AT, OffsetDateTime.now())
                .returning()
                .fetchSingle();

        final var tripOrderHistoryRecord = context.insertInto(Tables.TRIP_ORDER_HISTORY)
                .set(Tables.TRIP_ORDER_HISTORY.ORDER_ID, tripOrderRecord.getId())
                .set(Tables.TRIP_ORDER_HISTORY.STATUS, OrderState.valueOf(testTripOrderHistory.getStatus().name()))
                .set(Tables.TRIP_ORDER_HISTORY.COMMENT, testTripOrderHistory.getComment())
                .set(Tables.TRIP_ORDER_HISTORY.MODIFIED_AT, testTripOrderHistory.getModifiedAt())
                .set(Tables.TRIP_ORDER_HISTORY.REASON, testTripOrderHistory.getReason())
                .returning().fetchSingle();


        final var actualList = tripOrderHistories.get(tripOrderRecord.getId());
        final var actual = actualList.getFirst();

        assertSoftly(it -> {
            it.assertThat(actual.getComment()).isEqualTo(tripOrderHistoryRecord.getComment());
            it.assertThat(actual.getReason()).isEqualTo(tripOrderHistoryRecord.getReason());
            it.assertThat(actual.getModifiedAt()).isEqualTo(tripOrderHistoryRecord.getModifiedAt());
            it.assertThat(actual.getStatus().name()).isEqualTo(tripOrderHistoryRecord.getStatus().name());
        });
    }

    private record TestTripOrderHistory(State getStatus, OffsetDateTime getModifiedAt, String getComment,
                                       String getReason) implements TripOrderHistory {}
}