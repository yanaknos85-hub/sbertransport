package ru.sberbank.ditsib.transport.tariff.messaging.sender;

import ru.sberbank.ditsib.transport.tariff.database.model.TaxiTariff;

public interface TaxiTariffSender {
    
    /**
     * Send entity.
     *
     * @param taxiTariff data to send.
     */
    void send(TaxiTariff taxiTariff);
    
    /**
     * Send entity to delete.
     *
     * @param taxiTariff data to send.
     */
    void sendDeleted(TaxiTariff taxiTariff);
}
