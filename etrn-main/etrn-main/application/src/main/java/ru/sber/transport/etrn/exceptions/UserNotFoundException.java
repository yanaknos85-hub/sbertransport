package ru.sber.transport.etrn.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое, если пользователь не найден.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Пользователь не найден")
public class UserNotFoundException extends RuntimeException {

    public static final String MSG_FORMAT = "Пользователь с ID %s не найден";

    /**
     * Создает новое исключение.
     *
     * @param userId ID запрашиваемого пользователя.
     */
    public UserNotFoundException(java.util.UUID userId) {
        super(String.format(MSG_FORMAT, userId));
    }
}
