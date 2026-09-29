package ru.sberbank.ditsib.transport.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Исключение для неверного формата данных */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Wrong format")
public class IllegalFormatResonseException extends RuntimeException{

    /**
     * Фаблон сообщения неверного формата.
     */
    public static final String MSG_FORMAT = "Bad format for %s. Data was: %s";
    
    /**
     * Create a new exception.
     *
     * @param format формат
     * @param data  данные
     */
    public IllegalFormatResonseException(String format, String data) {
        super(String.format(MSG_FORMAT, format,data));
    }

    /**
     * Создать исключение.
     *
     * @param message сообщение исключения.
     */
    public IllegalFormatResonseException(String message) {
        super(message);
    }
}
