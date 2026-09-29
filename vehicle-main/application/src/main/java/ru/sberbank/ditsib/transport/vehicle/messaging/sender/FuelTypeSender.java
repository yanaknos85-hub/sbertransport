package ru.sberbank.ditsib.transport.vehicle.messaging.sender;

import ru.sberbank.ditsib.transport.vehicle.messaging.message.FuelTypeMessage;


public interface FuelTypeSender {

    /**
     * Отправляет сообщение о типе топлива.
     *
     * @param message сообщение
     */
    void send(FuelTypeMessage message);

}
