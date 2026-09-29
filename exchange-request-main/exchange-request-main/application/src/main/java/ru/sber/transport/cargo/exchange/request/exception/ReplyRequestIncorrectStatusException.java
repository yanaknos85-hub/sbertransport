package ru.sber.transport.cargo.exchange.request.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое, если заявка находится в некорректном статусе.
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Request in incorrect status")
public class ReplyRequestIncorrectStatusException extends RuntimeException {

    /**
     * Создает новое исключение с указанным описанием.
     *
     * @param message сообщение об ошибке
     */
    public ReplyRequestIncorrectStatusException(String message) {
        super(message);
    }
}
