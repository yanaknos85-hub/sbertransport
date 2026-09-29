package ru.sber.transport.cargo.exchange.request.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое при ошибках валидации заявки.
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Validation exception")
public class RequestValidationException extends RuntimeException {

    /**
     * Создает новое исключение с указанным описанием.
     *
     * @param message сообщение об ошибке
     */
    public RequestValidationException(String message) {
        super(message);
    }

    /**
     * Создает новое исключение с указанным описанием и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause   причина исключения
     */
    public RequestValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
