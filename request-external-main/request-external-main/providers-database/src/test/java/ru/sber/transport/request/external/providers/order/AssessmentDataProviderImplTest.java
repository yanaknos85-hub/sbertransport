package ru.sber.transport.request.external.providers.order;

import static org.assertj.core.api.Assertions.assertThat;

import io.qameta.allure.Feature;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.val;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.database.config.JooqDatabaseConfig;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.business.providers.CurrentUser;
import ru.sber.transport.database.external_request.Tables;
import ru.sber.transport.database.external_request.enums.AssessmentType;
import ru.sber.transport.database.external_request.enums.OrderState;
import ru.sber.transport.database.external_request.enums.Tariff;
import ru.sber.transport.postgres.EmbeddedPostgres;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка удаления оценки заявки")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JooqDatabaseConfig.class, AssessmentDataProviderImpl.class})
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
@MockitoBean(types = CurrentUser.class)
class AssessmentDataProviderImplTest {

    @Autowired
    private DSLContext context;

    private final AssessmentDataProviderImpl assessmentDataProvider = new AssessmentDataProviderImpl() {
        @Override
        public DSLContext context() {
            return context;
        }
    };

    @Test
    void deleteForRequest() {
        val id = UUID.randomUUID();
        val orderId = UUID.randomUUID();
        val employeeId = UUID.randomUUID();

        final var passenger = context.insertInto(Tables.EMPLOYEE)
            .set(Tables.EMPLOYEE.ID, employeeId)
            .set(Tables.EMPLOYEE.DEPARTMENT_ID, UUID.randomUUID())
            .set(Tables.EMPLOYEE.ORGANIZATION_ID, UUID.randomUUID())
            .set(Tables.EMPLOYEE.COST_CENTER, "1111L22222")
            .returning().fetchSingle();
        context.insertInto(Tables.TRIP_ORDER)
            .set(Tables.TRIP_ORDER.ID, orderId)
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

        val existed = context.insertInto(Tables.ASSESSMENTS)
            .set(Tables.ASSESSMENTS.ID, id)
            .set(Tables.ASSESSMENTS.TYPE, AssessmentType.SERVICE)
            .set(Tables.ASSESSMENTS.RATING, Short.valueOf("5"))
            .set(Tables.ASSESSMENTS.ORDER_ID, orderId)
            .set(Tables.ASSESSMENTS.EMPLOYEE_ID, employeeId)
            .set(Tables.ASSESSMENTS.CREATED_AT, OffsetDateTime.now())
            .returning().fetchSingle();

        // Проверка: оценка существует
        val before = context.selectFrom(Tables.ASSESSMENTS)
            .where(Tables.ASSESSMENTS.ORDER_ID.eq(orderId))
            .fetchInto(Tables.ASSESSMENTS);
        assertThat(before)
            .hasSize(1)
            .extracting("id")
            .contains(id);

        // Удаление
        assessmentDataProvider.deleteForRequest(orderId);

        // Проверка: оценка удалена
        val after = context.selectFrom(Tables.ASSESSMENTS)
            .fetchInto(Tables.ASSESSMENTS);
        assertThat(after).isEmpty();
    }

}