package ru.sber.transport.telemechanic.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Organization empty property")
public class OrganizationEmptyPropertyException extends BusinessException {
    
    public OrganizationEmptyPropertyException(String message) {
        super(message);
    }
}
