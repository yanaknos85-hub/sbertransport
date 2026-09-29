package ru.sberbank.ditsib.transport.reports.messaging.listeners;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import ru.sberbank.ditsib.transport.messaging.messages.PersonalCarMessage;

import java.util.function.Consumer;

public interface PersonalCarListener  extends Consumer<Message<PersonalCarMessage>> {
    
    /**
     * Handle personal car message.
     *
     * @param message message.
     */
    void handlePersonalCar(@Payload PersonalCarMessage message);
    
    @Override
    default void accept(Message<PersonalCarMessage> source) {
        handlePersonalCar(source.getPayload());
    }
}
