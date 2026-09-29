package ru.sber.transport.trip.messaging.senders.dispatcher;

import ru.sber.transport.trip.business.model.Shift;

/**
 * Отправитель смен.
 */
public interface ShiftSender {

    void send(Shift shift);

}
