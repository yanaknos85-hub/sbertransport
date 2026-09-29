package ru.sber.transport.request.external.providers.order.model;

import static ru.sber.transport.database.external_request.Tables.EMPLOYEE;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.jooq.Name;
import org.jooq.Record;
import ru.sber.transport.database.external_request.tables.records.EmployeeRecord;
import ru.sber.transport.request.external.model.Employee;

/**
 * Реализация модели сотрудника
 */
@RequiredArgsConstructor
public class EmployeeModel implements Employee {

    @Delegate
    private final EmployeeRecord source;

    /**
     * Создание модели сотрудника по его идентификатору
     *
     * @param id идентификатор сотрудника
     */
    public EmployeeModel(UUID id) {
        this(new EmployeeRecord());
        this.source.setId(id);
    }

    /**
     * Создание модели сотрудника по источнику данных
     *
     * @param source источник данных сотрудника
     */
    public EmployeeModel(Employee source) {
        this(new EmployeeRecord());
        this.source.setId(source.getId());
        this.source.setOrganizationId(source.getOrganizationId());
        this.source.setDepartmentId(source.getDepartmentId());
    }

    /**
     * Создание модели сотрудника по его идентификатору и имени
     *
     * @param alias  имя таблицы
     * @param source источник данных сотрудника
     */
    public EmployeeModel(Name alias, Record source) {
        this(new EmployeeRecord());
        this.source.setId(source.getValue(alias.append(EMPLOYEE.ID.getUnqualifiedName()), UUID.class));
        this.source.setLastName(source.getValue(alias.append(EMPLOYEE.LAST_NAME.getUnqualifiedName()), String.class));
        this.source.setFirstName(source.getValue(alias.append(EMPLOYEE.FIRST_NAME.getUnqualifiedName()), String.class));
        this.source.setPatronymic(source.getValue(alias.append(EMPLOYEE.PATRONYMIC.getUnqualifiedName()), String.class));
        this.source.setPersonnelNumber(source.getValue(alias.append(EMPLOYEE.PERSONNEL_NUMBER.getUnqualifiedName()), String.class));
    }
}