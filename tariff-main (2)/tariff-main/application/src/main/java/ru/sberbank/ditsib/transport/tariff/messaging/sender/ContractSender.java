package ru.sberbank.ditsib.transport.tariff.messaging.sender;

import ru.sberbank.ditsib.transport.tariff.database.model.Contract;

/**
 * Interface for sending contracts.
 */
public interface ContractSender {
    
    /**
     * Send entity.
     *
     * @param entity data to send.
     * @param deleted deleted.
     */
    void send(Contract entity, boolean deleted);
    
}
