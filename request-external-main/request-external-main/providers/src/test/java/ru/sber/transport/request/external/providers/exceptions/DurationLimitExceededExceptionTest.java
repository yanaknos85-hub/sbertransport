package ru.sber.transport.request.external.providers.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

@UnitTest
@IsolatedTest
@Isolated
@DisplayName("Проверка DurationLimitExceededException")
class DurationLimitExceededExceptionTest {

    @Test
    @DisplayName("Проверка конструктора исключения")
    void test_constructor() {
        var exception = new DurationLimitExceededException();

        assertThat(exception.getMessage()).isEqualTo("Превышен лимит длительности поездок");
    }

}