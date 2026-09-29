package ru.sber.transport.request.external.providers.available_classes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.sber.transport.database.external_request.Tables.EMPLOYEE;
import static ru.sber.transport.database.external_request.Tables.ORGANIZATION_TRANSPORT_TYPE;
import static ru.sber.transport.database.external_request.Tables.POSITION_TRANSPORT_TYPE;
import static ru.sber.transport.database.external_request.Tables.TRANSPORT_TYPES;

import io.qameta.allure.Feature;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.stream.Stream;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
@DisplayName("Проверка провайдера разрешений")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JooqDatabaseConfig.class})
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
class AvailableClassesImplTest {

    @Autowired
    private DSLContext dslContext;

    private final EmployeesProvider employeesProvider = mock(EmployeesProvider.class);
    private final Clock clock = Clock.fixed(OffsetDateTime.parse("2022-01-01T00:00:00+03:00").toInstant(), ZoneOffset.UTC);

    private final AvailableClasses provider = new AvailableClassesImpl(employeesProvider, false, true, clock) {

        @Override
        public DSLContext context() {
            return dslContext;
        }
    };

    public static Stream<Arguments> existsSource() {
        return Stream.of(
                Arguments.of(UUID.randomUUID(), null),
                Arguments.of(null, UUID.randomUUID())
        );
    }

    @ParameterizedTest
    @MethodSource("existsSource")
    @DisplayName("Проверка получения разрешений")
    void test_exists(UUID organizationId, UUID positionId) {
        dslContext.insertInto(TRANSPORT_TYPES)
                .set(TRANSPORT_TYPES.TRANSPORT_TYPE, "YANDEX")
                .execute();

        if (organizationId != null) {
            dslContext.insertInto(ORGANIZATION_TRANSPORT_TYPE)
                    .set(ORGANIZATION_TRANSPORT_TYPE.ORGANIZATION_ID, organizationId)
                    .set(ORGANIZATION_TRANSPORT_TYPE.TRANSPORT_TYPE, "YANDEX")
                    .execute();
        }

        if (positionId != null) {
            dslContext.insertInto(POSITION_TRANSPORT_TYPE)
                    .set(POSITION_TRANSPORT_TYPE.POSITION_ID, positionId)
                    .set(POSITION_TRANSPORT_TYPE.TRANSPORT_TYPE, "YANDEX")
                    .execute();
        }

        assertThat(provider.exists(organizationId, positionId, null)).isTrue();
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
        dslContext.insertInto(ORGANIZATION_TRANSPORT_TYPE)
                .set(ORGANIZATION_TRANSPORT_TYPE.ORGANIZATION_ID, employee.getOrganizationId())
                .set(ORGANIZATION_TRANSPORT_TYPE.TRANSPORT_TYPE, "YANDEX")
                .execute();

        when(employeesProvider.get(employee.getId())).thenReturn(new TestEmployee(employee));
        when(employeesProvider.get(employee2.getId())).thenReturn(new TestEmployee(employee2));

        assertThat(provider.allow(employee.getId())).isTrue();
        assertThat(provider.allow(employee2.getId())).isFalse();
    }

}