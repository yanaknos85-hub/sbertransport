package ru.sber.transport.notifications.services;


import ru.sber.transport.notifications.database.model.deadline.DeadlineSettings;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с настройками КС
 */
public interface DeadlineSettingsService {
    
    /**
     * Полуичть настройки по ID
     * @param settingsId ID настроек
     * @return настройки КС
     */
    DeadlineSettings getById(UUID settingsId);
    
    /**
     * Полуичть настройки КС по ID корп.клиента
     * @param organizationId ID корп.клиента
     * @return Optional<DeadlineSettings>
     */
    Optional<DeadlineSettings> findByOrganizationId(UUID organizationId);
    
    /**
     * Полуичть Optional<DeadlineSettings> по ID
     * @param settingsId ID настроек
     * @return Optional<DeadlineSettings>
     */
    Optional<DeadlineSettings> getOptional(UUID settingsId);
    
    /**
     * Получить флаг существования настроек по ID
     * @param settingsId ID настроек
     * @return флаг существования настроек
     */
    boolean existsById(UUID settingsId);
    
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
