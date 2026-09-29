package ru.sberbank.ditsib.transport.exceptions;

/**
 * Неверный тип транспорта.
 */
public class UnknownTransportTypeException extends RuntimeException {
    
    private static final String MSG_FORMAT = "Transport type '%s' is unknown";
    
    /**
     * Create a new exception.
     *
     * @param transportType transport type.
     */
    public UnknownTransportTypeException(String transportType) {
        super(String.format(MSG_FORMAT, transportType));
    }
}
