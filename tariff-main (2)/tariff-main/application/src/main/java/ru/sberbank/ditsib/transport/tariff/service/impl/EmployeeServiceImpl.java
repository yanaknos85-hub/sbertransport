package ru.sberbank.ditsib.transport.tariff.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.tariff.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Employee;
import ru.sberbank.ditsib.transport.tariff.service.EmployeeService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    
    @Override
    public Optional<Employee> get(UUID id) {
        return employeeRepository.findById(id);
    }
    
    @Override
    public void delete(Employee employee) {
        employeeRepository.delete(employee);
    }
    
    @Override
    public Optional<Employee> getByUserId(UUID userId) {
        return employeeRepository.findByUserId(userId);
    }
    
    @Override
    public Employee getAuthenticatedEmployee(JwtAuthenticationToken authentication) {
        var userId = UUID.fromString(authentication.getToken().getId());
        return employeeRepository.findByUserId(userId).orElseThrow(() -> new EntityNotFoundException(Employee.class, userId));
    }
    
    @Override
    public Optional<Employee> getByUserIdWithOrganizationId(UUID userId) {
        return employeeRepository.findByUserIdWithOrganizationId(userId)
                                 .map(dto -> Employee.builder()
                                                     .id(dto.getId())
                                                     .userId(dto.getUserId())
                                                     .patronymic(dto.getPatronymic())
                                                     .lastName(dto.getLastName())
                                                     .firstName(dto.getFirstName())
                                                     .organizationId(dto.getOrganizationId())
                                                     .departmentId(dto.getDepartmentId())
                                                     .build());
    }
    
    @Override
    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }
}
