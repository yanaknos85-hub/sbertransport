package ru.sber.transport.notifications.messaging.listeners;

import ru.sber.transport.notifications.messaging.message.NotificationMessage;

/**
 * Слушатель сообщений с уведомлениями заявок на яндекс такси (request_external)
 */
public interface NotificationHandler {

    /**
     * Получить уведомление
     *
     * @param message {@link NotificationMessage}
     */
    void accept(NotificationMessage message);
}
