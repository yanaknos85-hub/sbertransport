package ru.sberbank.ditsib.transport.reports.messaging.listeners;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import ru.sberbank.ditsib.transport.messaging.messages.StatsMessage;

import java.util.function.Consumer;

/**
 * Слушатель событий для целей поездкок.
 */
public interface StatsListener  extends Consumer<Message<StatsMessage>> {
    
    /**
     * Получено новое сообщение.
     *
     * @param message сообщение.
     */
    void handle(@Payload StatsMessage message);
    
    @Override
    default void accept(Message<StatsMessage> source) {
        handle(source.getPayload());
    }

}