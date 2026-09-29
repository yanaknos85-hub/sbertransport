package ru.sber.transport.notifications.messaging.listeners;

import ru.sber.transport.approvals.messages.ApproveSharedRideMessage;

public interface ApproveSharedRideHandler {

    /**
     * Полученное согласование на совместную поездку.
     *
     * @param message сообщение.
     */
    void handle(ApproveSharedRideMessage message);
}
