package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "qr-code not exists")
public class QrCodeNotExistsException extends RuntimeException {
    
    public static final String MSG = "QR-код не готов";
    
    public QrCodeNotExistsException() {
        super(MSG);
    }
}
