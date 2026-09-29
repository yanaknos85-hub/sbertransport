package ru.sber.transport.spreadsheet.model;

import jakarta.validation.ConstraintViolation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.sber.transport.spreadsheet.excel.Column;

import java.util.Map;
import java.util.Set;

/**
 * Ошибки валидации данных файла.
 */
@Getter
@RequiredArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public final class ValidationError extends RuntimeException {

    @ToString.Include
    private final int rowNumber;

    @ToString.Include
    private final transient Map<Column<?, ?>, Set<ConstraintViolation<?>>> violations;
}
