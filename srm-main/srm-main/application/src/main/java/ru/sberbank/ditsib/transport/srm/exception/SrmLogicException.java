package ru.sberbank.ditsib.transport.srm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception throws when limit logic is broken.
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Srm logic problem")
public class SrmLogicException extends RuntimeException {
    
    /**
     * Create a new exception.
     *
     * @param message message.
     */
    public SrmLogicException(String message) {
        super(message);
    }
}
