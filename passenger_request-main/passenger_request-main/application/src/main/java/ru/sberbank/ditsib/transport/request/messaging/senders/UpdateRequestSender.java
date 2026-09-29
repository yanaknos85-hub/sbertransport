package ru.sberbank.ditsib.transport.request.messaging.senders;

import ru.sberbank.ditsib.transport.request.database.model.UpdateRequest;

/**
 * Отправитель изменения заявок.
 */
public interface UpdateRequestSender {
    /**
     * Отправить.
     *
     * @param update изменение заявки для отправки.
     */
    void send(UpdateRequest update);
    
    /**
     * Отправить отмену.
     *
     * @param update отмена.
     */
    void sendDeleted(UpdateRequest update);
}
