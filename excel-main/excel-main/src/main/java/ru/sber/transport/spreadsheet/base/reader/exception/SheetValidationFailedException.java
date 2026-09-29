package ru.sber.transport.spreadsheet.base.reader.exception;

import jakarta.validation.ConstraintViolation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sber.transport.spreadsheet.excel.Column;
import ru.sber.transport.spreadsheet.model.ValidationError;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Исключение валидации страницы.
 */
@RequiredArgsConstructor
@Getter
public class SheetValidationFailedException extends RuntimeException {

    private final String sheetName;

    private final transient List<ValidationError> errors;

    @Override
    public String getMessage() {
        return """
            Read validation of sheet '%s' failed:
            %s""".formatted(sheetName, getErrors(errors));
    }

    private String getErrors(List<ValidationError> errors) {
        return errors.stream().map(this::getError).collect(Collectors.joining(System.lineSeparator()));
    }

    private String getError(ValidationError error) {
        return """
            Row number %s
            %s""".formatted(error.getRowNumber(), error.getViolations().entrySet().stream().map(this::getViolations).collect(Collectors.joining()));
    }

    private String getViolations(Map.Entry<Column<?,?>, Set<ConstraintViolation<?>>> violation) {
        return """
            %s %s
            """.formatted(violation.getKey().getName(), renderViolations(violation.getValue()));
    }

    private String renderViolations(Set<ConstraintViolation<?>> value) {
        return value.stream().map(v -> "%s %s".formatted(v.getInvalidValue(), v.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName())).collect(Collectors.joining("; "));
    }
}
