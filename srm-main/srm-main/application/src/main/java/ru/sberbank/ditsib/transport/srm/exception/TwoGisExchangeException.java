package ru.sberbank.ditsib.transport.srm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "2 gis exchange error")
public class TwoGisExchangeException extends RuntimeException {

    public TwoGisExchangeException() {
        super("requestDistanceMatrixAndGetResult: Empty response");
    }

}
