package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Not author")
public class NotAuthorException extends BusinessException {

    public NotAuthorException() {
        super("Только автор заявки может проходить проверки");
    }

}
