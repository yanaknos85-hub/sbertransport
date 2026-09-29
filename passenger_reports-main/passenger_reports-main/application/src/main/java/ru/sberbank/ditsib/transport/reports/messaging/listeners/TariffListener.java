package ru.sberbank.ditsib.transport.reports.messaging.listeners;

import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.messaging.messages.TariffMessage;

import java.util.function.Consumer;

public interface TariffListener  extends Consumer<Message<TariffMessage>> {
    /**
     * Обработка сообщений тарифов.
     *
     * @param message message.
     */
    void handle(TariffMessage message);
    
    @Override
    default void accept(Message<TariffMessage> source) {
        handle(source.getPayload());
    }
}
