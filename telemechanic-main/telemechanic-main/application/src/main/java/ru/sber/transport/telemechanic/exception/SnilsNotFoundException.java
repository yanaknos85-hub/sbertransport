package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "snils not found")
public class SnilsNotFoundException extends BusinessException {
    
    private static final String MSG = "У водителя id=%s не найден СНИЛС. Обратитесь к диспетчеру.";
    
    public SnilsNotFoundException(UUID driverId) {
        super(MSG.formatted(driverId));
    }
}
