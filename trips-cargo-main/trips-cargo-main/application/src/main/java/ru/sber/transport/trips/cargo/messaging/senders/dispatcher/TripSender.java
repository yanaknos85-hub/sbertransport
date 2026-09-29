package ru.sber.transport.trips.cargo.messaging.senders.dispatcher;

import ru.sber.transport.trips.cargo.business.model.Trip;
import ru.sber.transport.trips.cargo.messaging.ChannelType;

/**
 * Отправитель поездок.
 */
public interface TripSender {

    /**
     * Отправить поездку.
     *
     * @param trip     поездка.
     * @param isNew    флаг новизны.
     * @param channels типы канала.
     */
    void send(Trip trip, boolean isNew, ChannelType... channels);

}

