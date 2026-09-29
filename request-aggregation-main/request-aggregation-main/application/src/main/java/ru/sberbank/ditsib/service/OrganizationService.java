package ru.sberbank.ditsib.service;

import ru.sberbank.ditsib.database.model.Organization;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for working with organizations.
 */
public interface OrganizationService {
    
    /**
     * Опционально получаем организацию
     * @param id Идентификатор записи об организации
     * @return {@link Optional<Organization>}
     */
    Optional<Organization> get(UUID id);
    
    /**
     * Delete organization.
     *
     * @param entity organization to delete.
     */
    void delete(Organization entity);
    
    /**
     * Save organization.
     *
     * @param entity organization to save.
     */
    Organization save(Organization entity);
}
