package ru.sber.transport.telemechanic.provider;

import ru.sber.transport.telemechanic.messaging.listener.message.DispatcherMessage;

public interface DispatcherProvider {
    
    /**
     * Сохранение диспетчера
     *
     * @param message сообщение
     */
    void save(DispatcherMessage message);
}
