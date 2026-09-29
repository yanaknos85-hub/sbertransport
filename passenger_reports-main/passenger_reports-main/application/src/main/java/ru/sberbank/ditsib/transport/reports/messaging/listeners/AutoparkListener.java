package ru.sberbank.ditsib.transport.reports.messaging.listeners;

import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.messaging.messages.AutoparkMessage;

import java.util.function.Consumer;

/**
 * Listener of autoparks messages.
 */
public interface AutoparkListener extends Consumer<Message<AutoparkMessage>> {
    
    /**
     * Handle organization message.
     *
     * @param message message.
     */
    void handleAutopark(AutoparkMessage message);
    
    @Override
    default void accept(Message<AutoparkMessage> source) {
        handleAutopark(source.getPayload());
    }
    
}
