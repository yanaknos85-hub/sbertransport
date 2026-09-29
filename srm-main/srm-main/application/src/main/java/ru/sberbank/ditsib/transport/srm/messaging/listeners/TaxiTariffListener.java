package ru.sberbank.ditsib.transport.srm.messaging.listeners;

import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.messaging.messages.tariff.TaxiTariffMessage;

import java.util.function.Consumer;

/**
 * Слушатель тарифов.
 */
public interface TaxiTariffListener extends Consumer<Message<TaxiTariffMessage>> {
    
    /**
     * Обработка сообщений тарифов для такси.
     *
     * @param message message.
     */
    void handle(TaxiTariffMessage message);
    
    default void accept(Message<TaxiTariffMessage> message) {
        handle(message.getPayload());
    }
}
