package ru.sberbank.ditsib.transport.request.exceptions;

public class DocumentsValidationTimeoutException extends RuntimeException {

    public DocumentsValidationTimeoutException(String message) {
        super(message);
    }
}