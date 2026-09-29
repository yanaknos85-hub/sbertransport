package ru.sberbank.ditsib.transport.reports.messaging.listeners.impl;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.messaging.messages.trip.SharedRideMessage;
import ru.sberbank.ditsib.transport.reports.dao.SharedRideRepository;

import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@EmbeddedPostgres
@MockitoBean(types = JwtDecoder.class)
class SharedRideListenerImplTest extends KafkaTest {

    @Autowired
    private Consumer<Message<SharedRideMessage>> sharedRideInput;
    @Autowired
    private SharedRideRepository sharedRideRepository;

    @Sql({ "/scripts/cleanup_database.sql", "/scripts/shared_ride.sql"})
    @Test
    void handle() {
        var message = Instancio.of(SharedRideMessage.class)
                .set(Select.field(SharedRideMessage::getId), UUID.fromString("7fc0e474-d55a-4e9c-b5a8-7819aaf59c89"))
                .create();
        sharedRideInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(sharedRideRepository.count()).isEqualTo(1);
    }
}