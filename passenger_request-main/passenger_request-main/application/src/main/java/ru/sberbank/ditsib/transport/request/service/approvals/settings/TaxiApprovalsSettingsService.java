package ru.sberbank.ditsib.transport.request.service.approvals.settings;

import ru.sberbank.ditsib.transport.request.database.model.approvals.settings.TaxiApprovalsSettings;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с настройками согласования транспорта, кроме такси и общественного
 */
public interface TaxiApprovalsSettingsService {
    
    /**
     * Создать новую настройку согласования
     * @param settings - новая настройки согласования
     * @return созданная настройка согласования
     */
    TaxiApprovalsSettings save(TaxiApprovalsSettings settings);
    
    /**
     * Получить Optional для настройки согласования для ораганизации
     * @param organizationId - идентификатор организации
     * @return настройка согласования
     */
    Optional<TaxiApprovalsSettings> getOptional(UUID organizationId);
    
    /**
     * Получить настройку согласования для ораганизации
     * @param organizationId - идентификатор организации
     * @return настройка согласования
     */
    TaxiApprovalsSettings get(UUID organizationId);
    
    /**
     * Удалить настройку согласования
     * @param organizationId - идентификатор организации
     */
    void delete(UUID organizationId);
}
