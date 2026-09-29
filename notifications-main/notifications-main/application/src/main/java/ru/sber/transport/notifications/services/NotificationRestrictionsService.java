package ru.sber.transport.notifications.services;

import ru.sber.transport.notifications.database.model.settings.restriction.RestrictionSettings;

import java.util.UUID;

/**
 * Сервис для управления ограничениями.
 */
public interface NotificationRestrictionsService {
    
    /**
     * Получение ограничений.
     *
     * @param id идентификатор настроек.
     *
     * @return ограничения.
     */
    RestrictionSettings getRestrictionsOf(UUID organizationId, UUID id);
    
    /**
     * Установка ограничений.
     *
     * @param id идентификатор настроек.
     * @param data новые данные.
     */
    void setRestrictionsOf(UUID organizationId, UUID id, RestrictionSettings data);
}
