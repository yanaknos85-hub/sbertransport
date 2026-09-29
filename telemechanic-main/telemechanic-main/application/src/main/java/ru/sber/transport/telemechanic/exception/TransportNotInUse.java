package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Transport is not in use")
public class TransportNotInUse extends BusinessException {
    public TransportNotInUse(String message) {
        super(message);
    }
}
