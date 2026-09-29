package ru.sber.transport.trip.messaging.senders.dispatcher;

import ru.sber.transport.trip.business.model.Trip;
import ru.sber.transport.trip.messaging.ChannelType;

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

