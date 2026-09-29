package ru.sber.transport.dispatcher.messaging.senders;

import ru.sber.transport.dispatcher.database.model.Autopark;

import java.util.List;

/**
 * Отправитель данных о филиале.
 */
public interface AutoparkSender {

    /**
     * Отправляет данные о филиале.
     * @param autopark филиал
     */
    void send(Autopark autopark);

    /**
     * Отправляет данные о всех филиалах.
     * @param autoparks список филиалов
     */
    void sendAll(List<Autopark> autoparks);

}
