package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "medic request not found")
public class MedicRequestNotFoundException extends RuntimeException {

    public static final String MSG_FORMAT = "Заявка медика с идентификатором id=%s не найдена!";
    
    public MedicRequestNotFoundException(UUID medicRequestId) {
        super(MSG_FORMAT.formatted(medicRequestId));
    }
}
