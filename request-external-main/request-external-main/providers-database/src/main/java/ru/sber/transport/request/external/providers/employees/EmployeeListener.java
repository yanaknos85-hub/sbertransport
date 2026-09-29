package ru.sber.transport.request.external.providers.employees;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.beans.factory.ObjectProvider;
import ru.sber.database.listeners.SearchEngineListener;
import ru.sber.database.types.TsVector;
import ru.sber.transport.database.external_request.Tables;
import ru.sber.transport.database.external_request.tables.Employee;
import ru.sber.transport.database.external_request.tables.records.EmployeeRecord;
import ru.sberbank.utils.reflection.ReflectionUtils;

/**
 * Слушатель для обновления индекса
 */
@RequiredArgsConstructor
public class EmployeeListener implements SearchEngineListener<EmployeeRecord, Employee> {

    private transient final ObjectProvider<DSLContext> contextProvider;

    @Getter(lazy = true)
    @Accessors(fluent = true)
    private final DSLContext context = contextProvider.getObject();

    @Override
    public Employee getTable() {
        return Tables.EMPLOYEE;
    }

    @Override
    public Field<Object> getId(Employee employee) {
        return ReflectionUtils.cast(employee.ID);
    }

    @Override
    public Field<TsVector> getDocumentField(Employee employee) {
        return employee.DOCUMENT;
    }

    @Override
    public List<Field<?>> getDocumentableFields(Employee employee) {
        return List.of(
                Tables.EMPLOYEE.LAST_NAME,
                Tables.EMPLOYEE.FIRST_NAME,
                Tables.EMPLOYEE.PATRONYMIC
        );
    }

}
