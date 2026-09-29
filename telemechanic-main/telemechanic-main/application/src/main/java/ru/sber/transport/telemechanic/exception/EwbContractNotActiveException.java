package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Ewb contract not active")
public class EwbContractNotActiveException extends BusinessException {
    
    public EwbContractNotActiveException(UUID id) {
        super("Не найден активный договор для организации, id:%s".formatted(id));
    }
}