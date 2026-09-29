package ru.sberbank.ditsib.transport.tariff.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception throws when limit logic is broken.
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Contract logic problem")
public class ContractLogicException extends RuntimeException {
    
    /**
     * Create a new exception.
     *
     * @param message message.
     */
    public ContractLogicException(String message) {
        super(message);
    }
    
    /**
     * Create a new exception.
     *
     * @param e exception.
     */
    public ContractLogicException(Throwable e) {
        super(e);
    }
}
