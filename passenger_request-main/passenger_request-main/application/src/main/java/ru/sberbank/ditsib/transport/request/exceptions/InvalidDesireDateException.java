package ru.sberbank.ditsib.transport.request.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Invalid desire date")
public class InvalidDesireDateException extends RuntimeException{
    
    public InvalidDesireDateException(String message) {
        super(message);
    }
}
