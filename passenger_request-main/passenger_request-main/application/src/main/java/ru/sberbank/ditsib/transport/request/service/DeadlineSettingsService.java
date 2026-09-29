package ru.sberbank.ditsib.transport.request.service;

import ru.sberbank.ditsib.transport.request.database.model.deadline.DeadlineSettings;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с настройками КС
 */
public interface DeadlineSettingsService {

    /**
     * Полуичть настройки КС по ID корп.клиента
     * @param organizationId ID корп.клиента
     * @return настройки КС
     */
    Optional<DeadlineSettings> findByOrganizationId(UUID organizationId);
    
    /**
     * Полуичть {@link Optional<DeadlineSettings>} по ID
     * @param settingsId ID настроек
     * @return настройки КС
     */
    Optional<DeadlineSettings> getOptional(UUID settingsId);
    
    /**
     * Создать или отредактировать настройки КС
     * @param settings данные DeadlineSettings
     * @return сохраненные DeadlineSettings
     */
    DeadlineSettings save(DeadlineSettings settings);
    
    /**
     * Удаление настроек КС
     * @param settings существующие DeadlineSettings
     */
    void hardDelete(DeadlineSettings settings);
}
