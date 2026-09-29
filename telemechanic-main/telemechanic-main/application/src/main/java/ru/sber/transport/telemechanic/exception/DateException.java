package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Date error")
public class DateException extends BusinessException {
    
    public static final String ISSUE_DATE_AFTER_NOW = "Дата выдачи доверенности должна быть меньше или равна текущей дате";
    public static final String EXPIRY_DATE_BEFORE_NOW = "Дата окончания срока действия  доверенности должна быть больше или равна текущей дате";
    
    public DateException(String msg) {
        super(msg);
    }
}
