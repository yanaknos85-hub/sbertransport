package ru.sberbank.ditsib.transport.request.exceptions;

import lombok.Getter;

import java.util.List;

/**
 * Исключение выбрасывается, когда валидация корпоративных документов завершилась неудачей.
 * Сервис corporate-documents вернул, что документы невалидны.
 */
@Getter
public class DocumentsValidationFailedException extends RuntimeException {

    private final List<String> errors;

    /**
     * Конструктор для одиночной ошибки валидации.
     *
     * @param error сообщение об ошибке
     */
    public DocumentsValidationFailedException(String error) {
        super("Document validation failed: " + error);
        this.errors = List.of(error);
    }

    /**
     * Конструктор для списка ошибок валидации.
     *
     * @param errors список сообщений об ошибках
     */
    public DocumentsValidationFailedException(List<String> errors) {
        super("Document validation failed: " + String.join(", ", errors));
        this.errors = errors;
    }
}