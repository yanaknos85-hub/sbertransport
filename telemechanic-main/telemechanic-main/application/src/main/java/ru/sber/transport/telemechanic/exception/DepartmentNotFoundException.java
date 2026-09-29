package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Department not found")
public class DepartmentNotFoundException extends BusinessException {
    
    public static final String MSG_FORMAT = "Подразделение с идентификатором %s не найдено";
    
    public DepartmentNotFoundException(UUID departmentId) {
        super(String.format(MSG_FORMAT, departmentId));
    }
}
