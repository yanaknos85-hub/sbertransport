package ru.sberbank.ditsib.transport.exceptions;

/**
 * Exception throws when tariff type unknown.
 */
public class WrongTariffTypeException extends RuntimeException {
    
    private static final String MSG_FORMAT = "Tariff type '%s' is unknown";
    
    /**
     * Create a new exception.
     *
     * @param type checked type.
     */
    public WrongTariffTypeException(String type) {
        super(String.format(MSG_FORMAT, type));
    }
}
