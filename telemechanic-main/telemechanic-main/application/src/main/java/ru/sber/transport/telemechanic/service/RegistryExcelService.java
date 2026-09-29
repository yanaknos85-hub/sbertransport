package ru.sber.transport.telemechanic.service;

import ru.sber.transport.telemechanic.dto.RegistryExcelDto;
import ru.sber.transport.telemechanic.dto.ReportQueryParamDto;

import java.util.List;
import java.util.UUID;

/**
 * Service of request to excel processing
 */
public interface RegistryExcelService {

    /**
     * Получение всех dto для формирования excel по реестрам с учетом организации
     *
     * @param organizationId идентификатор организации {@link UUID}
     * @return {@link RegistryExcelDto}
     */
    List<RegistryExcelDto> getAllRegistryByOrganizationId(UUID organizationId, ReportQueryParamDto parameters);
}
