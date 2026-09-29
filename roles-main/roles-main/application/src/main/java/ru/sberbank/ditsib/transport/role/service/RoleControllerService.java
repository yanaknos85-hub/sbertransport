package ru.sberbank.ditsib.transport.role.service;

import ru.sberbank.ditsib.transport.role.dto.RoleDto;
import ru.sberbank.ditsib.transport.role.dto.RoleSortParameters;

/**
 * Service for working with roles for controller.
 */
public interface RoleControllerService {
    
    /**
     * Add a new role.
     *
     * @param role new role data.
     * @return added role.
     */
    RoleDto add(RoleDto role);
    
    /**
     * Edit role.
     *
     * @param code code of role to edit.
     * @param role new data of role.
     */
    void edit(String code, RoleDto role);
    
    /**
     * Delete role.
     *
     * @param code code of role to delete.
     */
    void delete(String code);
    
    /**
     * Get role by code.
     *
     * @param code code of role to get.
     * @return role.
     */
    RoleDto get(String code);
    
    /**
     * Get all roles.
     *
     * @return collection of roles.
     */
    Iterable<RoleDto> get(boolean pages, RoleSortParameters parameters);
}
