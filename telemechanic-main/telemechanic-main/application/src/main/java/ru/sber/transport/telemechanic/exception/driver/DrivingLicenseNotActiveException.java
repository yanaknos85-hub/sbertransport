package ru.sber.transport.telemechanic.exception.driver;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sber.transport.telemechanic.exception.BusinessException;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Driving license has been inactive")
public class DrivingLicenseNotActiveException extends BusinessException {
    public DrivingLicenseNotActiveException(UUID id) {
        super("Неактивна запись для водителя. ВУ ID:%s".formatted(id));
    }
}
