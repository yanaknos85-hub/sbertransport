package ru.sber.transport.authentication.web.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Ошибка входа.
 */
@Getter
@RequiredArgsConstructor
@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Unauthorized")
public class LoginException extends RuntimeException {
    
    /**
     * Тип ошибки.
     */
    private final Type type;
    
    /**
     * Доступные типы ошибок.
     */
    public enum Type {

        /**
         * Неверные данные для входа.
         */
        WRONG_CREDENTIALS,

        /**
         * Неверный код второго фактора.
         */
        WRONG_TWO_FA,
    
        /**
         * Токен обновления сессии истёк.
         */
        REFRESH_EXPIRED,

        /**
         * Большое количество неудачных попыток авторизации.
         */
        TOO_MANY_LOGIN_TRIES
    }
    
}
