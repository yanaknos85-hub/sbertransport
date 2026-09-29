package ru.sber.transport.telemechanic.exception.driver;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sber.transport.telemechanic.exception.BusinessException;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Driver not found")
public class DriverNotFoundException extends BusinessException {
    
    public static final String MSG_FORMAT = "Не найден водитель ID:%s";
    public static final String DRIVER_BY_USER_ID_MSG_FORMAT = "Не найден водитель c userId:%s";
    
    public DriverNotFoundException(UUID id) {
        super(MSG_FORMAT.formatted(id));
    }
    
    public DriverNotFoundException(UUID id, String msgFormat) {
        super(msgFormat.formatted(id));
    }
}
