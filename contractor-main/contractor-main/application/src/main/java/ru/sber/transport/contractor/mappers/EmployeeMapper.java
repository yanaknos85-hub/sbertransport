package ru.sber.transport.contractor.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.sber.transport.contractor.database.model.Employee;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

/**
 * Mapper of employees.
 */
@Mapper
public interface EmployeeMapper {

    /**
     * Update an employee with data.
     *
     * @param target the employee to update.
     * @param source source data for updating.
     */
    void update(@MappingTarget Employee target, EmployeeMessage source);

}
