package ru.sber.transport.trip.messaging.senders;

import ru.sber.transport.trip.business.model.Trip;

/**
 * Отправитель поездок.
 */
public interface TripSender {

    /**
     * Отправить поездку.
     *
     * @param trip поездка для отправки.
     */
    void send(Trip trip);

}
