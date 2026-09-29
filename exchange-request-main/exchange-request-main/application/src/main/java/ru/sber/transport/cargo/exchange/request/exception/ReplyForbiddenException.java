package ru.sber.transport.cargo.exchange.request.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое при отсутствии доступа к отклику.
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Forbidden")
public class ReplyForbiddenException extends RuntimeException {

    /**
     * Создает новое исключение с указанным описанием.
     *
     * @param message сообщение об ошибке
     */
    public ReplyForbiddenException(String message) {
        super(message);
    }
}
