package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class OdmeterValueNotAcceptedException extends RuntimeException {
    public OdmeterValueNotAcceptedException(String message) {
        super(message);
    }
}
