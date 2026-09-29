package ru.sberbank.ditsib.transport.srm.messaging.senders;


import ru.sber.transport.srm.model.SrmSharedRideDTO;

/**
 * Отправитель дополнительных данных для заявок по такси.
 */
public interface SharedRideSender {
    
    /**
     * Отправить.
     *
     * @param sharedRide данные для отправки.
     */
    void send(SrmSharedRideDTO sharedRide);
}
