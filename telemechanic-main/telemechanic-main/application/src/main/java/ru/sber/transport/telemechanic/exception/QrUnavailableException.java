package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class QrUnavailableException extends RuntimeException {
    public QrUnavailableException() {
        super("QR is not available for getting");
    }
}
