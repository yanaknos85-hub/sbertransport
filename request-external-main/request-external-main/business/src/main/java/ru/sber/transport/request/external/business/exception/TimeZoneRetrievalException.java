package ru.sber.transport.request.external.business.exception;


/**
 * Исключение для ситуации, когда провайдер геозон не предоставил информацию о таймзоне
 */
public class TimeZoneRetrievalException extends RuntimeException {

    public TimeZoneRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}