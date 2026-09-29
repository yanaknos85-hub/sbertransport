package ru.sber.transport.dispatcher.messaging.senders;

import ru.sber.transport.dispatcher.database.model.Dispatcher;

/**
 * Отправитель данных диспетчеров.
 */
public interface DispatcherSender {

    /**
     * Отправить данные диспетчера.
     *
     * @param dispatcher диспетчер.
     */
    void send(Dispatcher dispatcher);

}
