package ru.sber.transport.authentication.web.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception of unknown authentication type.
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED,
        reason = "Wrong authentication credentials type. 'Basic' and 'Token' are supported")
public class UnknownAuthenticationTypeException extends RuntimeException {

    private static final String MSG_FORMAT = "Authentication type '%s' is unknown";

    /**
     * Create a new exception.
     *
     * @param type authentication type.
     */
    public UnknownAuthenticationTypeException(String type) {
        super(String.format(MSG_FORMAT, type));
    }
}
