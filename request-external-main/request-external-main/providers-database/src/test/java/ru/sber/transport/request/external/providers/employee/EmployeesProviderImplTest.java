package ru.sber.transport.request.external.providers.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.qameta.allure.Feature;
import java.util.UUID;
import org.instancio.Instancio;
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
import ru.sber.transport.business.providers.DepartmentsProvider;
import ru.sber.transport.business.providers.EmployeesProvider;
import ru.sber.transport.business.providers.OrganizationsProvider;
import ru.sber.transport.database.external_request.Tables;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request.external.model.DepartmentStatus;
import ru.sber.transport.request.external.providers.employees.EmployeeListener;
import ru.sber.transport.request.external.providers.employees.EmployeesDataProviderImpl;
import ru.sber.transport.request.external.providers.model.TestDepartment;
import ru.sber.transport.request.external.providers.model.TestEmployee;
import ru.sber.transport.request.external.providers.model.TestOrganization;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка провайдера сотрудников")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JooqDatabaseConfig.class, EmployeeListener.class})
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
class EmployeesProviderImplTest {

    @Autowired
    private DSLContext context;

    private final EmployeesProvider additional = mock(EmployeesProvider.class);

    private final OrganizationsProvider organizationsProvider = mock(OrganizationsProvider.class);

    private final DepartmentsProvider departmentsProvider = mock(DepartmentsProvider.class);

    private final EmployeesProvider employeesProvider = new EmployeesDataProviderImpl(organizationsProvider, new SimpleObjectProvider(
        departmentsProvider), additional) {

        @Override
        public DSLContext context() {
            return context;
        }
    };

    @Test
    @DisplayName("Проверка сохранения сотрудника")
    void test_save() {
        final var source = new TestEmployee(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instancio.create(String.class),
                Instancio.create(String.class),
                Instancio.create(String.class),
                Instancio.create(String.class),
                Instancio.create(String.class)
        );
        final var organization = new TestOrganization(
                source.getOrganizationId(),
                Instancio.create(Integer.class),
                Instancio.createList(String.class)
        );
        final var department = new TestDepartment(
                source.getDepartmentId(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instancio.create(String.class),
                Instancio.create(DepartmentStatus.class),
                UUID.randomUUID(),
                1
        );

        when(organizationsProvider.get(source.getOrganizationId())).thenReturn(organization);
        when(departmentsProvider.get(source.getDepartmentId())).thenReturn(department);

        employeesProvider.save(source);

        assertThat(context.fetchCount(Tables.EMPLOYEE)).isEqualTo(1);

        final var actual = context.selectFrom(Tables.EMPLOYEE).fetchSingle();
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(source.getId());
        assertThat(actual.getDepartmentId()).isEqualTo(source.getDepartmentId());
        assertThat(actual.getOrganizationId()).isEqualTo(source.getOrganizationId());
        assertThat(actual.getLastName()).isEqualTo(source.getLastName());
        assertThat(actual.getFirstName()).isEqualTo(source.getFirstName());
        assertThat(actual.getPatronymic()).isEqualTo(source.getPatronymic());
        assertThat(actual.getPositionId()).isEqualTo(source.getPositionId());
        assertThat(actual.getPersonnelNumber()).isEqualTo(source.getPersonnelNumber());
        assertThat(actual.getCostCenter()).isEqualTo(source.getCostCenter());
    }

    @Test
    @DisplayName("Проверка получения сотрудника")
    void test_get() {
        final var id = UUID.randomUUID();
        final var saved = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, id)
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.POSITION_ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.LAST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.FIRST_NAME, Instancio.create(String.class))
                .set(Tables.EMPLOYEE.PATRONYMIC, Instancio.create(String.class))
                .returning()
                .fetchSingle();

        final var actual = employeesProvider.get(id);
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getOrganizationId()).isEqualTo(saved.getOrganizationId());
        assertThat(actual.getDepartmentId()).isEqualTo(saved.getDepartmentId());
        assertThat(actual.getLastName()).isEqualTo(saved.getLastName());
        assertThat(actual.getFirstName()).isEqualTo(saved.getFirstName());
        assertThat(actual.getPatronymic()).isEqualTo(saved.getPatronymic());
        assertThat(actual.getPositionId()).isEqualTo(saved.getPositionId());
        assertThat(actual.getPersonnelNumber()).isEqualTo(saved.getPersonnelNumber());
    }

    @Test
    @DisplayName("Проверка получения сотрудника из внешнего источника")
    void test_externalRequest() {
        final var id = UUID.randomUUID();

        final var employee = new TestEmployee(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instancio.create(String.class),
                Instancio.create(String.class),
                Instancio.create(String.class),
                Instancio.create(String.class),
                Instancio.create(String.class)
        );
        final var organization = new TestOrganization(
                employee.getOrganizationId(),
                Instancio.create(Integer.class),
                Instancio.createList(String.class)
        );
        final var department = new TestDepartment(
                employee.getDepartmentId(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instancio.create(String.class),
                Instancio.create(DepartmentStatus.class),
                UUID.randomUUID(),
                1
        );
        when(additional.get(id)).thenReturn(employee);
        when(organizationsProvider.get(employee.getOrganizationId())).thenReturn(organization);
        when(departmentsProvider.get(employee.getDepartmentId())).thenReturn(department);

        final var actual = employeesProvider.get(id);
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(employee.getId());
        assertThat(actual.getOrganizationId()).isEqualTo(employee.getOrganizationId());
        assertThat(actual.getDepartmentId()).isEqualTo(employee.getDepartmentId());
        assertThat(actual.getLastName()).isEqualTo(employee.getLastName());
        assertThat(actual.getFirstName()).isEqualTo(employee.getFirstName());
        assertThat(actual.getPatronymic()).isEqualTo(employee.getPatronymic());
        assertThat(actual.getPositionId()).isEqualTo(employee.getPositionId());
        assertThat(actual.getPersonnelNumber()).isEqualTo(employee.getPersonnelNumber());

        assertThat(context.fetchCount(Tables.EMPLOYEE)).isEqualTo(1);

        final var actualDb = context.selectFrom(Tables.EMPLOYEE).fetchSingle();
        assertThat(actualDb).isNotNull();
        assertThat(actualDb.getId()).isEqualTo(employee.getId());
        assertThat(actualDb.getOrganizationId()).isEqualTo(employee.getOrganizationId());
        assertThat(actualDb.getDepartmentId()).isEqualTo(employee.getDepartmentId());
        assertThat(actualDb.getLastName()).isEqualTo(employee.getLastName());
        assertThat(actualDb.getFirstName()).isEqualTo(employee.getFirstName());
        assertThat(actualDb.getPatronymic()).isEqualTo(employee.getPatronymic());
        assertThat(actualDb.getPositionId()).isEqualTo(employee.getPositionId());
        assertThat(actualDb.getPersonnelNumber()).isEqualTo(employee.getPersonnelNumber());
        assertThat(actualDb.getCostCenter()).isEqualTo(employee.getCostCenter());
    }

}