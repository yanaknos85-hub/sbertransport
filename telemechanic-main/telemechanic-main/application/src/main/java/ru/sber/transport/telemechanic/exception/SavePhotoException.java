package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Save photo exception")
public class SavePhotoException extends BusinessException {

    public SavePhotoException(String message) {
        super(message);
    }
}
