package ru.sber.transport.authentication.web.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Sending target is null")
public class SendingTargetException extends RuntimeException{

    private static final String MSG_FORMAT = "Can not send code by '%s', sending target is null";

    /**
     * Create a new exception.
     *
     * @param email email.
     */
    public SendingTargetException(String email) {
        super(String.format(MSG_FORMAT, email));
    }

}
