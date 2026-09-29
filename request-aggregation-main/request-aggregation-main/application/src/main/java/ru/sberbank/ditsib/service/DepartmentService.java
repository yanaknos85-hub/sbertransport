package ru.sberbank.ditsib.service;


import ru.sberbank.ditsib.database.model.Department;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for working with departments.
 */
public interface DepartmentService {

    /**
     * Get department by ID.
     *
     * @param id ID of department.
     * @return department.
     */
    Optional<Department> get(UUID id);

    /**
     * Delete department.
     *
     * @param entity department to delete.
     */
    void delete(Department entity);

    /**
     * Save department.
     *
     * @param entity department to save.
     */
    Department save(Department entity);
}
