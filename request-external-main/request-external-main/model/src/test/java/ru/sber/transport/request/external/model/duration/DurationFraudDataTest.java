package ru.sber.transport.request.external.model.duration;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class DurationFraudDataTest {

    @Test
    void testDurationFraudData() {
        final var id = UUID.randomUUID();
        final var comment = "Превышен лимит продолжительности поездки";
        final var fraud = new DurationFraudData(id, comment);

        assertThat(fraud.getId()).isEqualTo(id);
        assertThat(fraud.getComment()).isEqualTo(comment);
        assertThat(fraud.getType()).isEqualTo(DurationFraudData.DURATION_FRAUD_TYPE);
    }
}