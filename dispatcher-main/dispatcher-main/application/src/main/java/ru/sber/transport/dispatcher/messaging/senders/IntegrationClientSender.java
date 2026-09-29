package ru.sber.transport.dispatcher.messaging.senders;


import ru.sber.transport.dispatcher.database.model.IntegrationClient;

/**
 * Отправитель данных клиентов для интеграции с автосервисом.
 */
public interface IntegrationClientSender {

    void send(IntegrationClient client);

}
