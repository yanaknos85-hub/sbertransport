package ru.sber.transport.cargo.exchange.request.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое при отсутствии отклика с указанным идентификатором
 * или при попытке доступа к чужому отклику.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Data not found")
public class ReplyNotFoundException extends RuntimeException {

    /**
     * Создает новое исключение с указанным описанием.
     *
     * @param message сообщение об ошибке
     */
    public ReplyNotFoundException(String message) {
        super(message);
    }
}
