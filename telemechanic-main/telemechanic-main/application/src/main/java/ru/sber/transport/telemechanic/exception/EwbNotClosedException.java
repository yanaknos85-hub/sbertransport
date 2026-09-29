package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Незакрытый ЭПЛ")
public class EwbNotClosedException extends BusinessException {
    
    public static final String MSG = "У водителя с табельным номером: %s есть незакрытый ЭПЛ на дату %s";
    
    public EwbNotClosedException(String personnelNumber, String date) {
        super(MSG.formatted(personnelNumber, date));
    }
}
