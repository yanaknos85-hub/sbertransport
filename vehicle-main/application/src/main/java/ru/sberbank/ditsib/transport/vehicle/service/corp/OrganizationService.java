package ru.sberbank.ditsib.transport.vehicle.service.corp;


import ru.sberbank.ditsib.transport.vehicle.database.model.Organization;
import ru.sberbank.ditsib.transport.vehicle.dto.GetDepartmentsInfo;
import ru.sberbank.ditsib.transport.vehicle.dto.OrganizationDto;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Service for working with organizations.
 */
public interface OrganizationService {
    
    /**
     * Get organizations.
     *
     * @param id ID of organizations.
     *
     * @return organizations.
     */
    Optional<Organization> get(UUID id);
    
    List<Organization> getAllByIds(Set<UUID> ids);
    
    /**
     * Delete organizations.
     *
     * @param entity organizations to delete.
     */
    void delete(Organization entity);
    
    /**
     * Save organizations.
     *
     * @param entity organizations to save.
     */
    Organization save(Organization entity);
    
    /**
     * Get organizations
     *
     * @return List organizations.
     */
    List<OrganizationDto> getAll();
    
    List<GetDepartmentsInfo> getAllWithDepartment(Set<UUID> uuids);
    
    /**
     * Get organizations by user ID.
     *
     * @param userId ID of user.
     *
     * @return organizations {@link OrganizationDto}
     */
    OrganizationDto getByUserId(UUID userId);
}
