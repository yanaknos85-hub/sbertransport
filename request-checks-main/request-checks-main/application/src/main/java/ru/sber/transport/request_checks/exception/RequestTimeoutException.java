package ru.sber.transport.request_checks.exception;

/**
 * Исключение, выбрасываемое при истечении времени ожидания ответа внешнего сервиса (408 Request Timeout).
 */
public class RequestTimeoutException extends RuntimeException {

    public RequestTimeoutException(String message) {
        super(message);
    }

}