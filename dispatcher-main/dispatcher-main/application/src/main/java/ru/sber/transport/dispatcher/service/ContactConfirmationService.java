package ru.sber.transport.dispatcher.service;

import ru.sber.transport.user_data_confirmation.message.UserDataConfirmationMessage;

/**
 * Серивс подтверждения номера диспетчера
 */
public interface ContactConfirmationService {

    /**
     * Подтвердить номер
     * @param message данные с номером
     */
    void confirm(UserDataConfirmationMessage message);

}
