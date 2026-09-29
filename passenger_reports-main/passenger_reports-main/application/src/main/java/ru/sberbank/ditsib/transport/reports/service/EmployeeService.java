package ru.sberbank.ditsib.transport.reports.service;

import ru.sberbank.ditsib.transport.reports.model.Employee;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface EmployeeService {
    
    Employee findOrCreateEmployeeById(UUID id);
    
    Employee save(Employee employee);
    
    Optional<Employee> findById(UUID id);
    
    Optional<UUID> getOrganizationIdByUserId(UUID userId);
    
    List<Employee> findAllById(Set<UUID> ids);
}
