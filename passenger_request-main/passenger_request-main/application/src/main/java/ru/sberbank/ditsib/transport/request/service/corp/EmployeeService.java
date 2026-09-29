package ru.sberbank.ditsib.transport.request.service.corp;


import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
     * Delete employee.
     *
     * @param employee employee.
     */
    void delete(Employee employee);
    
    /**
     * Get employee.
     *
     * @param id ID of employee.
     *
     * @return employee.
     */
    Optional<Employee> get(UUID id);
    
    /**
     * Проверка наличия по id
     *
     * @param id
     */
    void check(UUID id);
    
    /**
     * Вернуть сотрудника по ID
     *
     * @param id ID сотрудника
     *
     * @return сотрудник
     */
    Employee getEmployee(UUID id);
    
    /**
     * Get employee by user ID.
     *
     * @param id ID of user.
     *
     * @return employee.
     */
    Optional<Employee> getByUserId(UUID id);
    
    
    /**
     * Получить Сотрудника из Authentication
     *
     * @param authentication
     *
     * @return Сотрудник
     */
    Employee getAuthenticatedEmployee(JwtAuthenticationToken authentication);
    
    /**
     * Получить сотрудников по списку ид
     *
     * @param uuidSet список ид
     *
     * @return мапа ид/сотрудник
     */
    Map<UUID, Employee> getByEmployeeIds(Set<UUID> uuidSet);
    
}
