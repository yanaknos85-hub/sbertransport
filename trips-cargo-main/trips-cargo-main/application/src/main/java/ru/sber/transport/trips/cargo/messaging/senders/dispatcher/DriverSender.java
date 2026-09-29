package ru.sber.transport.trips.cargo.messaging.senders.dispatcher;

import ru.sber.transport.trips.cargo.business.model.Driver;
import ru.sber.transport.trips.cargo.messaging.ChannelType;

/**
 * Отправитель водителей.
 */
public interface DriverSender {

    /**
     * Отправить данные о водителе только по кафке.
     *
     * @param driver водитель.
     */
    void send(Driver driver);

    /**
     * Отправить данные о водителе.
     *
     * @param driver  водитель.
     * @param channel тип канала.
     */
    void send(Driver driver, ChannelType channel);

}
