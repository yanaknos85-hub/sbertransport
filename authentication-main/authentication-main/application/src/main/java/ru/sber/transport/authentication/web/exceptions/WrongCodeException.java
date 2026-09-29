package ru.sber.transport.authentication.web.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Wrong code")
public class WrongCodeException extends RuntimeException{

    private static final String MSG_FORMAT = "Wrong code: '%s'";

    /**
     * Create a new exception.
     *
     * @param code code.
     */
    public WrongCodeException(String code) {
        super(String.format(MSG_FORMAT, code));
    }

}
