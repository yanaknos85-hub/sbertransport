package ru.sberbank.ditsib.transport.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * Exception of illegal state of target entity
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Illegal state")
public class IllegalStateResponseException extends IllegalStateException {

    /**
     * Создать ошибку.
     *
     * @param s сообщение ошибки
     */
    public IllegalStateResponseException(String s) {
        super(s);
    }
}
