package ru.sber.transport.authsb.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class HashException extends RuntimeException {

    public static final String MSG_FORMAT = "Ошибка хеширования: %s";

    /**
     * Ошибка кеширования
     * @param message
     */
    public HashException(String message) {
        super(String.format(MSG_FORMAT, message));
    }

}
