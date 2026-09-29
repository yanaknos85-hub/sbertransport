package ru.sberbank.ditsib.transport.tariff.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.tariff.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Department;
import ru.sberbank.ditsib.transport.tariff.service.DepartmentService;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class DepartmentServiceImpl implements DepartmentService {
    
    private final DepartmentRepository departmentRepository;
    
    @Override
    public Optional<Department> get(UUID id) {
        return departmentRepository.findById(id);
    }
    
    @Override
    public Department save(Department department) {
        return departmentRepository.save(department);
    }
    
    @Override
    public void delete(Department department) {
        departmentRepository.delete(department);
    }
}
