package ru.sberbank.ditsib.transport.vehicle.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT, reason = "Non unique attorney")
public class NonUniqueAttorneyException extends RuntimeException {
    public NonUniqueAttorneyException() {
        super("Доверенность с заданными данными уже существует в системе");
    }
}
