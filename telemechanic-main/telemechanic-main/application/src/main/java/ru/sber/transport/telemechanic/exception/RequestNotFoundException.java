package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Request not found")
public class RequestNotFoundException extends BusinessException {

    public static final String MSG_FORMAT = "Заявка не найдена, id:'%s'";

    /**
     * Create a new exception.
     *
     * @param request ID of requested Request.
     */
    public RequestNotFoundException(UUID request) {
        super(String.format(MSG_FORMAT, request));
    }
}
