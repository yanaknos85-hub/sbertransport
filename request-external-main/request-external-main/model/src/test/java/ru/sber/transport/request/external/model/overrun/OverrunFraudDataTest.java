package ru.sber.transport.request.external.model.overrun;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class OverrunFraudDataTest {

    @Test
    void testOverrunFraudData() {
        final var id = UUID.randomUUID();
        final var comment = "Превышен лимит километража";
        final var fraud = new OverrunFraudData(id, comment);

        assertThat(fraud.getId()).isEqualTo(id);
        assertThat(fraud.getComment()).isEqualTo(comment);
        assertThat(fraud.getType()).isEqualTo(OverrunFraudData.FRAUD_TYPE);
    }
}
