package ru.sber.transport.notifications.messaging.listeners;

import ru.sber.transport.approvals.messages.ApproveTripRequestMessage;

/**
 * Слушатель согласований.
 */
public interface TripApproveHandler {
    
    /**
     * Полученное согласование.
     *
     * @param message сообщение.
     */
    void handle(ApproveTripRequestMessage message);
    
}
