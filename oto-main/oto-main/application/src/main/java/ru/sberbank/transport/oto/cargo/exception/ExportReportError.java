package ru.sberbank.transport.oto.cargo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.persistence.NonUniqueResultException;

/**
 * IO ошибка при экспорте отчета в файл
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class ExportReportError extends RuntimeException {
    public ExportReportError(String message, Throwable cause) {
        super(message,cause);
    }
}