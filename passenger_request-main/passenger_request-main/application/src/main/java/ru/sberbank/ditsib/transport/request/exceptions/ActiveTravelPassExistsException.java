package ru.sberbank.ditsib.transport.request.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.CONFLICT;

/**
 * Ошибка при покупке билета на общественный транспорт при еще активном проездном
 */
@ResponseStatus(value = CONFLICT, reason = "Ошибка валидации запроса")
public class ActiveTravelPassExistsException extends BusinessException {

    public ActiveTravelPassExistsException(String message) {
        super(message);
    }

    public ActiveTravelPassExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
