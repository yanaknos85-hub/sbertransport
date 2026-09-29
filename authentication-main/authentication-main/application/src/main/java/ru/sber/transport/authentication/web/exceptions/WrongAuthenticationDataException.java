package ru.sber.transport.authentication.web.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception of wrong authorization data.
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Wrong authentication data")
public class WrongAuthenticationDataException extends RuntimeException {

    private static final String MSG_FORMAT = "Credentials '%s' are not parcelable";

    /**
     * Create a new exception.
     *
     * @param credentials credentials.
     */
    public WrongAuthenticationDataException(String credentials) {
        super(String.format(MSG_FORMAT, credentials));
    }

}
