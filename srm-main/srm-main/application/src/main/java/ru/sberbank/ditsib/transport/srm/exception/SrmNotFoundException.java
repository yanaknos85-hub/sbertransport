package ru.sberbank.ditsib.transport.srm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception throws when limit logic is broken.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Not found problem")
public class SrmNotFoundException extends RuntimeException {
    
    /**
     * Create a new exception.
     *
     * @param message message.
     */
    public SrmNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Create a new exception.
     *
     * @param e exception.
     */
    public SrmNotFoundException(Throwable e) {
        super(e);
    }
}
