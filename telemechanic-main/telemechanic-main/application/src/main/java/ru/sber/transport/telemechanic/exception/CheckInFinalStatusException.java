package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Check in final status")
public class CheckInFinalStatusException extends BusinessException {

    public CheckInFinalStatusException() {
        super("Проверка находится в финальном статусе");
    }

}
