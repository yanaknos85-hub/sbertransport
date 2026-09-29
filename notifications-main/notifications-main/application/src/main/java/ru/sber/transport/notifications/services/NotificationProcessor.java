package ru.sber.transport.notifications.services;

import ru.sber.transport.notifications.database.model.Notification;

/**
 * Процессинг уведомлений.
 */
public interface NotificationProcessor {
    
    /**
     * Запустить процессинг.
     */
    void process();
    
    /**
     * Запустить процессинг.
     *
     * @param notification уведомление.
     */
    void process(Notification notification);
    
}
