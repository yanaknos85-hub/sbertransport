package ru.sber.transport.request.external.model.singletripduration;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SingleTripDurationFraudDataTest {

    @Test
    void testSingleTripDurationFraudData() {
        final var id = UUID.randomUUID();
        final var comment = "Превышен лимит длительности одной заявки";
        final var fraud = new SingleTripDurationFraudData(id, comment);

        assertThat(fraud.getId()).isEqualTo(id);
        assertThat(fraud.getComment()).isEqualTo(comment);
        assertThat(fraud.getType()).isEqualTo(SingleTripDurationFraudData.SINGLE_TRIP_DURATION_FRAUD_TYPE);
    }
}
