package ru.sberbank.ditsib.transport.reports.messaging.listeners;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import ru.sberbank.ditsib.transport.messaging.messages.TripPurposeMessage;

import java.util.function.Consumer;

public interface TripPurposeListener  extends Consumer<Message<TripPurposeMessage>> {

    /**
     * Обработчик .
     *
     * @param message message.
     */
    void handle(@Payload TripPurposeMessage message);
    
    @Override
    default void accept(Message<TripPurposeMessage> source) {
        handle(source.getPayload());
    }

}

