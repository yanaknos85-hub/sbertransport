package ru.sber.transport.telemechanic.service;


import ru.sber.transport.telemechanic.database.model.EwbContract;
import ru.sber.transport.telemechanic.enumerate.InspectionType;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Сервис по работе с договорами ЭПЛ
 */
public interface EwbContractService {
    
    /**
     * Создание
     *
     * @param entity {@link EwbContract}
     */
    EwbContract save(EwbContract entity);
    
    /**
     * Получаем все договора по виду осмотра и идентификатору записи об организации
     *
     * @param inspectionTypes {@link InspectionType}
     * @param organizationId Идентификатор записи об организации
     *
     * @return {@link List<EwbContract>}
     */
    List<EwbContract> getAllByInspectionTypeAndOrganizationId(Set<InspectionType> inspectionTypes, UUID organizationId);
}