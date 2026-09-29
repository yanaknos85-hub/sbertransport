package ru.sberbank.ditsib.transport.request.messaging.senders;

import ru.sberbank.ditsib.transport.request.database.model.publicTransport.CompensationDocument;

import java.util.UUID;

/**
 * Отправитель факта создания/удаления документа заявки.
 */
public interface RequestDocumentSender {
    /**
     * Отправить добоавление документа.
     * @param document документ для отправки.
     * @param requestId заявка, к которой принадлежит документ
     * @param employeeId сотрудник, который создает заявку
     */
    void send(CompensationDocument document, UUID requestId, UUID employeeId);
    
    /**
     * Отправить удаление документа.
     * @param document документ для отправки.
     * @param requestId заявка, к которой принадлежит документ
     * @param employeeId сотрудник, который создает заявку
     */
    void sendDeleted(CompensationDocument document, UUID requestId, UUID employeeId);
}
