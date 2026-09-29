package ru.sber.transport.address.business.use_cases;

import ru.sber.transport.address.business.model.Employee;

/**
 * Interface for working with employees.
 */
public interface Employees {

    /**
     * Update an employee.
     *
     * @param source new data of employee.
     */
    void update(Employee source);
}
