package ru.sberbank.ditsib.transport.srm.messaging.listeners;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import ru.sberbank.ditsib.transport.messaging.messages.tariff.PersonalTariffMessage;

import java.util.function.Consumer;

/**
 * Слушатель тарифов.
 */
public interface PersonalTariffListener extends Consumer<Message<PersonalTariffMessage>> {
    
    /**
     * Обработка сообщений тарифов для такси.
     *
     * @param message message.
     */
    void handle(@Payload PersonalTariffMessage message);
    
    default void accept(Message<PersonalTariffMessage> message) {
        handle(message.getPayload());
    }
}
