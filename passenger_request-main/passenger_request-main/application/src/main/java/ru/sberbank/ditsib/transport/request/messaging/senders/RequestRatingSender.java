package ru.sberbank.ditsib.transport.request.messaging.senders;

import ru.sberbank.ditsib.transport.request.database.model.RequestRating;

import java.util.UUID;

/**
 * Отправитель заявок.
 */
public interface RequestRatingSender {
    
    /**
     * Отправить.
     *
     * @param requestId идентификатор заявки.
     * @param request заявка для отправки.
     */
    void send(UUID requestId, RequestRating request);
    
}
