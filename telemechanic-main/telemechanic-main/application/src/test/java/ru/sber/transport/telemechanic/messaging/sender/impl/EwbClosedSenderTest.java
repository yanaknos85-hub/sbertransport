package ru.sber.transport.telemechanic.messaging.sender.impl;

import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.messaging.sender.EwbClosedSender;
import ru.sber.transport.telemechanic.messaging.sender.message.EwbClosedMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
class EwbClosedSenderTest extends KafkaTest {

    @Autowired
    private EwbClosedSender ewbClosedSender;

    @Test
    @SneakyThrows
    void testSend() {
        var message = Instancio.of(EwbClosedMessage.class).create();
        ewbClosedSender.send(message);
        var actual = consumeMessage("service.fleet-registry.ewb_closed", EwbClosedMessage.class);
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(message);

    }
}