package ru.sberbank.ditsib.transport.tariff.messaging.sender;

import ru.sberbank.ditsib.transport.tariff.database.model.BaseTariff;
import ru.sberbank.ditsib.transport.tariff.database.model.PersonalTariff;
import ru.sberbank.ditsib.transport.tariff.database.model.TaxiTariff;

/**
 * Interface for sending tariffs.
 */
public interface PersonalTariffSender {
    
    /**
     * Send entity.
     *
     * @param tariff data to send.
     */
    void send(PersonalTariff tariff);
    
    /**
     * Send entity to delete.
     *
     * @param tariff data to send.
     */
    void sendDeleted(PersonalTariff tariff);
    
}
