package ru.sberbank.ditsib.transport.messaging.sender.impl;

import org.assertj.core.api.Assertions;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.messaging.message.CarLocationMessage;
import ru.sberbank.ditsib.transport.request.messaging.senders.CarLocationSender;

@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
class CarLocationSenderImplTest extends KafkaTest {

    @Autowired
    private CarLocationSender carLocationSender;

    @Test
    void send() {
        var message = Instancio.create(CarLocationMessage.class);
        carLocationSender.send(message);

        var actual = consumeMessage("service.request.car-location-request", CarLocationMessage.class);
        Assertions.assertThat(actual)
                .isNotNull()
                .extracting(
                        CarLocationMessage::id,
                        CarLocationMessage::contractorRequests
                )
                .containsExactly(
                        message.id(),
                        message.contractorRequests()
                );

    }
}
