package ru.sberbank.ditsib.transport.vehicle.service.corp;


import ru.sberbank.ditsib.transport.vehicle.database.model.Employee;
import ru.sberbank.ditsib.transport.vehicle.dto.EmployeeDto;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for working with employees.
 */
public interface EmployeeService {

    /**
     * Get employee.
     *
     * @param id ID of employee.
     * @return employee.
     */
    Optional<Employee> get(UUID id);

    /**
     * Delete employee.
     *
     * @param entity employee to delete.
     */
    void delete(Employee entity);

    /**
     * Save employee.
     *
     * @param entity employee to save.
     */
    Employee save(Employee entity);
    
    /**
     * Save or update employee.
     *
     * @param entity employee to save or update.
     */
    void saveOrUpdate(Employee entity);

    /**
     * Получение сотрудника по ИД пользователя
     * Params: userid
     * */
    Employee getByUserId(UUID userId);

    EmployeeDto getByPersonnelNumber(String personnelNumber, UUID userId);
}
