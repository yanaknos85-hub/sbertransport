package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Predict request return error")
public class PredictException extends RuntimeException {

    public PredictException(String message, Throwable cause) {
        super(message, cause);
    }
}
