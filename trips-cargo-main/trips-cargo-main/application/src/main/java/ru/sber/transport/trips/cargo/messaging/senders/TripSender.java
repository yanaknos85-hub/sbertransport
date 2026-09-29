package ru.sber.transport.trips.cargo.messaging.senders;

import ru.sber.transport.trips.cargo.business.model.Trip;

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
