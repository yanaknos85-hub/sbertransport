package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Active request is found")
public class ActiveRequestFound extends BusinessException {

    public ActiveRequestFound() {
        super("Найдена активная заявка");
    }
}
