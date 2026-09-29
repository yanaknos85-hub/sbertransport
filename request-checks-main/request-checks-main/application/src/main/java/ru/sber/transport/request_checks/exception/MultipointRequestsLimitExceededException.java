package ru.sber.transport.request_checks.exception;

/**
 * Исключение, выбрасываемое при превышении лимита многоточечных поездок
 */
public class MultipointRequestsLimitExceededException extends RuntimeException {

    public MultipointRequestsLimitExceededException(String message) {
        super(message);
    }

}
