package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Invalid date range")
public class DateRangeValidationException extends BusinessException {
    
    public DateRangeValidationException() {
        super("Некорректно задан период создания заявки");
    }
}
