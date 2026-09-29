package ru.sberbank.ditsib.transport.request.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "A request for one or more passengers already exists.")
public class CreateRequestConflictException extends BusinessException {
    public CreateRequestConflictException(String message) {
        super(message);
    }
}
