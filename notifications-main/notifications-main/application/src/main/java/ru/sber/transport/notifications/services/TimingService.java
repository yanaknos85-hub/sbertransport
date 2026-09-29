package ru.sber.transport.notifications.services;

import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.timing.TimingSettings;

import java.util.List;

/**
 * Сервис для работы с таймингом.
 */
public interface TimingService {
    
    /**
     * Получение тайминга настроек.
     *
     * @param settings настройки для получения тайминга.
     * @return тайминг.
     */
    List<TimingSettings> getOfSettings(NotificationSettings settings);
    
}
