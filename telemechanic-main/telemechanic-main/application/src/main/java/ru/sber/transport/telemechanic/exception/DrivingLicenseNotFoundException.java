package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Driving license not found")
public class DrivingLicenseNotFoundException extends BusinessException {
    
    public DrivingLicenseNotFoundException(UUID id) {
        super("Водительское удостоверение с идентификатором %s не найдено".formatted(id));
    }
}
