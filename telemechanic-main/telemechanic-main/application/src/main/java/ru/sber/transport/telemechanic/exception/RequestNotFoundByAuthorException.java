package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Request not found")
public class RequestNotFoundByAuthorException extends BusinessException {

    public RequestNotFoundByAuthorException() {
        super("Заявка не найдена, или найдено более одной заявки");
    }
}
