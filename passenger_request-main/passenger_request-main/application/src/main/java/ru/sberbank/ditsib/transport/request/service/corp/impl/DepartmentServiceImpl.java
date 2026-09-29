package ru.sberbank.ditsib.transport.request.service.corp.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.request.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.transport.request.database.model.Approver;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.service.corp.DepartmentService;

import java.util.*;

/**
 * Implementation of department service.
 */
@RequiredArgsConstructor
@Service
@Transactional
class DepartmentServiceImpl implements DepartmentService {
    
    private final DepartmentRepository repository;
    
    @Override
    public Optional<Department> get(UUID id) {
        return repository.findById(id);
    }
    
    @Override
    public Collection<Approver> getApprovals(UUID departmentId) {
        var opt = repository.getDepartmentWithApprovers(departmentId);
        return opt.map(Department::getApprovers).orElse(Collections.emptyList());
    }
    
    @Override
    public void delete(Department department) {
        department.setActive(false);
        repository.save(department);
    }
    
    @Override
    public Department save(Department department) {
        return repository.save(department);
    }
    
    @Override
    public Set<Department> getByIds(Set<UUID> ids) {
        return new HashSet<>(repository.findAllById(ids));
    }
    
    @Override
    public Set<Department> getWithApproversByIds(Collection<UUID> departmentIds) {
        return new HashSet<>(repository.getDepartmentsWithApprovers(departmentIds));
    }
    
    @Override
    public Department findOrCreateById(UUID id) {
        var department = repository.findById(id);
        return department.orElseGet(() -> repository.save(Department.builder()
                .id(id)
                .build()));
    }
}
