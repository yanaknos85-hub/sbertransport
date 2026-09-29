package ru.sberbank.ditsib.transport.vehicle.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Переданная организация не связана с департаментом")
public class OrgDepRelationValidationException extends BusinessException {
    public OrgDepRelationValidationException(String message) {
        super(message);
    }
}
