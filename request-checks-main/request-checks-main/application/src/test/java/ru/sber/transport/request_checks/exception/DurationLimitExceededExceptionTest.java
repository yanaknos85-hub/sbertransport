package ru.sber.transport.request_checks.exception;

import static org.assertj.core.api.Assertions.assertThat;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Проверка DurationLimitExceededException")
class DurationLimitExceededExceptionTest {

    @Test
    void testDurationLimitExceededExceptionWithMessage() {
        val message = "Превышена продолжительность поездок в 12 часов в сутки";
        val exception = new DurationLimitExceededException(message);

        assertThat(exception).isInstanceOf(DurationLimitExceededException.class);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void testDurationLimitExceededExceptionWithDifferentMessage() {
        val message = "Custom error message";
        val exception = new DurationLimitExceededException(message);

        assertThat(exception).isInstanceOf(DurationLimitExceededException.class);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void testDurationLimitExceededExceptionInheritance() {
        val exception = new DurationLimitExceededException("Test message");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

}
