package ru.sber.transport.authsb.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Data not found")
public class ResponseNotFoundException extends RuntimeException {
    public static final String NOT_FOUND_RESPONSE_MSG_FORMAT = "Не получен ответ от сервиса %s";
    public static final String NOT_FOUND_FIELDS_MSG_FORMAT = "Получен ответ от сервиса %s, в котором отсутствуют поля: %s";

    public ResponseNotFoundException(String message) {
        super(String.format(NOT_FOUND_RESPONSE_MSG_FORMAT, message));
    }

    public ResponseNotFoundException(String message, String message2) {
        super(String.format(NOT_FOUND_FIELDS_MSG_FORMAT, message, message2));
    }
}
