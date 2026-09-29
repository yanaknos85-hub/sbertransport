package ru.sberbank.ditsib.transport.reports.messaging.listeners;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

import java.util.function.Consumer;

/**
 * Listener of departments messages.
 */
public interface DepartmentListener extends Consumer<Message<DepartmentMessage>> {
    
    /**
     * Handle department message.
     *
     * @param message message.
     */
    void handleDepartments(@Payload DepartmentMessage message);
    
    @Override
    default void accept(Message<DepartmentMessage> source) {
        handleDepartments(source.getPayload());
    }
    
}
