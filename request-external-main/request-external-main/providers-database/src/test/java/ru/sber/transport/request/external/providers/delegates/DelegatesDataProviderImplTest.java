package ru.sber.transport.request.external.providers.delegates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.qameta.allure.Feature;
import java.time.LocalDate;
import java.util.UUID;
import org.instancio.Instancio;
import org.instancio.Select;
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
import ru.sber.transport.business.providers.DelegatesProvider;
import ru.sber.transport.business.providers.EmployeesProvider;
import ru.sber.transport.database.external_request.Tables;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request.external.model.Delegate;
import ru.sber.transport.request.external.providers.model.TestEmployee;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка провайдера делегатов")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JooqDatabaseConfig.class})
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
class DelegatesDataProviderImplTest {

    @Autowired
    DSLContext context;

    private final EmployeesProvider employeesProvider = mock(EmployeesProvider.class);

    private final DelegatesProvider additional = mock(DelegatesProvider.class);

    private final DelegatesProvider delegatesProvider = new DelegatesDataProviderImpl(employeesProvider, additional) {
        @Override
        public DSLContext context() {
            return context;
        }
    };

    @Test
    @DisplayName("Проверка сохранения делегата")
    void test_save() {
        final var testDelegate = Instancio.of(TestDelegate.class)
                .set(Select.field(TestDelegate::active), true)
                .create();

        final var organization = context.insertInto(Tables.ORGANIZATION)
                .set(Tables.ORGANIZATION.ID, UUID.randomUUID())
                .set(Tables.ORGANIZATION.DIGIT_ID, 1)
                .returning().fetchSingle();
        final var department = context.insertInto(Tables.DEPARTMENT)
                .set(Tables.DEPARTMENT.ID, UUID.randomUUID())
                .returning().fetchSingle();
        final var position = context.insertInto(Tables.POSITION)
                .set(Tables.POSITION.ID, UUID.randomUUID())
                .returning().fetchSingle();
        final var supervisor = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, testDelegate.supervisorId())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, department.getId())
                .set(Tables.EMPLOYEE.POSITION_ID, position.getId())
                .returning().fetchSingle();
        final var delegate = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, testDelegate.delegateId())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, department.getId())
                .set(Tables.EMPLOYEE.POSITION_ID, position.getId())
                .returning().fetchSingle();

        when(employeesProvider.get(testDelegate.supervisorId())).thenReturn(new TestEmployee(supervisor));
        when(employeesProvider.get(testDelegate.delegateId())).thenReturn(new TestEmployee(delegate));

        delegatesProvider.save(testDelegate);

        final var actual = context.fetchOne(Tables.DELEGATES);

        assertThat(actual).isNotNull();
        assertSoftly(it -> {
            it.assertThat(actual.getValue(Tables.DELEGATES.DELEGATE_ID)).isEqualTo(testDelegate.delegateId());
            it.assertThat(actual.getValue(Tables.DELEGATES.SUPERVISOR_ID)).isEqualTo(testDelegate.supervisorId());
            it.assertThat(actual.getValue(Tables.DELEGATES.START_DATE)).isEqualTo(testDelegate.startDate());
            it.assertThat(actual.getValue(Tables.DELEGATES.END_DATE)).isEqualTo(testDelegate.endDate());
        });
    }

    @Test
    @DisplayName("Проверка получения делегата")
    void test_get() {

        final var organization = context.insertInto(Tables.ORGANIZATION)
                .set(Tables.ORGANIZATION.ID, UUID.randomUUID())
                .set(Tables.ORGANIZATION.DIGIT_ID, 1)
                .returning().fetchSingle();
        final var department = context.insertInto(Tables.DEPARTMENT)
                .set(Tables.DEPARTMENT.ID, UUID.randomUUID())
                .returning().fetchSingle();
        final var position = context.insertInto(Tables.POSITION)
                .set(Tables.POSITION.ID, UUID.randomUUID())
                .returning().fetchSingle();
        final var supervisor = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, department.getId())
                .set(Tables.EMPLOYEE.POSITION_ID, position.getId())
                .returning().fetchSingle();
        final var delegate = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, department.getId())
                .set(Tables.EMPLOYEE.POSITION_ID, position.getId())
                .returning().fetchSingle();
        final var delegateRecord = context.insertInto(Tables.DELEGATES)
                .set(Tables.DELEGATES.DELEGATE_ID, delegate.getId())
                .set(Tables.DELEGATES.SUPERVISOR_ID, supervisor.getId())
                .set(Tables.DELEGATES.START_DATE, LocalDate.now().minusDays(1))
                .set(Tables.DELEGATES.END_DATE, LocalDate.now().plusDays(1))
                .returning().fetchSingle();

        final var actualList = delegatesProvider.get(delegateRecord.getSupervisorId());
        final var actual = actualList.getFirst();

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(delegate.getId());
            it.assertThat(actual.getDepartmentId()).isNull();
            it.assertThat(actual.getOrganizationId()).isNull();
            it.assertThat(actual.getLastName()).isNull();
            it.assertThat(actual.getFirstName()).isNull();
            it.assertThat(actual.getPatronymic()).isNull();
            it.assertThat(actual.getPositionId()).isNull();
        });
    }

    @Test
    @DisplayName("Проверка получения делегатов")
    void test_get_all() {
        final var organization = context.insertInto(Tables.ORGANIZATION)
                .set(Tables.ORGANIZATION.ID, UUID.randomUUID())
                .set(Tables.ORGANIZATION.DIGIT_ID, 1)
                .returning().fetchSingle();
        final var department = context.insertInto(Tables.DEPARTMENT)
                .set(Tables.DEPARTMENT.ID, UUID.randomUUID())
                .returning().fetchSingle();
        final var position = context.insertInto(Tables.POSITION)
                .set(Tables.POSITION.ID, UUID.randomUUID())
                .returning().fetchSingle();
        final var supervisor = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, department.getId())
                .set(Tables.EMPLOYEE.POSITION_ID, position.getId())
                .returning().fetchSingle();
        final var delegate = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, department.getId())
                .set(Tables.EMPLOYEE.POSITION_ID, position.getId())
                .returning().fetchSingle();
        final var delegateRecord = context.insertInto(Tables.DELEGATES)
                .set(Tables.DELEGATES.DELEGATE_ID, delegate.getId())
                .set(Tables.DELEGATES.SUPERVISOR_ID, supervisor.getId())
                .set(Tables.DELEGATES.START_DATE, LocalDate.now().minusDays(1))
                .set(Tables.DELEGATES.END_DATE, LocalDate.now().plusDays(1))
                .returning().fetchSingle();

        final var actualList = delegatesProvider.getAll();
        final var actual = actualList.getFirst();

        assertSoftly(it -> {
            it.assertThat(actual.supervisorId()).isEqualTo(delegateRecord.getSupervisorId());
            it.assertThat(actual.delegateId()).isEqualTo(delegateRecord.getDelegateId());
            it.assertThat(actual.startDate()).isEqualTo(delegateRecord.getStartDate());
            it.assertThat(actual.endDate()).isEqualTo(delegateRecord.getEndDate());
        });
    }

    @Test
    @DisplayName("Проверка удаления делегата")
    void test_delete() {
        final var organization = context.insertInto(Tables.ORGANIZATION)
                .set(Tables.ORGANIZATION.ID, UUID.randomUUID())
                .set(Tables.ORGANIZATION.DIGIT_ID, 1)
                .returning().fetchSingle();
        final var department = context.insertInto(Tables.DEPARTMENT)
                .set(Tables.DEPARTMENT.ID, UUID.randomUUID())
                .returning().fetchSingle();
        final var position = context.insertInto(Tables.POSITION)
                .set(Tables.POSITION.ID, UUID.randomUUID())
                .returning().fetchSingle();
        final var supervisor = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, department.getId())
                .set(Tables.EMPLOYEE.POSITION_ID, position.getId())
                .returning().fetchSingle();
        final var delegate = context.insertInto(Tables.EMPLOYEE)
                .set(Tables.EMPLOYEE.ID, UUID.randomUUID())
                .set(Tables.EMPLOYEE.ORGANIZATION_ID, organization.getId())
                .set(Tables.EMPLOYEE.DEPARTMENT_ID, department.getId())
                .set(Tables.EMPLOYEE.POSITION_ID, position.getId())
                .returning().fetchSingle();
        final var delegateRecord = context.insertInto(Tables.DELEGATES)
                .set(Tables.DELEGATES.DELEGATE_ID, delegate.getId())
                .set(Tables.DELEGATES.SUPERVISOR_ID, supervisor.getId())
                .set(Tables.DELEGATES.START_DATE, LocalDate.now().minusDays(1))
                .set(Tables.DELEGATES.END_DATE, LocalDate.now().plusDays(1))
                .returning().fetchSingle();
        final var testDelegate = Instancio.of(TestDelegate.class)
                .set(Select.field(TestDelegate::active), false)
                .set(Select.field(TestDelegate::delegateId), delegate.getId())
                .set(Select.field(TestDelegate::supervisorId), supervisor.getId())
                .set(Select.field(TestDelegate::startDate), delegateRecord.getStartDate())
                .create();

        delegatesProvider.save(testDelegate);

        assertThat(context.fetchCount(Tables.DELEGATES)).isZero();
    }

    private record TestDelegate(UUID id, UUID delegateId, UUID supervisorId, LocalDate startDate,
                                LocalDate endDate, boolean active) implements Delegate {
    }

}