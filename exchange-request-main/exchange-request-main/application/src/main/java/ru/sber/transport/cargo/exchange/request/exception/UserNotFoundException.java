package ru.sber.transport.cargo.exchange.request.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое при отсутствии пользователя в системе.
 * <p>
 * Используется в сервисах и контроллерах, когда пользователь с указанным идентификатором
 * не найден в базе данных (например, по tokenId или id).
 * </p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {

    /**
     * Создаёт исключение с сообщением об ошибке.
     *
     * @param message описание ошибки
     */
    public UserNotFoundException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с причиной и сообщением.
     *
     * @param message описание ошибки
     * @param cause   причина исключения
     */
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
