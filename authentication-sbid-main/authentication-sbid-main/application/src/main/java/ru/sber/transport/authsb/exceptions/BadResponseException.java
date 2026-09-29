package ru.sber.transport.authsb.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Ошибка при взаимодействии с Sber ID")
public class BadResponseException extends RuntimeException {

    public BadResponseException(String message) {
        super(message);
    }
}
