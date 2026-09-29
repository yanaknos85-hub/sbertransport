package ru.sber.transport.spreadsheet.base.reader.exception;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.spreadsheet.base.reader.annotation.NumberParsingViolation;
import ru.sber.transport.spreadsheet.base.reader.exception.CustomConstraintDescriptor;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Кастомный дескриптор")
@UnitTest
@IsolatedTest
@Feature("lib_excel")
class CustomConstraintDescriptorTest {

    @Test
    @DisplayName("Проверка")
    void test() {
        var actual = new CustomConstraintDescriptor<NumberParsingViolation>() {

            @Override
            public NumberParsingViolation getAnnotation() {
                return null;
            }
        };

        assertThat(actual.getMessageTemplate()).isNull();
        assertThat(actual.getValidationAppliesTo()).isNull();
        assertThat(actual.isReportAsSingleViolation()).isFalse();
        assertThat(actual.getValueUnwrapping()).isNull();
        assertThat(actual.<Object>unwrap(null)).isNull();
        assertThat(actual.getGroups()).isEmpty();
        assertThat(actual.getPayload()).isEmpty();
        assertThat(actual.getConstraintValidatorClasses()).isEmpty();
        assertThat(actual.getAttributes()).isEmpty();
        assertThat(actual.getComposingConstraints()).isEmpty();
    }

}