package ru.sberbank.ditsib.transport.vehicle.service.corp.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.vehicle.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Department;
import ru.sberbank.ditsib.transport.vehicle.service.corp.DepartmentService;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of department service.
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    public Optional<Department> get(UUID id) {
        return departmentRepository.findById(id);
    }

    @Override
    public List<Department> getAllById(Set<UUID> ids) {
        return departmentRepository.findAllById(ids);
    }

    @Override
    public void delete(Department entity) {
        entity.setActive(false);
        departmentRepository.save(entity);
    }

    @Override
    public Department save(Department entity) {
        return departmentRepository.save(entity);
    }

    @Override
    public List<UUID> getParentDepartments(UUID rootId) {
        return departmentRepository.getParentDepartments(rootId);
    }
}
