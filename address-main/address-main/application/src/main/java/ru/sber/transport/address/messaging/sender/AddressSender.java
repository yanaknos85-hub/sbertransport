package ru.sber.transport.address.messaging.sender;

import ru.sber.transport.address.business.model.Address;

/**
 * Sender of addresses.
 *
 * @param <T> type of address.
 */
public interface AddressSender<T extends Address> {


    /**
     * Send address data.
     *
     * @param address address to send.
     */
    void send(T address, boolean deleted);

}
