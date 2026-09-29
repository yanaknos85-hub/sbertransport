package ru.sber.transport.request.external.providers.available_classes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.sber.transport.database.external_request.Tables.DEPARTMENT;
import static ru.sber.transport.database.external_request.Tables.DEPARTMENT_TRANSPORT_TYPE;
import static ru.sber.transport.database.external_request.Tables.EMPLOYEE;
import static ru.sber.transport.database.external_request.Tables.TRANSPORT_TYPES;

import io.qameta.allure.Feature;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.jooq.DSLContext;
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
import ru.sber.transport.business.providers.AvailableClasses;
import ru.sber.transport.business.providers.EmployeesProvider;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request.external.providers.model.TestEmployee;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка провайдера разрешений. MVP")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JooqDatabaseConfig.class})
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
class AvailableClassesMvpImplTest {

    @Autowired
    private DSLContext dslContext;

    private final EmployeesProvider employeesProvider = mock(EmployeesProvider.class);
    private final Clock clock = Clock.fixed(OffsetDateTime.parse("2022-01-01T00:00:00+03:00").toInstant(), ZoneOffset.UTC);

    private final AvailableClasses provider = new AvailableClassesImpl(employeesProvider, true, true, clock) {

        @Override
        public DSLContext context() {
            return dslContext;
        }
    };

    @Test
    @DisplayName("Проверка получения разрешений")
    void test_exists() {

        final var parent = dslContext.insertInto(DEPARTMENT)
                .set(DEPARTMENT.ID, UUID.randomUUID())
                .returning().fetchSingle();

        final var allowed = dslContext.insertInto(DEPARTMENT)
                .set(DEPARTMENT.ID, UUID.randomUUID())
                .set(DEPARTMENT.PARENT_ID, parent.getId())
                .returning().fetchSingle();

        final var child = dslContext.insertInto(DEPARTMENT)
                .set(DEPARTMENT.ID, UUID.randomUUID())
                .set(DEPARTMENT.PARENT_ID, allowed.getId())
                .returning().fetchSingle();

        dslContext.insertInto(TRANSPORT_TYPES)
                .set(TRANSPORT_TYPES.TRANSPORT_TYPE, "YANDEX")
                .execute();

        dslContext.insertInto(DEPARTMENT_TRANSPORT_TYPE)
                .set(DEPARTMENT_TRANSPORT_TYPE.DEPARTMENT_ID, allowed.getId())
                .set(DEPARTMENT_TRANSPORT_TYPE.TRANSPORT_TYPE, "YANDEX")
                .execute();

        assertThat(provider.exists(null, null, allowed.getId())).isTrue();
        assertThat(provider.exists(null, null, child.getId())).isTrue();
        assertThat(provider.exists(null, null, parent.getId())).isFalse();
    }

    @Test
    @DisplayName("Проверка разрешения для пользователя")
    void test_allow() {
        final var employee = dslContext.insertInto(EMPLOYEE)
                .set(EMPLOYEE.ID, UUID.randomUUID())
                .set(EMPLOYEE.ORGANIZATION_ID, UUID.randomUUID())
                .set(EMPLOYEE.POSITION_ID, UUID.randomUUID())
                .set(EMPLOYEE.DEPARTMENT_ID, UUID.randomUUID())
                .returning().fetchSingle();
        final var employee2 = dslContext.insertInto(EMPLOYEE)
                .set(EMPLOYEE.ID, UUID.randomUUID())
                .set(EMPLOYEE.ORGANIZATION_ID, UUID.randomUUID())
                .set(EMPLOYEE.POSITION_ID, UUID.randomUUID())
                .set(EMPLOYEE.DEPARTMENT_ID, UUID.randomUUID())
                .returning().fetchSingle();
        dslContext.insertInto(TRANSPORT_TYPES)
                .set(TRANSPORT_TYPES.TRANSPORT_TYPE, "YANDEX")
                .execute();
        dslContext.insertInto(DEPARTMENT)
                .set(DEPARTMENT.ID, employee.getDepartmentId())
                .execute();
        dslContext.insertInto(DEPARTMENT_TRANSPORT_TYPE)
                .set(DEPARTMENT_TRANSPORT_TYPE.DEPARTMENT_ID, employee.getDepartmentId())
                .set(DEPARTMENT_TRANSPORT_TYPE.TRANSPORT_TYPE, "YANDEX")
                .execute();

        when(employeesProvider.get(employee.getId())).thenReturn(new TestEmployee(employee));
        when(employeesProvider.get(employee2.getId())).thenReturn(new TestEmployee(employee2));

        assertThat(provider.allow(employee.getId())).isTrue();
        assertThat(provider.allow(employee2.getId())).isFalse();
    }

}