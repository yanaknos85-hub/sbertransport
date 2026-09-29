package ru.sber.transport.etrn.service;

import ru.sber.transport.etrn.database.model.Department;

import java.util.Optional;
import java.util.UUID;

public interface DepartmentService {

    Department save(Department department);

    void delete(Department department);

    Optional<Department> findById(UUID id);

    Optional<Department> get(UUID id);

    Department findOrCreateById(UUID departmentId);
}