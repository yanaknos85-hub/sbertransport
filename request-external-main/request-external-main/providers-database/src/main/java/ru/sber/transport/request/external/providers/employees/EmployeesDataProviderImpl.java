package ru.sber.transport.request.external.providers.employees;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.ObjectProvider;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.business.providers.DepartmentsProvider;
import ru.sber.transport.business.providers.EmployeesProvider;
import ru.sber.transport.business.providers.OrganizationsProvider;
import ru.sber.transport.database.external_request.Tables;
import ru.sber.transport.database.external_request.tables.records.EmployeeRecord;
import ru.sber.transport.request.external.model.Employee;

/**
 * Реализация провайдера сотрудников
 */
@Slf4j
public class EmployeesDataProviderImpl implements EmployeesProvider, JooqRepository<ru.sber.transport.database.external_request.tables.Employee, EmployeeRecord, UUID> {

    private final OrganizationsProvider organizationsProvider;

    private final ObjectProvider<DepartmentsProvider> departments;

    private final EmployeesProvider employeesProvider;

    public EmployeesDataProviderImpl(OrganizationsProvider organizationsProvider, ObjectProvider<DepartmentsProvider> departments, EmployeesProvider employeesProvider) {
        this.organizationsProvider = organizationsProvider;
        this.departments = departments;
        this.employeesProvider = employeesProvider;
        CompletableFuture.runAsync(() -> {
            log.info("Check consistency");
            Optional<EmployeeRecord> check;
            do {
                check = context().selectFrom(table()).where(table().PERSONNEL_NUMBER.isNull()).limit(1).fetchOptional();
                check.map(EmployeeRecord::getId).ifPresent(it -> {
                    log.info("Fix inconsistency for {}", it);
                    save(employeesProvider.get(it));
                });
            } while (check.isPresent());
        });
    }

    @Override
    public Employee save(Employee source) {
        if (source == null) {
            return null;
        }

        var saved = context().insertInto(table())
                .set(table().ID, source.getId())
                .set(table().DEPARTMENT_ID, source.getDepartmentId())
                .set(table().POSITION_ID, source.getPositionId())
                .set(table().ORGANIZATION_ID, source.getOrganizationId())
                .set(table().LAST_NAME, source.getLastName())
                .set(table().FIRST_NAME, source.getFirstName())
                .set(table().PATRONYMIC, source.getPatronymic())
                .set(table().PERSONNEL_NUMBER, source.getPersonnelNumber())
                .set(table().COST_CENTER, source.getCostCenter())
                .onConflict(table().ID)
                .doUpdate()
                .set(table().DEPARTMENT_ID, DSL.excluded(table().DEPARTMENT_ID))
                .set(table().POSITION_ID, DSL.excluded(table().POSITION_ID))
                .set(table().ORGANIZATION_ID, DSL.excluded(table().ORGANIZATION_ID))
                .set(table().LAST_NAME, DSL.excluded(table().LAST_NAME))
                .set(table().FIRST_NAME, DSL.excluded(table().FIRST_NAME))
                .set(table().PATRONYMIC, DSL.excluded(table().PATRONYMIC))
                .set(table().PERSONNEL_NUMBER, DSL.excluded(table().PERSONNEL_NUMBER))
                .set(table().COST_CENTER, DSL.excluded(table().COST_CENTER))
                .returning()
                .fetchOne();

        if (saved == null) {
            saved = getById(source.getId());
        }

        CompletableFuture.runAsync(() -> organizationsProvider.get(source.getOrganizationId()));
        CompletableFuture.runAsync(() -> departments.getObject().get(source.getDepartmentId()));

        return createEmployee(saved);
    }

    @Override
    public Employee get(UUID id) {
        return findById(id)
                .map(this::createEmployee)
                .filter(it -> it.getPositionId() != null)
                .map(it -> {
                    log.debug("Employee {} found", id);
                    return it;
                })
                .orElseGet(() -> {
                    final var saved = save(employeesProvider.get(id));
                    log.debug("Responded employee {} saved", id);
                    return saved;
                });
    }

    @Override
    public ru.sber.transport.database.external_request.tables.Employee table() {
        return Tables.EMPLOYEE;
    }

    private Employee createEmployee(@NotNull EmployeeRecord employeeRecord) {
        return new Employee() {

            @Override
            public UUID getId() {
                return employeeRecord.getId();
            }

            @Override
            public UUID getDepartmentId() {
                return employeeRecord.getDepartmentId();
            }

            @Override
            public UUID getOrganizationId() {
                return employeeRecord.getOrganizationId();
            }

            @Override
            public String getLastName() {
                return employeeRecord.getLastName();
            }

            @Override
            public String getFirstName() {
                return employeeRecord.getFirstName();
            }

            @Override
            public String getPatronymic() {
                return employeeRecord.getPatronymic();
            }

            @Override
            public UUID getPositionId() {
                return employeeRecord.getPositionId();
            }

            @Override
            public String getPersonnelNumber() {
                return employeeRecord.getPersonnelNumber();
            }

            @Override
            public String getCostCenter() {
                return employeeRecord.getCostCenter();
            }
        };
    }
}
