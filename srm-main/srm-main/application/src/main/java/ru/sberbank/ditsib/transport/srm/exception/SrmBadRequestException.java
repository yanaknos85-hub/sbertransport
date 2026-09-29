package ru.sberbank.ditsib.transport.srm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception throws when limit logic is broken.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Bad request problem")
public class SrmBadRequestException extends RuntimeException {

    /**
     * Create a new exception.
     *
     * @param message message.
     */
    public SrmBadRequestException(String message) {
        super(message);
    }
}
