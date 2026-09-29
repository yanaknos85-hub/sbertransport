package ru.sber.transport.etrn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.etrn.database.dao.DepartmentRepository;
import ru.sber.transport.etrn.database.model.Department;
import ru.sber.transport.etrn.service.DepartmentService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository repository;

    @Override
    public Department save(Department department) {
        return repository.save(department);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Department> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public void delete(Department department) {
        repository.delete(department);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Department> get(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Department findOrCreateById(UUID id) {
        var department = repository.findById(id);
        return department.orElseGet(() -> repository.save(Department.builder().id(id).build()));
    }
}