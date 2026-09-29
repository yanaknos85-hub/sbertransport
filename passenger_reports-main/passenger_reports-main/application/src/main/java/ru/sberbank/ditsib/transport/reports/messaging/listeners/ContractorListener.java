package ru.sberbank.ditsib.transport.reports.messaging.listeners;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import ru.sberbank.ditsib.transport.messaging.messages.ContractorMessage;

import java.util.function.Consumer;

/**
 * Listener of contractor messages.
 */
public interface ContractorListener extends Consumer<Message<ContractorMessage>> {
    
    /**
     * Handle contractor message.
     *
     * @param message message.
     */
    void handleContractors(@Payload ContractorMessage message);
    
    @Override
    default void accept(Message<ContractorMessage> source) {
        handleContractors(source.getPayload());
    }
}
