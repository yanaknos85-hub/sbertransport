package ru.sberbank.ditsib.transport.reports.messaging.listeners;

import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Слушатель сообщений о сотрудниках.
 */
public interface EmployeeListener extends Consumer<Message<EmployeeMessage>> {
    
    /**
     * Получено новое сообщение.
     *
     * @param id идентификатор.
     * @param message сообщение.
     */
    void handleEmployees(UUID id, EmployeeMessage message);
    
    @Override
    default void accept(Message<EmployeeMessage> source) {
        handleEmployees(source.getPayload().getId(), source.getPayload());
    }
    
}
