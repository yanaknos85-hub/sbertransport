package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Department not active")
public class DepartmentNotActiveException extends BusinessException {
    
    public static final String MSG_FORMAT = "Подразделение с идентификатором %s не активно";
    
    public DepartmentNotActiveException(UUID departmentId) {
        super(String.format(MSG_FORMAT, departmentId));
    }
}
