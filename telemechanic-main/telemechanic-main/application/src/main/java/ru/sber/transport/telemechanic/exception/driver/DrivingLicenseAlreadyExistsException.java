package ru.sber.transport.telemechanic.exception.driver;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sber.transport.telemechanic.exception.BusinessException;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Driver already exists")
public class DrivingLicenseAlreadyExistsException extends BusinessException {
    
    public DrivingLicenseAlreadyExistsException() {
        super("В системе существует ВУ с указанными данными");
    }
}
