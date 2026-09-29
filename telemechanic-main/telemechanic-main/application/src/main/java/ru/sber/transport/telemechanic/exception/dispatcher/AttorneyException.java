package ru.sber.transport.telemechanic.exception.dispatcher;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sber.transport.telemechanic.exception.BusinessException;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Attorney exception")
public class AttorneyException extends BusinessException {

    public static final String NOT_UNIQUE_EMPLOYEE_MSG = "В системе существует доверенность на сотрудника ИД: %s, для организации ИД: %s";
    public static final String NOT_UNIQUE_ATTORNEY_MSG = "В системе существует доверенность с номером: %s, для организации ИД: %s";
    public static final String ATTORNEY_EXPIRED = "Истек срок действия доверенности (МЧД). ID доверенности:%s";
    
    public AttorneyException(String msg) {
        super(msg);
    }
}
