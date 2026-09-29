package ru.sberbank.ditsib.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.database.model.Department;
import ru.sberbank.ditsib.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.service.DepartmentService;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of department service.
 */
@RequiredArgsConstructor
@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository repository;

    @Override
    public Optional<Department> get(UUID id) {
        return repository.findById(id);
    }

    @Override
    public void delete(Department entity) {
        entity.setActive(false);
        repository.save(entity);
    }

    @Override
    public Department save(Department entity) {
        return repository.save(entity);
    }
}
