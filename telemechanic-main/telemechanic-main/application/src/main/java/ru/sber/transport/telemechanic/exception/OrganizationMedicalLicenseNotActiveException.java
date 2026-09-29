package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "License not active")
public class OrganizationMedicalLicenseNotActiveException extends BusinessException {
    
    public OrganizationMedicalLicenseNotActiveException(String message) {
        super(message);
    }
}