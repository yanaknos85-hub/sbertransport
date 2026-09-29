package ru.sber.transport.etrn.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Исключение ошибки проверки доверенности.
 */
@Getter
public class AttorneyCheckException extends RuntimeException {

    private final HttpStatus status;

    /**
     * Конструктор с сообщением об ошибке.
     *
     * @param message сообщение об ошибке
     */
    public AttorneyCheckException(String message) {
        super(message);
        this.status = HttpStatus.CONFLICT;
    }

    /**
     * Конструктор с сообщением и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause   причина исключения
     */
    public AttorneyCheckException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.BAD_GATEWAY;
    }
}
