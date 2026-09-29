package ru.sber.transport.address.providers.employee.mappers;

import org.mapstruct.Mapper;
import ru.sber.transport.address.business.model.Employee;
import ru.sber.transport.database.addresses.tables.records.EmployeeRecord;

/**
 * Database to business mapper of employees.
 */
@Mapper
public interface EmployeeDatabaseMapper {

    /**
     * Map database to business.
     *
     * @param source database entity.
     * @return business entity.
     */
    Employee toBusiness(EmployeeRecord source);

    /**
     * Map business to database.
     *
     * @param source business entity.
     * @return database entity.
     */
    EmployeeRecord toDatabase(Employee source);
}
