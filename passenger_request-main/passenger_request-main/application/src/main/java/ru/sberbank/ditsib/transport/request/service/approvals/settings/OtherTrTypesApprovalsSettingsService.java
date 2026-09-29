package ru.sberbank.ditsib.transport.request.service.approvals.settings;

import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.approvals.settings.OtherTrTypesApprovalsSettings;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с настройками согласования транспорта, кроме такси и общественного
 */
public interface OtherTrTypesApprovalsSettingsService {
    
    /**
     * Создать новую настройку согласования
     * @param settings - новая настройки согласования
     * @return созданная настройка согласования
     */
    OtherTrTypesApprovalsSettings save(OtherTrTypesApprovalsSettings settings);
    
    /**
     * Получить Optional для настройки согласования для ораганизации
     * @param organizationId - идентификатор организации
     * @param transportType - тип транспорта
     * @return настройка согласования
     */
    Optional<OtherTrTypesApprovalsSettings> getOptional(UUID organizationId, TransportTypeEnum transportType);
    
    /**
     * Получить настройку согласования для ораганизации
     * @param organizationId - идентификатор организации
     * @param transportType - тип транспорта
     * @return настройка согласования
     */
    OtherTrTypesApprovalsSettings get(UUID organizationId, TransportTypeEnum transportType);
    
    /**
     * Удалить настройку согласования
     * @param organizationId - идентификатор организации
     * @param transportType - тип транспорта
     */
    void delete(UUID organizationId, TransportTypeEnum transportType);
}
