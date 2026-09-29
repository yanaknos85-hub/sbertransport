package ru.sber.transport.telemechanic.service;

import org.springframework.data.domain.Page;
import ru.sber.transport.telemechanic.dto.medic_request_report.MedicRequestExcelAllOrganizationsDto;
import ru.sber.transport.telemechanic.dto.medic_request_report.MedicRequestExcelSelfOrganizationDto;
import ru.sber.transport.telemechanic.dto.medic_request_report.MedicRequestRegistryAllOrganizationsRequest;
import ru.sber.transport.telemechanic.dto.medic_request_report.MedicRequestRegistrySelfOrganizationRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MedicRequestService {
    
    /**
     * Поиск медицинских осмотров для реестра для всех организаций
     * @param request
     * @return
     */
    Page<Map<String, Object>> searchRegistryForAllOrganizations(MedicRequestRegistryAllOrganizationsRequest request);
    
    /**
     * Поиск медицинских осмотров для реестра для своей организации
     * @param request
     * @param userId
     * @return
     */
    Page<Map<String, Object>> searchRegistryForSelfOrganization(MedicRequestRegistrySelfOrganizationRequest request, UUID userId);
    
    /**
     * Поиск медицинских осмотров для экселя для всех организаций
     * @param parameters
     * @return
     */
    List<MedicRequestExcelAllOrganizationsDto> searchExcelForAllOrganizations(Map<String, ?> parameters);
    
    /**
     * Поиск медицинских осмотров для экселя для своей организации
     * @param parameters
     * @return
     */
    List<MedicRequestExcelSelfOrganizationDto> searchExcelForSelfOrganization(Map<String, ?> parameters, UUID userId);
}
