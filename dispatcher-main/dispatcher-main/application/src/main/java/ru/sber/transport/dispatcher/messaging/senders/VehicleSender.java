package ru.sber.transport.dispatcher.messaging.senders;

import ru.sber.transport.dispatcher.database.model.Vehicle;

/**
 * Отправитель данных автомобилей.
 */
public interface VehicleSender {

    /**
     * Отправить данные автомобиля.
     *
     * @param vehicle автомобиль.
     */
    void send(Vehicle vehicle);

}
