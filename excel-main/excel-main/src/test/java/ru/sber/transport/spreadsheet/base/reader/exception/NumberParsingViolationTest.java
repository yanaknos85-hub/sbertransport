package ru.sber.transport.spreadsheet.base.reader.exception;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Проверка отклонения парсинга чисел")
@UnitTest
@IsolatedTest
@Feature("lib_excel")
class NumberParsingViolationTest {

    @Test
    @DisplayName("Проверка")
    void test() {
        var actual = new ru.sber.transport.spreadsheet.base.reader.exception.NumberParsingViolation("field", "value");

        assertThat(actual.getMessageTemplate()).isEqualTo("[value] не является числом");
        assertThat(actual.getInvalidValue()).isEqualTo("value");
        assertThat(actual.getConstraintDescriptor()).isInstanceOf(NumberParsingViolation.NumberParsingViolationConstraintDescriptor.class);
        assertThat(actual.getConstraintDescriptor().getAnnotation().getClass().getName()).startsWith(NumberParsingViolation.class.getCanonicalName());
    }

}