package ru.sberbank.ditsib.transport.messaging.sender.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.payout.messaging.message.RequestPayoutMessage;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestPayoutSender;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
class RequestPayoutSenderImplTest extends KafkaTest {

    @Autowired
    private RequestPayoutSender requestPayoutSender;

    @Test
    void send() {
        var message = Instancio.create(RequestPayoutMessage.class);
        requestPayoutSender.send(message);

        var actual = consumeMessage("payout.create.request-payout", RequestPayoutMessage.class);
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(message);
    }
}
