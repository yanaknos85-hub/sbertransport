package ru.sberbank.ditsib.transport.request.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Возникла ошибка при смене статуса")
public class StatusChangeException extends RuntimeException {
    
    public StatusChangeException(String message) {
        super(message);
    }
}
