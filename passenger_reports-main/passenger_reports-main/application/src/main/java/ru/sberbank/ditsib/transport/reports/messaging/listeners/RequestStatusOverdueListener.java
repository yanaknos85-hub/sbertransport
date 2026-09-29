package ru.sberbank.ditsib.transport.reports.messaging.listeners;

import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.messaging.messages.RequestStatusOverdueMessage;

import java.util.function.Consumer;

/**
 * Слушатель записей о просроченных по КС поездках
 */
public interface RequestStatusOverdueListener extends Consumer<Message<RequestStatusOverdueMessage>> {
    /**
     * Получено новое сообщение.
     *
     * @param message сообщение.
     */
    void handle(RequestStatusOverdueMessage message);
    
    @Override
    default void accept(Message<RequestStatusOverdueMessage> source) {
        handle(source.getPayload());
    }
}
