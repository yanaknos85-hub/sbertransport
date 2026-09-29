package ru.sberbank.ditsib.transport.request.messaging.senders;

import ru.sber.transport.payout.messaging.message.RequestPayoutMessage;

/**
 * Отправитель сообщения в реестр выплат
 */
public interface RequestPayoutSender {

    /**
     * Отправка сообщения в реестр выплат
     *
     * @param message сообщение в реестр выплат
     */
    void send(RequestPayoutMessage message);
}
