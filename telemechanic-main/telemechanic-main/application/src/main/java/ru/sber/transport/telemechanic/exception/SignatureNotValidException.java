package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class SignatureNotValidException extends RuntimeException {
    public SignatureNotValidException(String message) {
        super(message);
    }
}
