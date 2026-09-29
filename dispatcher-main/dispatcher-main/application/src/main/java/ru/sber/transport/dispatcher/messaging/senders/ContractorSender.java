package ru.sber.transport.dispatcher.messaging.senders;

import ru.sber.transport.dispatcher.database.model.Contractor;
import ru.sber.transport.dispatcher.dto.IntegrationTypeDto;

/**
 * Отправитель данных о контрагенте.
 */
public interface ContractorSender {

    /**
     * Отправить данные контрагента.
     *
     * @param contractor      контрагент для отправки.
     * @param integrationType тип интеграции.
     */
    void send(Contractor contractor, IntegrationTypeDto integrationType);

}
