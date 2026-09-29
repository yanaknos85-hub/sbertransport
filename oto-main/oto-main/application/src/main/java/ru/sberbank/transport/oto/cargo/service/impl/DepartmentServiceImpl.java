package ru.sberbank.transport.oto.cargo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sberbank.transport.oto.cargo.database.dao.DepartmentRepository;
import ru.sberbank.transport.oto.cargo.database.model.Department;
import ru.sberbank.transport.oto.cargo.service.DepartmentService;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    
    @Override
    public Optional<Department> get(UUID id) {
        return departmentRepository.findById(id);
    }
    
    @Override
    public void delete(Department department) {
        departmentRepository.delete(department);
    }
    
    @Override
    public Department save(Department department) {
        return departmentRepository.save(department);
    }
    
    @Override
    public Department findOrCreateById(UUID id) {
        var department = departmentRepository.findById(id);
        return department.orElseGet(() -> departmentRepository.save(new Department(id)));
    }
    
    @Override
    public List<UUID> findAllChildren(UUID departmentId) {
        var foundedDepartments = new ArrayList<UUID>();
        foundedDepartments.add(departmentId);
        var children = departmentRepository.findAllByParentId(departmentId);
        log.debug("Найдено "+children.size()+" дочерних подразделений для подразделения "+departmentId.toString());
        foundedDepartments.addAll(children.stream().map(this::extractId).collect(Collectors.toList()));
        log.debug("Всего найдено "+foundedDepartments.size()+" подразделений");
        return foundedDepartments;
    }
    
    private UUID extractId(Department department){
        return department.getId();
    }
}
