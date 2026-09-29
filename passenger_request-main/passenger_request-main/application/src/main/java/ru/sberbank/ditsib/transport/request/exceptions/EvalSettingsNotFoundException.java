package ru.sberbank.ditsib.transport.request.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Eval settings null or empty")
public class EvalSettingsNotFoundException extends RuntimeException {

    public EvalSettingsNotFoundException() {
        super("Настройки для оценки не найдены, либо некорректны");
    }
}
