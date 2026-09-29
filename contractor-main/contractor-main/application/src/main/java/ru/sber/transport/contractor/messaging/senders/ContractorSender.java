package ru.sber.transport.contractor.messaging.senders;

import ru.sber.transport.contractor.database.model.Contractor;

/**
 * Отправитель данных о контрагенте.
 */
public interface ContractorSender {
    
    /**
     * Отправить данные контрагента.
     *
     * @param contractor контрагент для отправки.
     */
    void send(Contractor contractor);
    
}
