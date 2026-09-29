package ru.sberbank.ditsib.transport.tariff.messaging.sender;

import ru.sberbank.ditsib.transport.tariff.database.model.BaseTariff;

/**
 * Interface for sending tariffs.
 */
public interface TariffSender {
    
    /**
     * Send entity.
     *
     * @param entity data to send.
     * @param deleted deleted.
     */
    void send(BaseTariff entity, boolean deleted);
    
}
