package ru.sber.transport.request.external.providers.department;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.sber.transport.database.external_request.tables.Department.DEPARTMENT;

import io.qameta.allure.Feature;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.instancio.Instancio;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import ru.sber.transport.database.external_request.Tables;
import ru.sber.transport.database.external_request.tables.records.DepartmentRecord;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request.external.model.DepartmentStatus;
import ru.sber.transport.request.external.providers.model.TestDepartment;
import ru.sber.transport.request.external.providers.model.TestEmployee;

@Slf4j
@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка провайдера подразделений")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JooqDatabaseConfig.class})
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
class DepartmentsProviderImplTest {
    @Autowired
    private DSLContext context;

    private final DepartmentsProvider additional = mock(DepartmentsProvider.class);

    private final EmployeesProvider employeesProvider = mock(EmployeesProvider.class);

    private static final Executor DIRECT_EXECUTOR = Runnable::run;

    private final DepartmentsProvider departmentsProvider = new DepartmentsDataProviderImpl(additional,
        employeesProvider, DIRECT_EXECUTOR) {
        @Override
        public DSLContext context() {
            return DepartmentsProviderImplTest.this.context;
        }
    };

    @Test
    void test_checkConsistency() {
        final var id = UUID.randomUUID();
        final var orgId = UUID.randomUUID();
        final var headId = UUID.randomUUID();
        final var parentId = UUID.randomUUID();
        final var status = ru.sber.transport.database.external_request.enums.DepartmentStatus.ACTIVE;
        final var level = 1;
        final var name = "Name";

        context.insertInto(Tables.DEPARTMENT)
                .set(Tables.DEPARTMENT.ID, id)
                .set(Tables.DEPARTMENT.HEAD_ID, headId)
                .set(Tables.DEPARTMENT.PARENT_ID, parentId)
                .set(Tables.DEPARTMENT.STATUS, status)
                .set(Tables.DEPARTMENT.ORGANIZATION_ID, orgId)
                .set(Tables.DEPARTMENT.LEVEL, level)
                .execute();


        var deptFromGrpc = new TestDepartment(id, headId, parentId, name, DepartmentStatus.ACTIVE, orgId, level);

        when(additional.get(any(UUID.class))).thenReturn(deptFromGrpc);

        new DepartmentsDataProviderImpl(additional, employeesProvider, DIRECT_EXECUTOR) {
            @Override
            public DSLContext context() {
                return DepartmentsProviderImplTest.this.context;
            }
        };

        DepartmentRecord result = context.selectFrom(Tables.DEPARTMENT)
                .where(Tables.DEPARTMENT.ID.eq(id)).fetchOne();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getHeadId()).isEqualTo(headId);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getOrganizationId()).isEqualTo(orgId);
        assertThat(result.getParentId()).isEqualTo(parentId);
        assertThat(result.getStatus()).isEqualTo(status);
        assertThat(result.getLevel()).isEqualTo(level);
    }

    @Test
    @DisplayName("Проверка сохранения подразделения")
    void test_save() {
        final var source = new TestDepartment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instancio.create(String.class),
                Instancio.create(DepartmentStatus.class),
                UUID.randomUUID(),
                1
                );
        final var employee = new TestEmployee(
                source.getHeadId(),
                source.getId(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instancio.create(String.class),
                Instancio.create(String.class),
                Instancio.create(String.class),
                Instancio.create(String.class),
                Instancio.create(String.class)
        );

        when(employeesProvider.get(source.getHeadId())).thenReturn(employee);

        departmentsProvider.save(source);

        assertThat(context.fetchCount(Tables.DEPARTMENT)).isEqualTo(1);

        final var actual = context.selectFrom(Tables.DEPARTMENT).fetchSingle();
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(source.getId());
        assertThat(actual.getHeadId()).isEqualTo(source.getHeadId());
        assertThat(actual.getParentId()).isEqualTo(source.getParentId());
        assertThat(actual.getName()).isEqualTo(source.getName());
        assertThat(actual.getStatus().name()).isEqualTo(source.getStatus().name());
        assertThat(actual.getOrganizationId()).isEqualTo(source.getOrganizationId());
        assertThat(actual.getLevel()).isEqualTo(source.getLevel());
    }

    @Test
    @DisplayName("Проверка обновления подразделения")
    void test_update() {
        final var source = new TestDepartment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instancio.create(String.class),
                DepartmentStatus.ACTIVE,
                UUID.randomUUID(),
                1
        );
        final var employee = new TestEmployee(
                source.getHeadId(),
                source.getId(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instancio.create(String.class),
                Instancio.create(String.class),
                Instancio.create(String.class),
                Instancio.create(String.class),
                Instancio.create(String.class)
        );

        when(employeesProvider.get(source.getHeadId())).thenReturn(employee);

        departmentsProvider.save(source);
        final var updatedSource = new TestDepartment(
                source.getId(),
                source.getHeadId(),
                source.getParentId(),
                Instancio.create(String.class),
                DepartmentStatus.INACTIVE,
                source.getOrganizationId(),
                1
        );
        departmentsProvider.save(updatedSource);

        assertThat(context.fetchCount(Tables.DEPARTMENT)).isEqualTo(1);

        final var actual = context.selectFrom(Tables.DEPARTMENT).fetchSingle();
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(source.getId());
        assertThat(actual.getHeadId()).isEqualTo(source.getHeadId());
        assertThat(actual.getParentId()).isEqualTo(source.getParentId());
        assertThat(actual.getName()).isEqualTo(updatedSource.getName());
        assertThat(actual.getStatus().name()).isEqualTo(updatedSource.getStatus().name());
        assertThat(actual.getOrganizationId()).isEqualTo(source.getOrganizationId());
        assertThat(actual.getLevel()).isEqualTo(source.getLevel());
    }

    @Test
    @DisplayName("Проверка сохранения подразделения с null")
    void test_save_when_sourse_is_null() {
        assertThat(departmentsProvider.save(null)).isNull();
    }

    @Test
    @DisplayName("Проверка сохранения подразделения без руководителя")
    void test_save_no_head() {
        final var source = new TestDepartment(
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                Instancio.create(String.class),
                Instancio.create(DepartmentStatus.class),
                UUID.randomUUID(),
                1
        );

        departmentsProvider.save(source);

        assertThat(context.fetchCount(Tables.DEPARTMENT)).isEqualTo(1);

        final var actual = context.selectFrom(Tables.DEPARTMENT).fetchSingle();
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(source.getId());
        assertThat(actual.getHeadId()).isNull();
    }

    @Test
    @DisplayName("Проверка получения подразделения")
    void test_get() {
        final var id = UUID.randomUUID();
        final var headId = UUID.randomUUID();
        context.insertInto(Tables.DEPARTMENT)
                .set(Tables.DEPARTMENT.ID, id)
                .set(Tables.DEPARTMENT.HEAD_ID, headId)
                .execute();

        final var actual = departmentsProvider.get(id);
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getHeadId()).isEqualTo(headId);
    }

    @Test
    @DisplayName("Проверка получения подразделения из внешнего источника")
    void test_externalRequest() {
        final var id = UUID.randomUUID();

        final var department = new TestDepartment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instancio.create(String.class),
                Instancio.create(DepartmentStatus.class),
                UUID.randomUUID(),
                1
        );
        final var employee = new TestEmployee(
                department.getHeadId(),
                department.getId(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instancio.create(String.class),
                Instancio.create(String.class),
                Instancio.create(String.class),
                Instancio.create(String.class),
                Instancio.create(String.class)
        );
        when(additional.get(id)).thenReturn(department);
        when(employeesProvider.get(department.getHeadId())).thenReturn(employee);

        final var actual = departmentsProvider.get(id);
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(department.getId());
        assertThat(actual.getHeadId()).isEqualTo(department.getHeadId());

        assertThat(context.fetchCount(Tables.DEPARTMENT)).isEqualTo(1);

        final var actualDb = context.selectFrom(Tables.DEPARTMENT).fetchSingle();
        assertThat(actualDb).isNotNull();
        assertThat(actualDb.getId()).isEqualTo(department.getId());
        assertThat(actualDb.getHeadId()).isEqualTo(department.getHeadId());
    }

    @Test
    @DisplayName("Проверка получения подразделения из внешнего источника. Нет руководителя")
    void test_externalRequest_noHead() {
        final var id = UUID.randomUUID();

        final var department = new TestDepartment(
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                Instancio.create(String.class),
                Instancio.create(DepartmentStatus.class),
                UUID.randomUUID(),
                1
        );
        when(additional.get(id)).thenReturn(department);

        final var actual = departmentsProvider.get(id);
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(department.getId());
        assertThat(actual.getHeadId()).isNull();

        assertThat(context.fetchCount(Tables.DEPARTMENT)).isEqualTo(1);

        final var actualDb = context.selectFrom(Tables.DEPARTMENT).fetchSingle();
        assertThat(actualDb).isNotNull();
        assertThat(actualDb.getId()).isEqualTo(department.getId());
        assertThat(actualDb.getHeadId()).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6})
    @DisplayName("Получение дочерних подразделений по ID департамента. Всего 6 уровней")
    void test_getChildrenDepartments_by_level(int departmentsLevel) {
        final var hierarchyDepth = 6;
        final var departmentIds = createDepartmentHierarchy();

        final var startId = departmentIds.get(departmentsLevel - 1);

        final var childrenDepartments = departmentsProvider.getChildrenDepartments(Set.of(startId));

        final var expectedSize = hierarchyDepth - departmentsLevel + 1;
        assertThat(childrenDepartments).hasSize(expectedSize);

        List<UUID> expectedSubList = departmentIds.subList(departmentsLevel - 1, departmentIds.size());
        assertThat(childrenDepartments).containsExactlyInAnyOrderElementsOf(expectedSubList);
    }

    @Test
    @DisplayName("Поиск подразделений по ID организации, статусу и уровню")
    void test_findDepartmentsByOrganizationIdAndStatusAndLevelBetween() {

        final var organizationId = UUID.randomUUID();
        final var expectedDepartments = new ArrayList<DepartmentRecord>(10);

        for (int i = 0; i < 10; i++) {
            var department = context
                    .insertInto(Tables.DEPARTMENT)
                    .set(Tables.DEPARTMENT.ID, UUID.randomUUID())
                    .set(Tables.DEPARTMENT.HEAD_ID, UUID.randomUUID())
                    .set(Tables.DEPARTMENT.NAME, "Отделение-" + i)
                    .set(Tables.DEPARTMENT.ORGANIZATION_ID, organizationId)
                    .set(Tables.DEPARTMENT.STATUS, ru.sber.transport.database.external_request.enums.DepartmentStatus.ACTIVE)
                    .set(Tables.DEPARTMENT.LEVEL, 1)
                    .returning();
            expectedDepartments.add(department.fetchOne());
        }

        final var result = departmentsProvider
                .findDepartmentsByOrganizationIdAndStatusAndLevelBetween(organizationId, DepartmentStatus.ACTIVE, 1, 6);

        assertThat(result).hasSize(expectedDepartments.size());
    }

    private List<UUID> createDepartmentHierarchy() {
        UUID rootId = UUID.randomUUID();
        context.insertInto(Tables.DEPARTMENT)
                .set(DEPARTMENT.ID, rootId)
                .execute();

        List<UUID> allIds = new ArrayList<>(6);
        allIds.add(rootId);

        UUID currentId = rootId;
        for (int i = 0; i < 6 - 1; i++) {
            UUID nextId = UUID.randomUUID();
            context.insertInto(Tables.DEPARTMENT)
                    .set(DEPARTMENT.ID, nextId)
                    .set(DEPARTMENT.PARENT_ID, currentId)
                    .execute();

            allIds.add(nextId);
            currentId = nextId;
        }

        return allIds;
    }
}