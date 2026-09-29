package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Transport not found")
public class TransportNotFound extends BusinessException {
    
    public static final String MSG_FORMAT = "Транспортное средство с идентификатором %s не найдено";
    
    public TransportNotFound(UUID transportId) {
        super(String.format(MSG_FORMAT, transportId));
    }
}
