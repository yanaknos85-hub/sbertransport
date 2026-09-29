package ru.sber.transport.request.external.providers.department;

import static ru.sber.transport.database.external_request.Tables.DEPARTMENT;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jooq.impl.DSL;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.business.providers.DepartmentsProvider;
import ru.sber.transport.business.providers.EmployeesProvider;
import ru.sber.transport.database.external_request.Tables;
import ru.sber.transport.database.external_request.tables.records.DepartmentRecord;
import ru.sber.transport.request.external.model.Department;
import ru.sber.transport.request.external.model.DepartmentStatus;

/**
 * Реализация провайдера сотрудников
 */
@Slf4j
public class DepartmentsDataProviderImpl implements DepartmentsProvider, JooqRepository<ru.sber.transport.database.external_request.tables.Department, DepartmentRecord, UUID> {

    private final DepartmentsProvider departmentsProviderGrpcProvider;
    private final EmployeesProvider employeesProvider;

    public DepartmentsDataProviderImpl(DepartmentsProvider departmentsProviderGrpcProvider, EmployeesProvider employeesProvider, Executor departmentConsistencyCheckTaskExecutor) {
        this.departmentsProviderGrpcProvider = departmentsProviderGrpcProvider;
        this.employeesProvider = employeesProvider;

        CompletableFuture.runAsync(() -> {
                    log.info("Check consistency for departments");
                    final var inconsistencyDepartmentsIds = context()
                            .select(DEPARTMENT.ID)
                            .from(table())
                            .where(table().NAME.isNull()
                                    .or(table().ORGANIZATION_ID.isNull())
                                    .or(table().STATUS.isNull()))
                            .fetchInto(UUID.class);
                    inconsistencyDepartmentsIds.forEach(id -> saveOnCheckConsistency(departmentsProviderGrpcProvider.get(id)));
                }, departmentConsistencyCheckTaskExecutor)
                .exceptionally(ex -> {
                    log.error("Error on check consistency for departments: {}", ex.getMessage(), ex);
                    return null;
                });
    }

    @Override
    public Department save(Department source) {
        if (source == null) {
            return null;
        }
        final var departmentRecord = findById(source.getId()).orElseGet(DepartmentRecord::new);
        departmentRecord.setId(source.getId());
        departmentRecord.setHeadId(source.getHeadId());
        departmentRecord.setParentId(source.getParentId());
        departmentRecord.setName(source.getName());
        departmentRecord.setStatus(DepartmentStatusMapper.toJooq(source.getStatus()));
        departmentRecord.setOrganizationId(source.getOrganizationId());
        departmentRecord.setLevel(this.calculateLevel(source.getParentId()));

        final var saved = save(departmentRecord);
        if (departmentRecord.getHeadId() != null) {
            CompletableFuture.runAsync(() -> employeesProvider.get(departmentRecord.getHeadId()));
        }
        if (departmentRecord.getParentId() != null) {
            CompletableFuture.runAsync(() -> departmentsProviderGrpcProvider.get(departmentRecord.getParentId()));
        }
        return createDepartment(saved);
    }

    @Override
    public Department get(UUID id) {
        return findById(id)
                .map(this::createDepartment)
                .orElseGet(() -> save(departmentsProviderGrpcProvider.get(id)));
    }

    @Override
    public ru.sber.transport.database.external_request.tables.Department table() {
        return Tables.DEPARTMENT;
    }

    @Override
    public Set<UUID> getChildrenDepartments(Set<UUID> ids) {
        log.debug("Get children for {}", ids);
        final var recursiveDepartment = DSL.name("recursive_department");
        final var recursiveDepartmentIdField = DSL.field(recursiveDepartment.append(DEPARTMENT.ID.getUnqualifiedName()), UUID.class);

        final var children = context().withRecursive(recursiveDepartment).as(
                        DSL.select(DSL.asterisk()).from(DEPARTMENT).where(DEPARTMENT.ID.in(ids))
                                .union(DSL.select(DEPARTMENT.asterisk()).from(DEPARTMENT)
                                        .innerJoin(recursiveDepartment).on(recursiveDepartmentIdField.eq(DEPARTMENT.PARENT_ID)))
                )
                .select(recursiveDepartmentIdField).from(recursiveDepartment)
                .fetchInto(UUID.class);
        log.debug("Get children for {} result {}", ids, children);
        return new HashSet<>(children);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Department> findDepartmentsByOrganizationIdAndStatusAndLevelBetween(UUID organizationId,
                                                                                    DepartmentStatus status,
                                                                                    int levelFrom, int levelTo) {
        log.debug("Find departments by organizationId={}, status={}, levelFrom={}, levelTo={}", organizationId, status, levelFrom, levelTo);
        final var departments = context()
                .selectFrom(table())
                .where(table().
                        ORGANIZATION_ID.eq(organizationId)
                        .and(table().STATUS.eq(DepartmentStatusMapper.toJooq(status)))
                        .and(table().LEVEL.between(levelFrom, levelTo))
                )
                .fetch(this::createDepartment);
        log.debug("Find departments by organizationId={}, status={}, levelFrom={}, levelTo={} size={}", organizationId, status, levelFrom, levelTo, departments.size());
        return departments;
    }

    private void saveOnCheckConsistency(Department source) {
        if (source == null) {
            return;
        }
        findById(source.getId()).ifPresent(it -> {
            it.setName(source.getName());
            it.setStatus(DepartmentStatusMapper.toJooq(source.getStatus()));
            it.setOrganizationId(source.getOrganizationId());
            save(it);
        });
    }

    private Integer calculateLevel(UUID parentId) {
        if (parentId == null) {
            return 1;
        }

        Integer parentLevel = context()
                .select(DEPARTMENT.LEVEL)
                .from(DEPARTMENT)
                .where(DEPARTMENT.ID.eq(parentId))
                .fetchOptional(DEPARTMENT.LEVEL).orElse(0);

        return parentLevel + 1;
    }

    private Department createDepartment(@NotNull DepartmentRecord source) {
        return new Department() {
            @Override
            public UUID getId() {
                return source.getId();
            }

            @Override
            public UUID getHeadId() {
                return source.getHeadId();
            }

            @Override
            public UUID getParentId() {
                return source.getParentId();
            }

            @Override
            public String getName() {
                return source.getName();
            }

            @Override
            public DepartmentStatus getStatus() {
                return DepartmentStatus.valueOf(source.getStatus().name());
            }

            @Override
            public UUID getOrganizationId() {
                return source.getOrganizationId();
            }

            @Override
            public Integer getLevel() {
                return source.getLevel();
            }
        };
    }
}
