package ru.sberbank.ditsib.transport.request.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception is thrown if bonus sum of request is invalid
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Invalid bonus sum")
public class InvalidBonusSumException extends RuntimeException{
    
    public InvalidBonusSumException(String message) {
        super(message);
    }
}
