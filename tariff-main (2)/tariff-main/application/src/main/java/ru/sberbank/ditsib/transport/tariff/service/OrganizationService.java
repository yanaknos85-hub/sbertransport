package ru.sberbank.ditsib.transport.tariff.service;


import ru.sberbank.ditsib.transport.tariff.database.model.Organization;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
     * Получение организации по названию.
     *
     * @param name название организации.
     * @return организация.
     */
    Optional<Organization> get(String name);
    
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
    void save(Organization organization);

    Map<UUID, String> getNames(Set<UUID> ids);
}
