package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Oraganization has no departments with autoparks")
public class OrganizationHasNotDepartmentWithAutoparkException extends BusinessException {
    
    public OrganizationHasNotDepartmentWithAutoparkException() {
        super(String.format("У данной организации отсутствует внутренний автопарк"));
    }
}
