package ru.sber.transport.dispatcher.messaging.senders;

import ru.sber.transport.dispatcher.database.model.Driver;
import ru.sber.transport.dispatcher.messages.Source;

import java.util.List;

/**
 * Отправитель данных водителей.
 */
public interface DriverSender {

    /**
     * Отправить данные водителя.
     *
     * @param driver водитель.
     * @param source источник.
     * @param isUserChanging флаг изменения данных пользователя.
     */
    void send(Driver driver, Source source, boolean isUserChanging);

    /**
     * Отправить данные всех водителей.
     * @param drivers водители.
     */
    void sendAll(List<Driver> drivers);

}
