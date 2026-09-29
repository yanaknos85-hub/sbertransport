package ru.sberbank.ditsib.transport.vehicle.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Error in dates")
public class AttorneyWrongDatesException extends RuntimeException {
    public AttorneyWrongDatesException() {
        super("Дата окончания срока действия не может быть раньше Даты выдачи");
    }
}
