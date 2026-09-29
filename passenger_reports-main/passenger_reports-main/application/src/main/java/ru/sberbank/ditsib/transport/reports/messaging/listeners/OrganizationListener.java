package ru.sberbank.ditsib.transport.reports.messaging.listeners;

import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Listener of organization messages.
 */
public interface OrganizationListener extends Consumer<Message<OrganizationMessage>> {
    
    /**
     * Handle organization message.
     *
     * @param message message.
     */
    void handleOrganization(UUID id, OrganizationMessage message);
    
    @Override
    default void accept(Message<OrganizationMessage> source) {
        handleOrganization(source.getPayload().getId(), source.getPayload());
    }
    
}
