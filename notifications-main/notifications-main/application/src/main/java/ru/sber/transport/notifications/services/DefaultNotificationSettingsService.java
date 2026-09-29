package ru.sber.transport.notifications.services;

import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;

import java.util.List;

public interface DefaultNotificationSettingsService {
    
    /**
     * Получение всех доступных конфигураций
     *
     * @return список конфигураций
     */
    List<NotificationSettings> getDefaultSettings();
    
    /**
     * Получение конфигураций по параметрам
     *
     * @param notificationClass класс уведомления
     * @param notificationType тип уведомления
     * @return конфигурация с указанными параметрами
     */
    NotificationSettings getSettingByClassAndType(NotificationClass notificationClass,
                                                   NotificationType notificationType);
}
