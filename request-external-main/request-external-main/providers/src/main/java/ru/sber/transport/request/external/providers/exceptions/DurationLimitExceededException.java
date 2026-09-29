package ru.sber.transport.request.external.providers.exceptions;

/**
 * Превышен лимит длительности поездок
 */
public class DurationLimitExceededException extends RuntimeException {

    /**
     * Конструктор исключения
     */
    public DurationLimitExceededException() {
        super("Превышен лимит длительности поездок");
    }

}