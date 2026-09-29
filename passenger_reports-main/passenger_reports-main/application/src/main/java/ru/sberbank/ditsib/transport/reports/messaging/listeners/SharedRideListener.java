package ru.sberbank.ditsib.transport.reports.messaging.listeners;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import ru.sberbank.ditsib.transport.messaging.messages.trip.SharedRideMessage;

import java.util.function.Consumer;

public interface SharedRideListener  extends Consumer<Message<SharedRideMessage>> {
    /**
     * Обработка сообщений тарифов.
     *
     * @param message message.
     */
    void handle(@Payload SharedRideMessage message);
    
    @Override
    default void accept(Message<SharedRideMessage> source) {
        handle(source.getPayload());
    }
}
