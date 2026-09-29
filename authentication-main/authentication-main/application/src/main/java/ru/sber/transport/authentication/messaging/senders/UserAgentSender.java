package ru.sber.transport.authentication.messaging.senders;

import ru.sber.transport.authentication.business.exceptions.AccountNotFoundException;

public interface UserAgentSender {

    /**
     * Отправить сообщение об авторизации.
     *
     * @param login логин пользователя.
     * @param userAgent инфоомация загловка User Agent.
     * @param clientType тип клиента.
     */
    void send(String login, String userAgent, String clientType) throws AccountNotFoundException;

}
