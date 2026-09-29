package ru.sber.transport.cargo.exchange.request.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ResponseStatus(value = BAD_REQUEST)
public class StatusNotTransitionException extends RuntimeException {
    public StatusNotTransitionException(String message) {
        super(message);
    }
}
