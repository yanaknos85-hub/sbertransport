package ru.sber.transport.etrn.service;

import org.springframework.security.core.Authentication;
import ru.sber.transport.etrn.database.model.Employee;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeService {

    void save(Employee employee);

    void delete(Employee employee);

    Optional<Employee> findById(UUID id);

    /**
     * Получить Сотрудника из Authentication
     *
     * @param authentication Authentication
     * @return Сотрудник
     */
    Employee getAuthenticatedEmployee(Authentication authentication);
}