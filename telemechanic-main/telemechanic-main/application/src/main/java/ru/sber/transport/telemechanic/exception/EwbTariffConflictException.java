package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Ewb tariff conflict")
public class EwbTariffConflictException extends BusinessException {
    
    public EwbTariffConflictException(String message) {
        super(message);
    }
}