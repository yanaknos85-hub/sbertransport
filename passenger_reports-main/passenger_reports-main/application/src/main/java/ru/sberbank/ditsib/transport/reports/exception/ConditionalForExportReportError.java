package ru.sberbank.ditsib.transport.reports.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * IO ошибка при экспорте отчета в файл
 */
@ResponseStatus(value = HttpStatus.PRECONDITION_FAILED)
public class ConditionalForExportReportError extends RuntimeException {
    
    public static final String MESSAGE = "Выгрузка превышает допустимые объемы. Уменьшите выбранные параметры или период";
    
    public ConditionalForExportReportError() {
        super(MESSAGE);
    }
}