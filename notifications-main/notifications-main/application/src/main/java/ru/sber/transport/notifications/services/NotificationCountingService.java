package ru.sber.transport.notifications.services;

import ru.sber.transport.notifications.database.model.settings.counting.CountingSettings;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с настройками количественного триггера.
 */
public interface NotificationCountingService {
    
    /**
     * Получение данных.
     *
     * @param id идентификатор настроек.
     *
     * @return данные о количественных триггерах.
     */
    List<CountingSettings> getCountingOf(UUID organizationId, UUID id);
    
    /**
     * Установка количественных триггеров.
     *
     * @param id идентификатор настроек.
     * @param data новые данные.
     */
    void setCountingOf(UUID organizationId, UUID id, List<? extends CountingSettings> data);
    
}
