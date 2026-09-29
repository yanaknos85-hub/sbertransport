package ru.sber.transport.request_checks.exception;

/**
 * Исключение, выбрасываемое при превышении лимита длительности поездок
 */
public class DurationLimitExceededException extends RuntimeException {

    public DurationLimitExceededException(String message) {
        super(message);
    }

}
