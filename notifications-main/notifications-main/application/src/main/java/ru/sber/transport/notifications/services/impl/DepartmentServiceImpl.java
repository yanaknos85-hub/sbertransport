package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.notifications.database.dao.messages.corp.DepartmentRepository;
import ru.sber.transport.notifications.database.model.coprorate.Department;
import ru.sber.transport.notifications.services.DepartmentService;

import java.util.Optional;
import java.util.UUID;

/**
 * Реализация интерфейса по работе с подразделениями.
 */
@Component
@RequiredArgsConstructor
class DepartmentServiceImpl implements DepartmentService {
    
    private final DepartmentRepository repository;
    
    @Override
    public Optional<Department> get(UUID departmentId) {
        return repository.findById(departmentId);
    }
    
    @Override
    public Department save(Department department) {
        return repository.save(department);
    }
    
    @Override
    public void delete(UUID id) {
        repository.findById(id).ifPresent(repository::delete);
    }
}
