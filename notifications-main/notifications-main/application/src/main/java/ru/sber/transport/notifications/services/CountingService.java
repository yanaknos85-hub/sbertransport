package ru.sber.transport.notifications.services;

import ru.sber.transport.notifications.database.model.settings.counting.CountingSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;

import java.util.List;

/**
 * Сервис для работы с количественными триггерами.
 */
public interface CountingService {
    
    /**
     * Получение количественных настроек.
     *
     * @param settings настройки для получения количественных настроек.
     * @return количественные настройки.
     */
    List<CountingSettings> getOfSettings(NotificationSettings settings);
    
}
