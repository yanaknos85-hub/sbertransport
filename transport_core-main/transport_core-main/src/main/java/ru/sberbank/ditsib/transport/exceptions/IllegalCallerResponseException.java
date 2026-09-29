package ru.sberbank.ditsib.transport.exceptions;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение неавторизованного доступа.
 */
@NoArgsConstructor
@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Action not allowed for user")
public class IllegalCallerResponseException extends RuntimeException {

    /**
     * Создание исключения.
     *
     * @param message сообщение исключения.
     */
    public IllegalCallerResponseException(String message) {
        super(message);
    }
}
