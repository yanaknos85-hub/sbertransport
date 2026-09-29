package ru.sberbank.ditsib.transport.exceptions;

import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.ConstraintViolation;
import java.util.HashSet;
import java.util.Set;

/**
 * Ошибка приложения.
 */
@NoArgsConstructor
public class ApplicationException extends RuntimeException {

    /**
     * Ограничения.
     */
    @Getter
    private final Set<ConstraintViolation<?>> violations = new HashSet<>();
    
    /**
     * Создать исключение.
     *
     * @param message сообщение.
     */
    public ApplicationException(String message) {
        super(message);
    }
    
    /**
     * Создать исключение.
     *
     * @param cause корневое исключение.
     */
    public ApplicationException(Throwable cause) {
        super(cause);
    }
    
    /**
     * Создать исключение.
     *
     * @param message сообщение.
     * @param cause корневое исключение.
     */
    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Создать исключение.
     *
     * @param violations ограничения.
     */
    public ApplicationException(Set<ConstraintViolation<?>> violations) {
        setViolations(violations);
    }

    /**
     * Установка ограничений.
     *
     * @param violations ограничения.
     */
    public void setViolations(Set<ConstraintViolation<?>> violations) {
        this.violations.addAll(violations);
    }
}