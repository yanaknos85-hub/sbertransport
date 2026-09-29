package ru.sber.transport.notifications.database.dao.settings;

import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;

import java.util.List;

public interface DefaultNotificationSettingsRepository {
    
    /**
     * Список существующих конфигураций для уведомлений по умолчанию
     * @return список настроек
     */
    List<NotificationSettings> findAllDefaultSettings();
    
    /**
     * Поиск конфигураций по параметрам
     * @param notificationClass класс уведомления
     * @param notificationType тип уведомления
     * @return конфигурация с указанными параметрами
     */
    NotificationSettings findSettingByClassAndType(NotificationClass notificationClass,
                                                 NotificationType notificationType);
    
}
