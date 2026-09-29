package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Транспортное средство не связано с организацией диспетчера")
public class TransportUnboundException extends BusinessException{
    public static final String MSG_FORMAT = "Транспортное средство %s не связано с организацией диспетчера";
    
    public TransportUnboundException(UUID transportId) {
        super(String.format(MSG_FORMAT, transportId));
    }
}
