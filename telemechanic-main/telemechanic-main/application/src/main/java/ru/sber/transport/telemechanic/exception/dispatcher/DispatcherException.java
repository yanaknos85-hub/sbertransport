package ru.sber.transport.telemechanic.exception.dispatcher;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sber.transport.telemechanic.exception.BusinessException;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Dispatcher error")
public class DispatcherException extends BusinessException {
    
    public static final String NOT_FOUND_MSG = "Не найден диспетчер ID %s";
    public static final String NOT_IN_DEPARTMENT_MSG = "Сотрудник %s не найден в подразделении с id:%s";
    public static final String EDIT_NOT_ACTIVE_MSG = "Нельзя отредактировать. Неактивна запись для диспетчера ID:%s";
    public static final String NOT_ACTIVE_MSG = "Неактивна запись для диспетчера ID:%s";
    public static final String NOT_FOUND_BY_USER_ID_MSG = "Сотрудник не является диспетчером. user id сотрудника :%s";
    public static final String NOT_FOUND_BY_ATTORNEY_NUMBER_MSG = "Не найден диспетчер с attorneyNumber %s";

    public DispatcherException(String msg) {
        super(msg);
    }
}
