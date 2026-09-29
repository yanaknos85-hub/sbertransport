package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Wrong file counts in request")
public class CheckPhotoCountException extends BusinessException {
    
    public CheckPhotoCountException() {
        super("Тип проверки не поддерживает количество фото, которое было передано в запросе.");
    }
}
