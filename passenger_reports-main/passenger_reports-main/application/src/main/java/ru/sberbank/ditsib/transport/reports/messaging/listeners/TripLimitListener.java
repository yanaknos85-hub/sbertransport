package ru.sberbank.ditsib.transport.reports.messaging.listeners;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import ru.sberbank.ditsib.transport.messaging.messages.LimitActionResultMessage;

import java.util.function.Consumer;

public interface TripLimitListener  extends Consumer<Message<LimitActionResultMessage>> {

    /**
     * Обработка сообщений лимитов.
     *
     * @param message message.
     */
    void handle(@Payload LimitActionResultMessage message);
    
    @Override
    default void accept(Message<LimitActionResultMessage> source) {
        handle(source.getPayload());
    }
}
