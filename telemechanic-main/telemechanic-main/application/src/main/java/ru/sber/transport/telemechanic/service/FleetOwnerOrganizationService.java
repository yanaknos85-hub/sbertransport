package ru.sber.transport.telemechanic.service;


import ru.sber.transport.telemechanic.database.model.FleetOwnerOrganization;
import ru.sber.transport.telemechanic.dto.GetAllActiveOrganizationNamesDto;

import java.util.List;
import java.util.UUID;

/**
 * Сервис по работе с организациями владельцев автопарков
 */
public interface FleetOwnerOrganizationService {
    
    /**
     * Создание
     *
     * @param entity {@link FleetOwnerOrganization}
     */
    FleetOwnerOrganization save(FleetOwnerOrganization entity);
    
    FleetOwnerOrganization get(UUID id);
    
    void validateFleetOwnerOrganization(UUID organizationId);
    
    /**
     * Получение списка активных организаций владельцев автопарков
     *
     * @return {@link List <GetAllActiveOrganizationNamesDto>}
     */
    List<GetAllActiveOrganizationNamesDto> getAllActive();
}