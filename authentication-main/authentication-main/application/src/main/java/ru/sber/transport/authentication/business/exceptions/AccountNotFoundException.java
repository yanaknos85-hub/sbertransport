package ru.sber.transport.authentication.business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Исключение, выбрасываемое если аккаунт не найден.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class AccountNotFoundException extends RuntimeException{
    
    /**
     * Создать исключение.
     *
     * @param login искомый логин.
     */
    public AccountNotFoundException(String login) {
        super(String.format("AccountDto with login '%s' not found", login));
    }
    
    /**
     * Создать исключение.
     *
     * @param userId идентификатор пользователя.
     */
    public AccountNotFoundException(UUID userId) {
        super(String.format("AccountDto with ID '%s' not found", userId));
    }
}
