package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY, reason = "Driver is invalid")
public class DriverDoesNotAssociatedWithOrganizationException extends BusinessException {

    public DriverDoesNotAssociatedWithOrganizationException(String message) {
        super(message);
    }

}
