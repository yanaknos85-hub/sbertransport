package ru.sber.transport.spreadsheet.base.reader.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Исключение валидации заголовка таблицы.
 */
@RequiredArgsConstructor
@Getter
public class HeaderValidationException extends RuntimeException {

    private final List<String> brokenColumn;
}
