package ru.sberbank.ditsib.transport.tariff.service;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Employee;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeService {
    
    /**
     * Получение сотрудника по id
     * @param id
     * @return сотрудник
     */
    Optional<Employee> get(UUID id);
    
    /**
     * Удаление сотрудника
     * @param employee сущность сотрудника
     */
    void delete(Employee employee);
    
    /**
     * Получение сотрудника по userId
     * @param userId
     * @return сотрудник
     */
    Optional<Employee> getByUserId(UUID userId);
    
    /**
     * Получить Сотрудника из Authentication
     * @param authentication authentication
     * @return Сотрудник
     */
    Employee getAuthenticatedEmployee(JwtAuthenticationToken authentication);
    
    
    /**
     * Получение сотрудника по userId
     * @param userId
     * @return сотрудник
     */
    Optional<Employee> getByUserIdWithOrganizationId(UUID userId);
    
    /**
     * Сохранение сущности сотрудника
     * @param employee сотрудник
     * @return сохраненный сотрудник
     */
    Employee save(Employee employee);
}
