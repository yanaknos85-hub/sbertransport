package ru.sberbank.ditsib.transport.request.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Ошибка валидации запроса")
public class EmployeeAbsenceException extends BusinessException {

    private static final String MESSAGE = "Невозможно создать заявку";

    public EmployeeAbsenceException() {
        super(MESSAGE);
    }
}
