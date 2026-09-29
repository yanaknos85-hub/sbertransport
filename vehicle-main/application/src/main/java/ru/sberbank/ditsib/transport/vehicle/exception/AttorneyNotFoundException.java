package ru.sberbank.ditsib.transport.vehicle.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Attorney not found")
public class AttorneyNotFoundException extends RuntimeException {

    public static final String MSG_FORMAT = "Доверенность у пользователя с id=%s не найдена или просрочена!";

    public AttorneyNotFoundException(UUID telemechanicId) {
        super(String.format(MSG_FORMAT, telemechanicId));
    }
}
