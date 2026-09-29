package ru.sberbank.ditsib.transport.vehicle.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Переданные типы топлива не соответствуют типу двигателя")
public class FuelEngineTypesRelationValidationException extends RuntimeException {
    public FuelEngineTypesRelationValidationException(String message) {
        super(message);
    }
}
