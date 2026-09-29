package ru.sber.transport.telemechanic.service;


import ru.sber.transport.telemechanic.database.model.EwbContract;
import ru.sber.transport.telemechanic.database.model.EwbTariff;
import ru.sber.transport.telemechanic.dto.EwbContractDetails;
import ru.sber.transport.telemechanic.enumerate.InspectionType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Сервис по работе с тарифами ЭПЛ
 */
public interface EwbTariffService {
    
    /**
     * Создание
     *
     * @param entity {@link EwbTariff}
     */
    EwbTariff save(EwbTariff entity);
    
    /**
     * Проверка наличия записи по идентификатору организации
     *
     * @param organizationId идентификатор организации
     */
    boolean existsActiveTariffByOrganizationId(UUID organizationId);
    
    Map<InspectionType, EwbContract> getActiveContractByDepartmentId(UUID departmentId, boolean checkNoTariffs);
    
    Set<EwbContractDetails> validateEwbTariff(UUID departmentId);
    
    /**
     * Получаем активные тарифы по списку идентификаторов договоров
     *
     * @param contractIdList список идентификаторов договоров
     *
     * @return {@link List<EwbTariff>}
     */
    List<EwbTariff> getAllByActiveAndByContractIds(List<UUID> contractIdList);
    
    UUID getContractorOrganizationId(UUID tariffDepartmentId);
    
    Set<InspectionType> getInspectionTypesByDriverDepartmentId(UUID tariffDepartmentId);
}