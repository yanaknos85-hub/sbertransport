package ru.sberbank.transport.oto.cargo.service;

import ru.sberbank.transport.oto.cargo.database.model.Department;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentService {
    
    /**
     * Get department by ID.
     *
     * @param id ID of department.
     *
     * @return department.
     */
    Optional<Department> get(UUID id);
    
    /**
     * Delete department.
     *
     * @param department department.
     */
    void delete(Department department);
    
    /**
     * Save department.
     *
     * @param department department to save.
     */
    Department save(Department department);
    
    Department findOrCreateById(UUID id);
    
    /**
     * Get all children of department.
     *
     * @param departmentId department id.
     */
    List<UUID> findAllChildren(UUID departmentId);
}
