package ru.sberbank.ditsib.transport.request.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Ошибка валидации запроса")
public class ValidationRequestException extends RuntimeException {
    
    public ValidationRequestException(String message) {
        super(message);
    }
    
}
