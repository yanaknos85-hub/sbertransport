package ru.sber.transport.request.external.messaging.senders.impl;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.request.external.messaging.mapper.NotificationMapper;
import ru.sber.transport.request.external.messaging.senders.NotificationSender;
import ru.sber.transport.request.external.model.TripOrderData;

/**
 * Отправитель уведомлений
 */
@RequiredArgsConstructor
public class NotificationSenderImpl implements NotificationSender {

    private final ObjectProvider<OutputBridge> kafkaBridge;
    private final ObjectProvider<OutputBridge> kafkaSslBridge;
    private final ObjectProvider<OutputBridge> avroBridge;
    private final NotificationMapper mapper;

    @Override
    public void send(TripOrderData order, String messageType, UUID receiver) {
        kafkaBridge.ifAvailable(it -> CompletableFuture.runAsync(() ->
                it.send(mapper.toMessage(order, messageType, receiver))));
        kafkaSslBridge.ifAvailable(it -> CompletableFuture.runAsync(() ->
                it.send(mapper.toMessage(order, messageType, receiver))));
        avroBridge.ifAvailable(it -> CompletableFuture.runAsync(() ->
                it.send(mapper.toAvroMessage(order, messageType, receiver))));
    }
}