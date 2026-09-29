package ru.sberbank.ditsib.transport.reports.messaging.listeners;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

import java.util.function.Consumer;

/**
 * Listener of departments messages.
 */
public interface PositionListener  extends Consumer<Message<PositionMessage>> {
    
    
    /**
     * Handle organization message.
     *
     * @param message message.
     */
    void handlePosition(@Payload PositionMessage message);
    
    @Override
    default void accept(Message<PositionMessage> source) {
        handlePosition(source.getPayload());
    }
}
