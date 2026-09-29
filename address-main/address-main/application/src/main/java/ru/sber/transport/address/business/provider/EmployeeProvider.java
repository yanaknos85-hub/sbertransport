package ru.sber.transport.address.business.provider;

import ru.sber.transport.address.business.model.Employee;

import java.util.Optional;
import java.util.UUID;

/**
 * Provider of employee data.
 */
public interface EmployeeProvider {

    /**
     * Get an employee by ID.
     *
     * @param id ID of employee.
     * @return employee.
     */
    Optional<Employee> get(UUID id);

    /**
     * Save a new employee in database.
     *
     * @param source source data.
     */
    void save(Employee source);
}
