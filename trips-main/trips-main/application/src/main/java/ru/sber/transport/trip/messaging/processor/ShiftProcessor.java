package ru.sber.transport.trip.messaging.processor;

import ru.sber.transport.dispatcher.messages.ShiftMessage;
import ru.sber.transport.trip.business.model.Driver;
import ru.sber.transport.trip.message.EwbMessage;

import java.util.concurrent.Future;

/**
 * Обработчик входящих смен.
 */
public interface ShiftProcessor {

    /**
     * Обработка входящих смен.
     * @param message сообщение с данными смены
     * @param driver водитель
     * @return результат обработки
     */
    Future<Integer> processShift(ShiftMessage message, Driver driver);

    /**
     * Обновление данных EWB
     * @param message сообщение
     */
    void processEwbUpdate(EwbMessage message);

}
