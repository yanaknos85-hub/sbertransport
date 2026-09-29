package ru.sber.transport.notifications.services;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.transport.notifications.database.model.coprorate.Employee;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface EmployeeService {
    
    Optional<Employee> get(UUID employeeId);
    
    List<Employee> getByIds(List<UUID> employeeIds);
    
    List<Employee> getPassengersByRequestIds(Set<UUID> requestIds);
    
    Optional<Employee> getByUserId(UUID userId);
    
    void delete(UUID id);
    
    void save(Employee build);

    /**
     * Получить Сотрудника из Authentication
     *
     * @param authentication Authentication
     * @return Сотрудник
     */
    Employee getAuthenticatedEmployee(JwtAuthenticationToken authentication);}
