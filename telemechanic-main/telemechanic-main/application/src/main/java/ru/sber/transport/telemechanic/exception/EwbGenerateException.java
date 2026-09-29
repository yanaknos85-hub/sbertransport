package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "xml generation error")
public class EwbGenerateException extends BusinessException {
    
    public EwbGenerateException() {
        super("При формировании xml произошла ошибка!");
    }
}
