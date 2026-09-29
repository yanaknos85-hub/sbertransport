package ru.sberbank.ditsib.transport.request.service.corp;


import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for working with organizations.
 */
public interface OrganizationService {
    
    /**
     * Get organization.
     *
     * @param id ID of organization.
     *
     * @return organization.
     */
    Optional<Organization> get(UUID id);
    
    /**
     * Get all active organizations.
     *
     * @return organization.
     */
    List<Organization> getAll();
    
    /**
     * Проверить наличие организации по ID
     * @param id ID организации
     */
    void check(UUID id);
    
    /**
     * Вернуть организацию по ID
     * @param id ID организации
     * @return организация
     */
    Organization getOrganization(UUID id);
    
    /**
     * Delete organization.
     *
     * @param organization organization to delete.
     */
    void delete(Organization organization);
    
    /**
     * Save organization.
     *
     * @param organization organization to save.
     */
    Organization save(Organization organization);
}
