package ru.sberbank.ditsib.transport.tariff.messaging.sender;

import ru.sberbank.ditsib.transport.tariff.database.model.GroupTransferTariff;

public interface GroupTransferTariffSender {
    
    /**
     * Send entity.
     *
     * @param groupTransferTariff data to send.
     */
    void send(GroupTransferTariff groupTransferTariff);
    
}
