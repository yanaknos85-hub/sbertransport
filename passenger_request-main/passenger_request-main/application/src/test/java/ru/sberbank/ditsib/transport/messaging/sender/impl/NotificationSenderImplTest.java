package ru.sberbank.ditsib.transport.messaging.sender.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.ws.messages.WebSocketMessage;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.messaging.senders.NotificationSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
class NotificationSenderImplTest extends KafkaTest {

    @Autowired
    private NotificationSender notificationSender;

    @Test
    void send() {
        var message = Instancio.of(WebSocketMessage.class)
                .set(field(WebSocketMessage::payload), "some info")
                .create();
        notificationSender.sendNotificationToSubscriber(message);

        var actual = consumeMessage("sessions_subscriptions_notifications", WebSocketMessage.class);
        assertThat(actual)
                .extracting(
                        WebSocketMessage::subscription,
                        WebSocketMessage::resourceId,
                        WebSocketMessage::payload
                )
                .containsExactly(
                        message.subscription(),
                        message.resourceId(),
                        message.payload()
                );
    }
}
