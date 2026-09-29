package ru.sber.transport.request.external.providers.exceptions;

/**
 * Ошибка взаимодействия со слоем данных
 */
public class DatabaseLayerException extends RuntimeException {

    public DatabaseLayerException(String message) {
        super(message);
    }

    public DatabaseLayerException(Throwable cause) {
        super(cause);
    }
}
