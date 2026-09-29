package ru.sber.transport.journal.exception;

/**
 * Абстрактная бизнес-ошибка
 */
public abstract class BusinessException extends RuntimeException {
    protected BusinessException(String message) {
        super(message);
    }
}