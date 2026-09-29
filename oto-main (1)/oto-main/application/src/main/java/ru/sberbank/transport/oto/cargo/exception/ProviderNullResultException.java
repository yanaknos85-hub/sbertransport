package ru.sberbank.transport.oto.cargo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Провайдер вернул null результат
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Provider applied null value")
public class ProviderNullResultException extends RuntimeException {
    private final static String MESSAGE = "Ошибка при получении результата из колонки %s";
    
    public ProviderNullResultException(String column) {
        super(String.format(MESSAGE, column));
    }
}
