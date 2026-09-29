package ru.sber.transport.cargo.exchange.request.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое при наличии в системе отклика от того же перевозчика на ту же заявку.
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Duplicate reply from same carrier for same request")
public class ReplyDuplicatedException extends RuntimeException {

    /**
     * Создает новое исключение с указанным описанием.
     *
     * @param message сообщение об ошибке
     */
    public ReplyDuplicatedException(String message) {
        super(message);
    }
}
