package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.PRECONDITION_FAILED, reason = "First check not in finish status")
public class FirstCheckNotFinishStatusException extends BusinessException {
    
    public FirstCheckNotFinishStatusException() {
        super("Проверка на автомобильный номер не в финальном статусе");
    }

}
