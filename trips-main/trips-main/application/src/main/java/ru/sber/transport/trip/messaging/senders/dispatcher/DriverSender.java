package ru.sber.transport.trip.messaging.senders.dispatcher;

import ru.sber.transport.trip.business.model.Driver;
import ru.sber.transport.trip.messaging.ChannelType;

/**
 * Отправитель водителей.
 */
public interface DriverSender {

    /**
     * Отправить данные о водителе.
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
