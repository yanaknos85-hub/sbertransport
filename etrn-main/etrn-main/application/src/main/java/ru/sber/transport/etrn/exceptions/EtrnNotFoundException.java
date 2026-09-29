package ru.sber.transport.etrn.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EtrnNotFoundException extends RuntimeException {

    public EtrnNotFoundException(String humanReadableId) {
        super("ЭТрН " + humanReadableId + " не найдена");
    }
}
