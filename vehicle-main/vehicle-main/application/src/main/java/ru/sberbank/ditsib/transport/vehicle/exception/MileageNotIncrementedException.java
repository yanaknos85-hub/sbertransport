package ru.sberbank.ditsib.transport.vehicle.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Значение пробега должно быть больше предыдущего")
public class MileageNotIncrementedException extends RuntimeException {
    public MileageNotIncrementedException(String message) {
        super(message);
    }
}
