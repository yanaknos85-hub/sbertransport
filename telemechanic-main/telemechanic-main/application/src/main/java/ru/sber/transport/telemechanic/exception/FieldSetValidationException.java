package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "FieldSet validation exception")
public class FieldSetValidationException extends BusinessException {
    
    public static final String MSG = "Набор полей пуст, или не задан";
    
    public FieldSetValidationException() {
        super(MSG);
    }
}
