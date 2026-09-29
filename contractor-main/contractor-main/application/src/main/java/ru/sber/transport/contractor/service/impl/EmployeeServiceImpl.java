package ru.sber.transport.contractor.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.contractor.service.EmployeeService;
import ru.sber.transport.contractor.database.dao.EmployeeRepository;
import ru.sber.transport.contractor.database.model.Employee;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of employee service.
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
class EmployeeServiceImpl implements EmployeeService {
    
    private final EmployeeRepository repository;
    
    @Override
    @Transactional
    public Employee save(Employee employee) {
        return repository.save(employee);
    }
    
    @Override
    public Optional<Employee> get(UUID employeeId) {
        return employeeId == null ? Optional.empty() : repository.findById(employeeId);
    }
    
    @Override
    public Optional<Employee> getByUserId(UUID id) {
        return repository.findByUserId(id);
    }
}
