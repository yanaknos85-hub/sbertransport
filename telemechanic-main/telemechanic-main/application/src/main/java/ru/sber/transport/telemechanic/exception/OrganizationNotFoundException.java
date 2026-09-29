package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Organization not found")
public class OrganizationNotFoundException extends BusinessException {
    
    public OrganizationNotFoundException(UUID departmentId) {
        super(String.format("Организация с идентификатором %s не найдена", departmentId));
    }
}
