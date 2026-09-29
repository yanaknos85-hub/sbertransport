package ru.sber.transport.notifications.messaging.listeners;

import ru.sber.transport.request.messaging.RequestMessage;

/**
 * Слушатель событий о поездках.
 */
public interface TripRequestHandler {
    
    /**
     * Получено сообщение.
     *
     * @param message сообщение с заявкой о поездке.
     */
    void handle(RequestMessage message);
    
}
