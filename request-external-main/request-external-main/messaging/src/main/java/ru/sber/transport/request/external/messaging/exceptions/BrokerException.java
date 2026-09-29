package ru.sber.transport.request.external.messaging.exceptions;

/**
 * Ошибка взаимодействия с брокером сообщений
 */
public class BrokerException extends RuntimeException {

    public BrokerException(Throwable cause) {
        super(cause);
    }
}
