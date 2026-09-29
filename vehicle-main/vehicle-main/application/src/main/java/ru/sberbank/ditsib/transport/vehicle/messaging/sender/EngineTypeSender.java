package ru.sberbank.ditsib.transport.vehicle.messaging.sender;

import ru.sberbank.ditsib.transport.vehicle.messaging.message.EngineTypeMessage;


public interface EngineTypeSender {

    /**
     * Отправляет сообщение о типе двигателя
     * @param message сообщение
     */
    void send(EngineTypeMessage message);

}
