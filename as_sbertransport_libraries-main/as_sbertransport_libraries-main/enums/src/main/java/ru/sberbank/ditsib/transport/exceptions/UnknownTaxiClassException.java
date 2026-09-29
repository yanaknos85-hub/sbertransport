package ru.sberbank.ditsib.transport.exceptions;

/**
 * Неверный тип услуги.
 */
public class UnknownTaxiClassException extends RuntimeException {
    
    private static final String MSG_FORMAT = "axi class '%s' is unknown";
    
    /**
     * Create a new exception.
     *
     * @param transportType transport type.
     */
    public UnknownTaxiClassException(String transportType) {
        super(String.format(MSG_FORMAT, transportType));
    }
}
