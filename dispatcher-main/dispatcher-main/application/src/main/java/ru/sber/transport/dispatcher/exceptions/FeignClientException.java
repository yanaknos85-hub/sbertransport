package ru.sber.transport.dispatcher.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Feign client return client errror")
public class FeignClientException extends RuntimeException {

    public FeignClientException(String message, Exception e) {
        super(message, e);
    }
}
