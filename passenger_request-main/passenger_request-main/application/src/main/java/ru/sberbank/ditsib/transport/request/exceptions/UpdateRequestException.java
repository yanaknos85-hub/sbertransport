package ru.sberbank.ditsib.transport.request.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Возникла ошибка при обновление данных заявки")
public class UpdateRequestException extends RuntimeException {
    public UpdateRequestException(String message) {
        super(message);
    }
}
