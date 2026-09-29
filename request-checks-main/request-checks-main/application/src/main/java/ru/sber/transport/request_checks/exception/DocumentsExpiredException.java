package ru.sber.transport.request_checks.exception;

import java.util.List;

/**
 * Исключение, выбрасываемое при просроченных документах (409 Conflict).
 */
public class DocumentsExpiredException extends RuntimeException {

    private final List<String> errors;

    public DocumentsExpiredException(List<String> errors) {
        super("Документы просрочены: " + String.join(", ", errors));
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }

}