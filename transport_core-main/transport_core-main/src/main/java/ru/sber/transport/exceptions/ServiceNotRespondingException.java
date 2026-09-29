package ru.sber.transport.exceptions;

/**
 * Exception throws when tariff type unknown.
 */
public class ServiceNotRespondingException extends RuntimeException {

    /**
     * Формат сообщения.
     */
    public static final String MSG_FORMAT = "'%s' service not responding";

    /**
     * Create a new exception.
     *
     * @param name service name
     */
    public ServiceNotRespondingException(String name) {
        super(String.format(MSG_FORMAT, name));
    }
}