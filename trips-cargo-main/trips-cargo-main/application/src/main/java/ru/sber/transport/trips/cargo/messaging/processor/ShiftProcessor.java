package ru.sber.transport.trips.cargo.messaging.processor;

import ru.sber.transport.dispatcher.messages.ShiftMessage;
import ru.sber.transport.trips.cargo.business.model.Driver;
import ru.sber.transport.trips.cargo.message.EwbMessage;

import java.util.concurrent.Future;

public interface ShiftProcessor {

    Future<Integer> processShift(ShiftMessage message, Driver driver);

    /**
     * Обновление данных EWB
     * @param message сообщение
     */
    void processEwbUpdate(EwbMessage message);

}
