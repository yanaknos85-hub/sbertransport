package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Validation error")
public class MedicBusinessException extends BusinessException {
    
    public static final String MSG_UNIQUE_ERROR = "Медик с лицензией [series=%s, number=%s] уже существует!";
    public static final String MSG_DATES_ERROR = "Дата получение лицензии не может быть больше даты окончания срока действия лицензии! " +
                                                 "issueDate=%s, expiryDate=%s";
    
    public MedicBusinessException(String series, String number) {
        super(String.format(MSG_UNIQUE_ERROR, series, number));
    }
    
    public MedicBusinessException(LocalDate issueDate, LocalDate expiryDate) {
        super(String.format(MSG_DATES_ERROR, issueDate, expiryDate));
    }
}
