package ru.sber.transport.driver_track.service;

import ru.sber.transport.dispatcher.messages.DriverMessage;

/**
 * Сервис для работы с водителями.
 */
public interface DriverService {

    /**
     * Сохранение сообщения водителя.
     *
     * @param message сообщение водителя.
     */
    void save(DriverMessage message);
}
