package ru.sber.transport.notifications.services;

import ru.sber.transport.notifications.database.model.settings.timing.TimingSettings;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с настройками тайминга.
 */
public interface NotificationTimingService {
    
    /**
     * Получение данных тайминга.
     *
     * @param id идентификатор настроек.
     *
     * @return настройки тайминга.
     */
    List<TimingSettings> getTimingOf(UUID organizationId, UUID id);
    
    /**
     * Установка данных тайминга.
     *
     * @param id идентификатор настроек.
     * @param data новые данные тайминга.
     */
    void setTimingOf(UUID organizationId, UUID id, List<? extends TimingSettings> data);
}
