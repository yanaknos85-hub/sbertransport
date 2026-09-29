package ru.sberbank.ditsib.transport.request.service.corp.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.authorization.utils.ControllerUtils;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.request.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.exceptions.UserNotFoundException;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of employee service.
 */
@RequiredArgsConstructor
@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {
    
    private final EmployeeRepository repository;
    
    @Override
    public Employee save(Employee employee) {
        return repository.save(employee);
    }
    
    @Override
    public void delete(Employee employee) {
        employee.setActive(false);
        repository.save(employee);
    }
    
    @Override
    public Optional<Employee> get(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return repository.findById(id);
    }
    
    @Override
    public void check(UUID id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException(Employee.class, id);
        }
    }
    
    @Override
    public Employee getEmployee(UUID id) {
        return get(id).orElseThrow(() -> new EntityNotFoundException(Employee.class, id));
    }
    
    @Override
    public Optional<Employee> getByUserId(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return repository.findByUserId(id);
    }
    
    @Override
    public Employee getAuthenticatedEmployee(JwtAuthenticationToken authentication) {
        final var userId = ControllerUtils.currentUser();
        return repository.findByUserId(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }
    
    @Override
    public Map<UUID, Employee> getByEmployeeIds(Set<UUID> uuidSet) {
        return repository.findAllByIdIn(uuidSet).collect(Collectors.toMap(Employee::getId, e -> e));
    }
}
