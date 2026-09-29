package ru.sberbank.ditsib.service;

import ru.sberbank.ditsib.database.model.Employee;

import java.util.Optional;
import java.util.UUID;

/**
 * Доменный сервис для работы с сотрудниками
 */
public interface EmployeeService {

    /**
     * Получает информацию о сотруднике по человекочитаемому ID
     * @param personnelNumber табельный номер сотрудника
     * @return информация о сотруднике или null если не найден
     */
    Employee getEmployeeByPersonnelNumber(String personnelNumber);

    /**
     * Get employee.
     *
     * @param id ID of employee.
     *
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
     * @param employee employee to save.
     */
    Employee save(Employee employee);
}