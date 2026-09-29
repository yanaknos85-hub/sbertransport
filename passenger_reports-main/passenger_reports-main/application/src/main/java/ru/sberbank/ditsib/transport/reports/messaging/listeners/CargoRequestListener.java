package ru.sberbank.ditsib.transport.reports.messaging.listeners;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import ru.sberbank.ditsib.transport.messaging.messages.trip.CargoRequestMessage;

import java.util.function.Consumer;

/**
 * @deprecated грузы отпиливаются
 */
@Deprecated(since = "2023-05-17")
public interface CargoRequestListener extends Consumer<Message<CargoRequestMessage>> {
    
    /**
     * Получено новое сообщение по грузоперевозкам.
     *
     * @param message сообщение.
     */
//    @StreamListener(CargoRequestSink.INPUT)
    void handleCargo(@Payload CargoRequestMessage message);
    
    @Override
    default void accept(Message<CargoRequestMessage> source) {
        handleCargo(source.getPayload());
    }
    
}
