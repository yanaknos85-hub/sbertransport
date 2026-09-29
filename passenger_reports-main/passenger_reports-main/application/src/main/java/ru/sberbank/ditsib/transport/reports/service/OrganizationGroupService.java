package ru.sberbank.ditsib.transport.reports.service;

import ru.sberbank.ditsib.transport.reports.dto.OrganizationGroupDto;
import ru.sberbank.ditsib.transport.reports.model.OrganizationGroup;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationGroupService {
    
    /**
     * Сохранение группы организаций
     * @param organizationGroupDto данные группы организаций
     * @return группа организаций
     */
    OrganizationGroup save(OrganizationGroupDto organizationGroupDto);
    
    /**
     * Поиск группы организаций по ID
     * @param id ID группы организаций
     * @return группа организаций
     */
    Optional<OrganizationGroup> findById(UUID id);
    
    /**
     * Удаление группы организаций по ID
     * @param id ID группы организаций
     */
    void delete (UUID id);
    
}
