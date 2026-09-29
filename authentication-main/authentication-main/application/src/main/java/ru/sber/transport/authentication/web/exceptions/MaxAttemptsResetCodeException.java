package ru.sber.transport.authentication.web.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Too many attempts to reset the password code")
public class MaxAttemptsResetCodeException extends RuntimeException{

    private static final String MSG_TEXT = "Too many attempts to reset the password code";

    /**
     * Create a new exception.
     */
    public MaxAttemptsResetCodeException() {
        super(MSG_TEXT);
    }

}
