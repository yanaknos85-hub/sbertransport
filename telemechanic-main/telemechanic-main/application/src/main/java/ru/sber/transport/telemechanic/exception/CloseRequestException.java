package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Wrong request status")
public class CloseRequestException extends BusinessException{
    
    public static final String MSG_STATUS = "Заявку можно закрыть только в статусе \"На линии\"";
    public static final String MSG_AUTHOR = "Заявку может закрыть только автор";

    public CloseRequestException(String message) {
        super(message);
    }
}
