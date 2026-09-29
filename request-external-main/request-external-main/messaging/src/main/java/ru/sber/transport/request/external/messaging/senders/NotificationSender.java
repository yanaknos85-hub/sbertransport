package ru.sber.transport.request.external.messaging.senders;

import java.util.UUID;
import ru.sber.transport.request.external.model.TripOrderData;

/**
 * Отправитель уведомлений
 */
public interface NotificationSender {

    /**
     * Отправить уведомление
     *
     * @param order данные заказа
     * @param messageType тип сообщения
     * @param receiver идентификатор получателя уведомления
     */
    void send(TripOrderData order, String messageType, UUID receiver);

}