package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Ошибка при работе с клиентом")
public class TelemedicClientException extends BusinessException {
    
    public TelemedicClientException(String message) {
        super(message);
    }
}
