package ru.sber.transport.request.external.messaging.senders.impl;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.experimental.Delegate;
import ru.sber.transport.request.external.messaging.senders.NotificationSender;
import ru.sber.transport.request.external.model.TripOrderData;
import ru.sber.transport.web_socket.handlers.WebSocketHandler;

public class NotificationWebSocketSenderImpl extends WebSocketHandler<TripOrderData> implements NotificationSender {

    @Override
    public void send(TripOrderData order, String messageType, UUID receiver) {
        CompletableFuture.runAsync(() -> send(receiver, new NotificationMessage(order)));
    }

    @Override
    public String url() {
        return "/requests";
    }

    private record NotificationMessage(@Delegate TripOrderData source) implements TripOrderData {}

}
