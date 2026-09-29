package ru.sber.transport.telemechanic.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Time zone Exception")
public class DepartmentTimeZoneException extends BusinessException {
    
    public DepartmentTimeZoneException(String message) {
        super(message);
    }
    
    public DepartmentTimeZoneException(String message, UUID departmentId) {
        super(message.formatted(departmentId));
    }
}
