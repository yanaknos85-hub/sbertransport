package ru.sber.transport.cargo.exchange.request.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое при отсутствии заявки с указанным идентификатором
 * или при попытке доступа к чужой заявке.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Data not found")
public class RequestNotFoundException extends RuntimeException {

    /**
     * Создает новое исключение с указанным описанием.
     *
     * @param message сообщение об ошибке
     */
    public RequestNotFoundException(String message) {
        super(message);
    }

    /**
     * Создает новое исключение с указанным описанием и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause   причина исключения
     */
    public RequestNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
