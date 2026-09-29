package ru.sberbank.ditsib.transport.request.messaging.senders;

import ru.sber.transport.ws.messages.WebSocketMessage;

public interface NotificationSender {

    void sendNotificationToSubscriber(WebSocketMessage event);
}
