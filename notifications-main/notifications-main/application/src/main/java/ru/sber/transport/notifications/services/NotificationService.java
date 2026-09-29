package ru.sber.transport.notifications.services;

import ru.sber.transport.notifications.database.model.Notification;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с уведомлениями.
 */
public interface NotificationService {
    
    /**
     * Добавить новое уведомление.
     *
     * @param notification уведомление.
     * @return добавленное уведомление.
     */
    Notification save(Notification notification);
    
    /**
     * Получение списка неотправленных уведомлений.
     *
     * @return список неотправленных уведомлений.
     */
    List<Notification> getCountingNotSent();
    
    /**
     * Получение списка неотправленных уведомлений.
     *
     * @return список неотправленных уведомлений.
     */
    List<Notification> getTimingNotSent();
    
    /**
     * Получение одного уведомления.
     *
     * @param id идентификатор уведомления.
     * @return уведомление.
     */
    Notification get(UUID id);
    
    /**
     * Отмена отправки уведомлений.
     *
     * @param notificationSettings настройка, к которой уведомления привязаны.
     */
    void cancelAll(NotificationSettings notificationSettings);

    Optional<Notification> find(UUID entityId);
}
