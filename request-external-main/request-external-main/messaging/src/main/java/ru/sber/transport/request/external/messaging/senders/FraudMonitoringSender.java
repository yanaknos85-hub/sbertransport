package ru.sber.transport.request.external.messaging.senders;

import java.util.UUID;
import ru.sber.transport.request.external.messaging.message.FraudMonitoringMessage;

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
