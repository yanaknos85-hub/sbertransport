package ru.sberbank.ditsib.transport.reports.messaging.listeners;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import ru.sberbank.ditsib.transport.messaging.messages.ContractMessage;

import java.util.function.Consumer;

/**
 * Listener of contract messages.
 */
public interface ContractListener extends Consumer<Message<ContractMessage>> {
    
    /**
     * Handle contract message.
     *
     * @param message message.
     */
    void handleContracts(@Payload ContractMessage message);
    
    @Override
    default void accept(Message<ContractMessage> source) {
        handleContracts(source.getPayload());
    }
}
