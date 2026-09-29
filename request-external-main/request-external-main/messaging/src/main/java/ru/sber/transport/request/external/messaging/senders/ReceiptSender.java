package ru.sber.transport.request.external.messaging.senders;

import lombok.NonNull;
import ru.sber.transport.request.external.messaging.exceptions.BrokerException;
import ru.sber.transport.request.external.messaging.message.ReceiptMessage;

/**
 * Отправитель данных чека
 */
public interface ReceiptSender {

    /**
     * Отправить чек
     *
     * @param message чек
     * @throws BrokerException в случае сбоя отправки сообщения
     */
    void send(@NonNull ReceiptMessage message) throws BrokerException;
}
