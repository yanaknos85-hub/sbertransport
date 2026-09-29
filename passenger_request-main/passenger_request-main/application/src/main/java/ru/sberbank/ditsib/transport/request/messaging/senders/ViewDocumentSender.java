package ru.sberbank.ditsib.transport.request.messaging.senders;

import ru.sberbank.ditsib.transport.request.database.model.publicTransport.CompensationDocument;

import java.util.UUID;

/**
 * Отправитель факта просмотра документа заявки.
 */
public interface ViewDocumentSender {
    /**
     * Отправить.
     *
     * @param document документ для отправки.
     * @param employeeId сотрдуник, загрузивший документ
     */
    void send(CompensationDocument document, UUID employeeId);
}
