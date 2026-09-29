package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.reports.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.reports.model.Employee;
import ru.sberbank.ditsib.transport.reports.service.EmployeeService;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    
    @Override
    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }
    
    @Override
    public Optional<Employee> findById(UUID id) {
        return employeeRepository.findById(id);
    }
    
    @Override
    public Optional<UUID> getOrganizationIdByUserId(UUID userId) {
        return employeeRepository.findOrganizationIdByUserId(userId);
    }
    
    public Employee findOrCreateEmployeeById(UUID id) {
        var employee = employeeRepository.findById(id);
        return employee.orElseGet(() -> employeeRepository.save(new Employee(id)));
    }
    
    @Override
    public List<Employee> findAllById(Set<UUID> ids) {
        return employeeRepository.findAllById(ids);
    }
}