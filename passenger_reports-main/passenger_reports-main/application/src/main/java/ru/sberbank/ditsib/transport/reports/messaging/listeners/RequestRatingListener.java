package ru.sberbank.ditsib.transport.reports.messaging.listeners;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import ru.sberbank.ditsib.transport.messaging.messages.trip.TripRatingMessage;

import java.util.function.Consumer;

/**
 * Слушатель рейтинга поездок
 */
public interface RequestRatingListener  extends Consumer<Message<TripRatingMessage>> {
    /**
     * Получено новое сообщение.
     *
     * @param message сообщение.
     */
    void handle(@Payload TripRatingMessage message);
    
    @Override
    default void accept(Message<TripRatingMessage> source) {
        handle(source.getPayload());
    }
}
