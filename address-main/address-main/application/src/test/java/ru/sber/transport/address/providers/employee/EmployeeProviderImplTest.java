package ru.sber.transport.address.providers.employee;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.config.JooqDatabaseConfig;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.address.business.provider.EmployeeProvider;
import ru.sber.transport.address.providers.employee.mappers.EmployeeDatabaseMapperImpl;
import ru.sber.transport.database.addresses.tables.Employee;
import ru.sber.transport.database.addresses.tables.records.EmployeeRecord;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.scripting.ScriptUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@JooqTest
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@UnitTest
@IsolatedTest
@Feature("app_platform_address")
@DisplayName("Проверка провайдера сотрудников")
@MockBean(ScriptUtils.class)
@AutoConfigurationPackage
@Transactional
@Import({JooqDatabaseConfig.class, EmployeeProviderImpl.class, EmployeeDatabaseMapperImpl.class})
@ContextConfiguration(classes = {JooqDatabaseConfig.class, EmployeeProviderImpl.class, EmployeeDatabaseMapperImpl.class})
@ActiveProfiles("test")
class EmployeeProviderImplTest {

    @Autowired
    private DSLContext context;

    @Autowired
    private EmployeeProvider provider;

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    @DisplayName("Получение данных сотрудника")
    void test_get() {
        var record = new EmployeeRecord(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        context.insertInto(Employee.EMPLOYEE).set(record).execute();

        var saved = provider.get(record.getId());

        assertSoftly(soft -> {
            soft.assertThat(saved).isPresent();
            soft.assertThat(saved.get().id()).isEqualTo(record.getId());
            soft.assertThat(saved.get().userId()).isEqualTo(record.getUserId());
            soft.assertThat(saved.get().organizationId()).isEqualTo(record.getOrganizationId());
        });
    }

    @Test
    @DisplayName("Сохранение данных сотрудника")
    void test_save() {
        var source = Instancio.create(ru.sber.transport.address.business.model.Employee.class);

        provider.save(source);

        assertThat(context.fetchCount(Employee.EMPLOYEE)).isEqualTo(1);
        var db = context.selectFrom(Employee.EMPLOYEE).fetchOne();

        assertSoftly(soft -> {
            soft.assertThat(db).isNotNull();
            assert db != null;
            soft.assertThat(db.getId()).isEqualTo(source.id());
            soft.assertThat(db.getUserId()).isEqualTo(source.userId());
            soft.assertThat(db.getOrganizationId()).isEqualTo(source.organizationId());
        });
    }

}