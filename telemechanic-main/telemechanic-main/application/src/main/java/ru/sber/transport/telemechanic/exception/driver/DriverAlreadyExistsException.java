package ru.sber.transport.telemechanic.exception.driver;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sber.transport.telemechanic.exception.BusinessException;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Driver already exists.")
public class DriverAlreadyExistsException extends BusinessException {
    
    public static final String TIN_SNILS_EXISTS = "В системе существует сотрудник с ИНН - %s, СНИЛС - %s";
    public static final String SERIES_NUMBER_EXISTS = "В системе существует сотрудник с серией - %s, номером - %s";
    
    public DriverAlreadyExistsException(String msg) {
        super(msg);
    }
}
