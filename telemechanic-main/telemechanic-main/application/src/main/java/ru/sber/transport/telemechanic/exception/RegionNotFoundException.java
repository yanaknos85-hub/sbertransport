package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Region not found")
public class RegionNotFoundException extends BusinessException{
    
    public RegionNotFoundException(String regionCode) {
        super("Неизвестный код региона %s".formatted(regionCode));
    }
}
