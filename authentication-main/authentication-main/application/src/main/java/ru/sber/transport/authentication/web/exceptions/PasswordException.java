package ru.sber.transport.authentication.web.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Collection;

/**
 * Ошибка смены пароля.
 */
@Getter
@RequiredArgsConstructor
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Password changing failed")
public class PasswordException extends RuntimeException {
    
    /**
     * Проваленные проверки.
     */
    private final Collection<String> checks;
    
}
