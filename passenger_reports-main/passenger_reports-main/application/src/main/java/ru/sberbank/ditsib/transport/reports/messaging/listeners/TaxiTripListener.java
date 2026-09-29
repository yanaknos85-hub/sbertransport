package ru.sberbank.ditsib.transport.reports.messaging.listeners;

import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.messaging.messages.trip.TaxiTripMessage;

import java.util.UUID;
import java.util.function.Consumer;

public interface TaxiTripListener extends Consumer<Message<TaxiTripMessage>> {
    
    /**
     * Получено новое сообщение.
     *
     * @param message сообщение.
     */
    void handle(TaxiTripMessage message, UUID id);

    default void accept(Message<TaxiTripMessage> message) {
        handle(message.getPayload(), message.getHeaders().get(KafkaHeaders.RECEIVED_KEY, UUID.class));
    }

}
