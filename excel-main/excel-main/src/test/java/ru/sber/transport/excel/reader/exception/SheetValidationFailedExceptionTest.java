package ru.sber.transport.excel.reader.exception;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.spreadsheet.excel.Column;
import ru.sber.transport.spreadsheet.base.reader.exception.DateParsingViolation;
import ru.sber.transport.spreadsheet.base.reader.exception.SheetValidationFailedException;
import ru.sber.transport.spreadsheet.model.ValidationError;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Формирование исключения")
@UnitTest
@IsolatedTest
@Feature("lib_excel")
class SheetValidationFailedExceptionTest {

    @Test
    @DisplayName("Проверка")
    void test() {
        var violation = Instancio.create(DateParsingViolation.class);
        var actual = new SheetValidationFailedException("name", List.of(new ValidationError(1, Map.of(Column.builder().field("field").name("name").build(), Set.of(violation)))));

        assertThat(actual.getMessage()).isEqualTo(
            """
            Read validation of sheet 'name' failed:
            Row number %s
            name %s %s
            """.formatted(1, violation.getInvalidValue(), "DataParsingViolation")
        );
    }

}