package ru.sber.transport.spreadsheet.base.reader.exception;

import io.qameta.allure.Feature;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Кастомное отклонение")
@UnitTest
@IsolatedTest
@Feature("lib_excel")
class CustomViolationTest {

    @Test
    @DisplayName("Проверка дефолтов")
    void test() {
        var actual = new CustomViolation() {

            @Override
            public String getMessage() {
                return "MSG";
            }

            @Override
            public String getMessageTemplate() {
                return "Template";
            }

            @Override
            public Path getPropertyPath() {
                return new FieldPath("Name");
            }

            @Override
            public Object getInvalidValue() {
                return 15;
            }

            @Override
            public ConstraintDescriptor<?> getConstraintDescriptor() {
                return null;
            }
        };

        assertThat(actual.getRootBean()).isNull();
        assertThat(actual.getRootBeanClass()).isNull();
        assertThat(actual.getLeafBean()).isNull();
        assertThat(actual.getExecutableReturnValue()).isNull();
        assertThat(actual.<Object>unwrap(null)).isNull();
        assertThat(actual.getExecutableParameters()).isEmpty();
        assertThat(actual.getMessage()).isEqualTo("MSG");
        assertThat(actual.getMessageTemplate()).isEqualTo("Template");
        assertThat(actual.getPropertyPath()).isInstanceOf(CustomViolation.FieldPath.class);
        assertThat(((CustomViolation.FieldPath) actual.getPropertyPath()).fieldName()).isEqualTo("Name");
        assertThat(actual.getPropertyPath().iterator().next().isInIterable()).isFalse();
        assertThat(actual.getPropertyPath().iterator().next().getIndex()).isNull();
        assertThat(actual.getPropertyPath().iterator().next().getKey()).isNull();
        assertThat(actual.getPropertyPath().iterator().next().getKind()).isNull();
        assertThat(actual.getPropertyPath().iterator().next().as(Path.Node.class)).isNull();
    }

}