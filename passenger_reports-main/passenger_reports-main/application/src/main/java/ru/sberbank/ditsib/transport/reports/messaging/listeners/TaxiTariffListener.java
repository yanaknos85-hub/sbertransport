package ru.sberbank.ditsib.transport.reports.messaging.listeners;

import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.messaging.messages.tariff.TaxiTariffMessage;

import java.util.UUID;
import java.util.function.Consumer;

public interface TaxiTariffListener  extends Consumer<Message<TaxiTariffMessage>> {
    
    /**
     * Обработка сообщений тарифов для такси.
     *
     * @param message message.
     */
    void handle(UUID key, TaxiTariffMessage message);
    
    @Override
    default void accept(Message<TaxiTariffMessage> source) {
        handle(source.getPayload().getId(), source.getPayload());
    }
}
