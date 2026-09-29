package ru.sberbank.ditsib.transport.request.messaging.senders;

import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.Request;

/**
 * Отправитель заявок.
 */
public interface RequestSender<T extends Request> {

    /**
     * Отправить.
     *
     * @param request заявка для отправки.
     */
    default void send(T request) {
        send(request, false);
    }

    /**
     * Отправить заявку с пометкой на удаление.
     * @param request заявка
     * @param isDeleted пометка на удаление
     */
    void send(T request, boolean isDeleted);

    /**
     * Тип транспорта.
     * @return тип транспорта.
     */
    TransportTypeEnum type();
}
