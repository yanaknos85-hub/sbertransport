package ru.sber.transport.request.external.model.overrun;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class OverrunCheckResultTest {

    @Test
    void testOverrunCheckResult() {
        final var comment = "Превышен лимит километража";
        final var totalDistance = 150;
        final var result = new OverrunCheckResult(comment, totalDistance);

        assertThat(result.comment()).isEqualTo(comment);
        assertThat(result.totalDistance()).isEqualTo(totalDistance);
    }

    @Test
    void testOverrunCheckResultWithEmptyComment() {
        final var comment = "";
        final var totalDistance = 0;
        final var result = new OverrunCheckResult(comment, totalDistance);

        assertThat(result.comment()).isEmpty();
        assertThat(result.totalDistance()).isEqualTo(totalDistance);
    }

    @Test
    void testOverrunCheckResultWithNullComment() {
        final String comment = null;
        final var totalDistance = 100;
        final var result = new OverrunCheckResult(comment, totalDistance);

        assertThat(result.comment()).isNull();
        assertThat(result.totalDistance()).isEqualTo(totalDistance);
    }
}
