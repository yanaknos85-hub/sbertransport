package ru.sberbank.ditsib.transport.reports.messaging.listeners;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import ru.sberbank.ditsib.transport.messaging.messages.LimitMessage;

import java.util.function.Consumer;

/**
 * Слушатель лимитов
 */
public interface LimitListener extends Consumer<Message<LimitMessage>> {
    /**
     * Обработчик входящих сообщений лимитов
     * @param message сообщение с лимитом
     */
    void handleLimit(@Payload LimitMessage message);
    
    @Override
    default void accept(Message<LimitMessage> source) {
        handleLimit(source.getPayload());
    }

}
