package ru.sberbank.ditsib.transport.reports.messaging.listeners;

import org.springframework.messaging.Message;
import ru.sber.transport.request.messaging.RequestMessage;

import java.util.function.Consumer;

/**
 * Слушатель заявок на поездки.
 */
public interface TripRequestListener  extends Consumer<Message<RequestMessage>> {
    
    /**
     * Получено новое сообщение.
     *
     * @param message сообщение.
     */
    void handle(Message<RequestMessage> message);
    
    @Override
    default void accept(Message<RequestMessage> source) {
        handle(source);
    }
    
}
