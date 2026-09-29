package ru.sber.transport.notifications.services;

import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.dto.notification.NewNotificationSettingsDto;
import ru.sber.transport.notifications.dto.notification.NotificationSettingsDto;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с настройками уведомлениями.
 */
public interface NotificationSettingsService {
    
    /**
     * Добавление настроек.
     *
     * @param data новые данные.
     *
     * @return добавленные данные.
     */
    NotificationSettings add(UUID organizationId, NotificationSettings data, UUID ownerId);
    
    /**
     * Изменение данных.
     *
     * @param id идентификатор настроек.
     * @param data новые данные.
     */
    NotificationSettings edit(UUID organizationId, UUID id, NotificationSettings data);
    
    /**
     * Изменение данных уведомлений диспетчерской.
     *
     * @param id идентификатор настроек.
     * @param data новые данные.
     */
    void editDispatcherNotification(UUID organizationId, UUID ownerId, UUID id, NotificationSettings data);
    
    /**
     * Удаление данных.
     *
     * @param id идентификатор настроек.
     */
    void delete(UUID organizationId, UUID id);
    
    /**
     * Удаление данных.
     *
     * @param id идентификатор настроек.
     */
    void deleteDispatcherNotification(UUID ownerId, UUID id);
    
    /**
     * Получение настроек.
     *
     * @param id идентификатор настроек.
     *
     * @return настройки.
     */
    NotificationSettings get(UUID organizationId, UUID id);
    
    /**
     * Получение настроек.
     *
     * @param id идентификатор настроек.
     *
     * @return настройки.
     */
    NotificationSettings getDispatcherNotification(UUID ownerId, UUID id);
    
    /**
     * Получение настроек.
     *
     * @param parentId          идентификатор родительской сущности (организация, контрагент или прочие).
     * @param notificationClass класс уведомления.
     * @param type              тип уведомления для получения.
     * @return настройки.
     */
    NotificationSettings get(UUID parentId, NotificationClass notificationClass,
                             NotificationType type);
    
    /**
     * Получение всех настроек.
     *
     * @return все настройки.
     */
    List<NotificationSettings> getAll(UUID organizationId);
    
    /**
     * Получение всех настроек.
     *
     * @return все настройки.
     */
    List<NotificationSettings> getAllDispatcherNotifications(UUID ownerId);
    
    /**
     * Добавление настроек уведомлений для новых организаций
     *
     * @param organizationId ID организации
     */
    void addSettingsForNewOrganization(UUID organizationId);
    
    NotificationSettings toModel(NewNotificationSettingsDto dto);
    
    NotificationSettingsDto toDto(NotificationSettings dto);
    
    NotificationSettings toModel(NotificationSettingsDto dto);
}
