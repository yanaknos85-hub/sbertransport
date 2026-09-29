package ru.sberbank.ditsib.transport.vehicle.service.corp;


import ru.sberbank.ditsib.transport.vehicle.database.model.Department;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    List<Department> getAllById(Set<UUID> ids);

    /**
     * Deactivates and persists the specified department entity. Instead of permanently deleting the entity, this method marks it as inactive by
     * setting its 'active' status to false, then saves the updated entity.
     *
     * @param entity the department entity to be deactivated
     * @throws IllegalArgumentException if the provided entity is null or invalid
     */
    void delete(Department entity);

    /**
     * Save department.
     *
     * @param entity department to save.
     */
    Department save(Department entity);

    /**
     * Получаем подразделения по дереву вверх, включая начальное подразделение
     *
     * @param rootId начальное подразделение для построения дерева
     * @return список идентификаторов подразделений
     */
    List<UUID> getParentDepartments(UUID rootId);
}
