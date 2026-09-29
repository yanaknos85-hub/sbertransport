package ru.sberbank.ditsib.transport.request.service.corp;


import ru.sberbank.ditsib.transport.request.database.model.Approver;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;

import java.util.Collection;
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
     *
     * @return department.
     */
    Optional<Department> get(UUID id);
    
    /**
     * Get approvals for department
     *
     * @param departmentId ID of department.
     *
     * @return department.
     */
    Collection<Approver> getApprovals(UUID departmentId);
    
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
    
    /**
     * @param ids list of required ids
     *
     * @return set of required departments
     */
    Set<Department> getByIds(Set<UUID> ids);
    
    /**
     * Список подразделений с согласующими
     * @param departmentIds
     * @return
     */
    Set<Department> getWithApproversByIds(Collection<UUID> departmentIds);

    Department findOrCreateById(UUID departmentId);
}
