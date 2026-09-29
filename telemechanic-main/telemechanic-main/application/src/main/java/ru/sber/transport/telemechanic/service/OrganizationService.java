package ru.sber.transport.telemechanic.service;


import ru.sber.transport.telemechanic.database.model.Organization;
import ru.sber.transport.telemechanic.dto.OrganizationDto;
import ru.sber.transport.telemechanic.dto.OrganizationWithDepartmentDto;
import ru.sber.transport.telemechanic.dto.TariffDepartmentResponse;
import ru.sber.transport.telemechanic.exception.AwaitingSynchronizationException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    
    /**
     * Get organization
     *
     * @return List organizations.
     */
    List<OrganizationDto> getAll();
    
    /**
     * Get all with internal contractors
     *
     * @return List organizations
     */
    List<OrganizationDto> getAllWithInternalContractor();
    
    List<OrganizationWithDepartmentDto> getAllWithDepartment(Set<UUID> uuids);
    
    OrganizationDto getByUserId(UUID userId);
    
    List<TariffDepartmentResponse> getAllDepartmentWithTariff(UUID id);
    
    /**
     * Получаем первый контакт организации типа Phone, или пустую строку
     *
     * @param id Идентификатор записи об организации
     * @return контакт типа Phone
     */
    String getFirstContactPhone(UUID id);
    
    /**
     * Сохраняем организацию, которую мы получим по grpc из сервиса corporate
     *
     * @param message Сообщение в случае ошибки
     * @param id Идентификатор записи об организации
     */
    void saveGrpcEntity(String message, UUID id) throws AwaitingSynchronizationException;
}
