package ru.sberbank.ditsib.transport.tariff.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception throws when tariff logic is broken.
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Contract logic problem")
public class TariffLogicException extends RuntimeException{
    /**
     * Create a new exception.
     *
     * @param message message.
     */
    public TariffLogicException(String message) {
        super(message);
    }
}
