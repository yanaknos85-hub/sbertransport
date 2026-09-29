package ru.sberbank.transport.oto.cargo.exception;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.qa.allure.layer.layers.UnitTest;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_oto")
@ActiveProfiles("test")
@DisplayName("Тест VisibilityScopeException")
class VisibilityScopeExceptionTest {

    @ParameterizedTest
    @ValueSource(strings = {"Visibility scope violation", ""})
    @DisplayName("Создание исключения с сообщением")
    void testConstructorWithMessage(String message) {
        VisibilityScopeException exception = new VisibilityScopeException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getLocalizedMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("Создание исключения с null сообщением")
    void testConstructorWithNullMessage() {
        VisibilityScopeException exception = new VisibilityScopeException(null);

        assertThat(exception.getMessage()).isNull();
    }

    @Test
    @DisplayName("Проверка, что исключение является RuntimeException")
    void testIsRuntimeException() {
        VisibilityScopeException exception = new VisibilityScopeException("test");

        assertThat(exception).isNotNull();
    }
}
