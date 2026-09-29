package ru.sber.transport.authentication.messaging.senders;

/**
 * Отправитель данных в черный лист.
 */
public interface BlackListSender {
    
    /**
     * Отправить токен в черный список.
     *
     * @param token токен.
     */
    void send(String token);
}
