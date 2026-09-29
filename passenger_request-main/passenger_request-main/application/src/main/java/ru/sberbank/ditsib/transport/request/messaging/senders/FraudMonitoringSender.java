package ru.sberbank.ditsib.transport.request.messaging.senders;

import ru.sberbank.ditsib.transport.request.messaging.message.FraudMonitoringMessage;

/**
 * Sender для отправки сообщений в топик fraud-monitoring.
 */
public interface FraudMonitoringSender {

    /**
     * Отправить сообщение в топик fraud-monitoring.
     *
     * @param message сообщение
     */
    void send(FraudMonitoringMessage message);
}
