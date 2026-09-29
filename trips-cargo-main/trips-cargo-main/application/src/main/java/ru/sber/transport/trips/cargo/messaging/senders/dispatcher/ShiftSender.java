package ru.sber.transport.trips.cargo.messaging.senders.dispatcher;

import ru.sber.transport.trips.cargo.business.model.Shift;

/**
 * Отправитель смен.
 */
public interface ShiftSender {

    void send(Shift shift);

}
