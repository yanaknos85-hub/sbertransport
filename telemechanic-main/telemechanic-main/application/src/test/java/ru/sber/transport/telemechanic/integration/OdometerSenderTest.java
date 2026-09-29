package ru.sber.transport.telemechanic.integration;

import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.messaging.sender.OdometerSender;
import ru.sber.transport.telemechanic.messaging.sender.message.OdometerHistoryValueMessage;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@EmbeddedPostgres
class OdometerSenderTest extends KafkaTest {
    
    @Autowired
    private OdometerSender sender;
    
    @Test
    @SneakyThrows
    void send() {
        var message = Instancio.create(OdometerHistoryValueMessage.class);
        var ewbId = UUID.randomUUID();
        var metaAttributes = Map.of("EWB_ID", ewbId,
                                    "DIRECTION", "IN");
        message.metaAttributes().clear();
        message.metaAttributes().putAll(metaAttributes);
        var expectedMetaAttributes = Map.of("EWB_ID", ewbId.toString(),
                                            "DIRECTION", "IN");
        sender.send(message);
        var actual = consumeMessage("service.odometer.history", OdometerHistoryValueMessage.class);
        assertThat(actual)
                .isNotNull()
                .extracting(OdometerHistoryValueMessage::transportId,
                            OdometerHistoryValueMessage::value,
                            OdometerHistoryValueMessage::creatorUserId,
                            OdometerHistoryValueMessage::creationTime,
                            OdometerHistoryValueMessage::metaAttributes)
                .containsExactly(message.transportId(),
                                 message.value(),
                                 message.creatorUserId(),
                                 message.creationTime(),
                                 expectedMetaAttributes
                                );
    }
}