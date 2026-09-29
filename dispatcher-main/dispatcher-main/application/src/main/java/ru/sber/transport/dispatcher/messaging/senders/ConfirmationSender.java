package ru.sber.transport.dispatcher.messaging.senders;

import java.util.UUID;

/**
 * Отправитель в кафку на подтверждение номера
 */
public interface ConfirmationSender {

    /**
     * Отправить запрос на подтверждение номер
     * @param id идентификатор пользователя
     * @param phone номер
     */
    void send(UUID id, String phone);

}
