package ru.sberbank.ditsib.transport.tariff.messaging.sender;

import ru.sberbank.ditsib.transport.tariff.database.model.PublicTariff;

/**
 * Отправитель тарифов общественного транспорта.
 */
public interface PublicTariffSender {
    
    /**
     * Send entity.
     *
     * @param tariff data to send.
     */
    void send(PublicTariff tariff);
    
    /**
     * Send entity to delete.
     *
     * @param tariff data to send.
     */
    void sendDeleted(PublicTariff tariff);
    
}
