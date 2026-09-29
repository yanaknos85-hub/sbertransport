package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(code = HttpStatus.CONFLICT, reason = "cannot create new medic request")
public class MedicRequestCreateException extends RuntimeException {
    
    public static final String MSG_FORMAT = "Невозможно создать заявку телемедицины для ЭПЛ с идентификатором id=%s";
    
    public MedicRequestCreateException(UUID ewbId) {
        super(MSG_FORMAT.formatted(ewbId));
    }
}
