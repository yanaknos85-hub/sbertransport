package ru.sberbank.ditsib.transport.reports.messaging.listeners;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import ru.sberbank.ditsib.transport.integrations.carsharing.messages.CarsharingDataMessage;

import java.util.function.Consumer;

public interface CarsharingTripListener extends Consumer<Message<CarsharingDataMessage>> {
    /**
     * Handler сообщений о контрактах
     *
     * @param message сообщение
     */
    void handle(@Payload CarsharingDataMessage message);
    
    @Override
    default void accept(Message<CarsharingDataMessage> source) {
        handle(source.getPayload());
    }
}
