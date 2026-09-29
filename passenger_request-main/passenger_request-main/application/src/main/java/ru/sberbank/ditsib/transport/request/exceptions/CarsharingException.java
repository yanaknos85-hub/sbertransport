package ru.sberbank.ditsib.transport.request.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Каршеринг")
public class CarsharingException extends RuntimeException {
    
    public CarsharingException(String message) {
        super(message);
    }
    
}
