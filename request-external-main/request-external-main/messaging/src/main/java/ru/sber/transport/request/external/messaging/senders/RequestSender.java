package ru.sber.transport.request.external.messaging.senders;

import ru.sber.transport.request.external.model.TripOrderData;

/**
 * Отправитель сообщений о заявках
 */
public interface RequestSender {

    /**
     * Отправляет сообщение о заявке
     *
     * @param source данные заявки
     */
    void send(TripOrderData source);

}
