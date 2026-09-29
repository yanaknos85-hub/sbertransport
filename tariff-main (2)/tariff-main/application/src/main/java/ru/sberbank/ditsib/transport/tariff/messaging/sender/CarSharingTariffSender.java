package ru.sberbank.ditsib.transport.tariff.messaging.sender;

import ru.sberbank.ditsib.transport.tariff.database.model.CarSharingTariff;

/**
 * Отправитель тарифов каршеринга.
 */
public interface CarSharingTariffSender {
    
    /**
     * Send entity.
     *
     * @param carSharingTariff data to send.
     */
    void send(CarSharingTariff carSharingTariff);
    
    /**
     * Send entity to delete.
     *
     * @param carSharingTariff data to send.
     */
    void sendDeleted(CarSharingTariff carSharingTariff);
}
