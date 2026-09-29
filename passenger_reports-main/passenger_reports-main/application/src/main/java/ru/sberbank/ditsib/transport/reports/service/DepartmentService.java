package ru.sberbank.ditsib.transport.reports.service;

import ru.sberbank.ditsib.transport.reports.dto.DepartmentDTO;
import ru.sberbank.ditsib.transport.reports.model.Department;
import ru.sberbank.ditsib.transport.reports.model.Request;

import java.util.*;

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
     * Get department by ID.
     *
     * @param id ID of department.
     *
     * @return department.
     */
    List<Department> get(List<UUID> id);
    
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

    void setDepartmentHierarchy(Map<UUID, Request> requestList);

    Map<Integer, Department> getDepartmentHierarchy(Department department);
    
    Map<Integer, Department> getDepartmentHierarchy(UUID departmentId);
    
    Set<UUID> getDepartmentWithAllChildren(HashSet<UUID> departmentIds);
    
    List<Department> findDepartments(UUID organizationId, DepartmentDTO departmentDTO);
}
