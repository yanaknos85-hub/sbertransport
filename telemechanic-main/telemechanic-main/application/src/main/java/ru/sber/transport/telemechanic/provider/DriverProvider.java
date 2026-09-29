package ru.sber.transport.telemechanic.provider;

import ru.sber.transport.telemechanic.messaging.listener.message.DriverMessage;

public interface DriverProvider {
    
    /**
     * Сохранение водителя
     *
     * @param message сообщение
     */
    void save(DriverMessage message);
}
