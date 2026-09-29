package ru.sber.transport.contractor.service;

import ru.sber.transport.contractor.database.model.Employee;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for working with employees.
 */
public interface EmployeeService {
    
    /**
     * Save employee.
     *
     * @param employee employee.
     */
    Employee save(Employee employee);
    
    /**
     * Get employee of organization.
     *
     * @param employeeId ID of employee.
     *
     * @return employee.
     */
    Optional<Employee> get(UUID employeeId);

    /**
     * Get employee by user ID.
     *
     * @param id ID of user.
     *
     * @return employee.
     */
    Optional<Employee> getByUserId(UUID id);

    
}
