package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Титул уже существует")
public class EwbTitleAlreadyExistsException extends BusinessException {
    
    public static final String MSG = "У ЭПЛ с id=%s уже есть %s!";
    
    public EwbTitleAlreadyExistsException(UUID id, String title) {
        super(MSG.formatted(id, title));
    }
}
