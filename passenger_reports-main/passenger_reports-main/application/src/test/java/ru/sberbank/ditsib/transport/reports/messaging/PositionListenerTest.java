package ru.sberbank.ditsib.transport.reports.messaging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;
import ru.sberbank.ditsib.transport.reports.dao.PositionRepository;
import ru.sberbank.ditsib.transport.reports.dao.SharedTest;

import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EmbeddedPostgres
@Transactional
@DisplayName("Проверка получения должностей")
@MockitoBean(types = JwtDecoder.class)
public class PositionListenerTest extends SharedTest {
    @Autowired
    private Consumer<Message<PositionMessage>> input;
    @Autowired
    private PositionRepository positionRepository;
    
    @Test
    @DisplayName("Новая должность")
    void handlePosition_new() {
        var id = UUID.randomUUID();
        var message = PositionMessage.builder()
                                     .id(id)
                                     .positionName("Boss")
                                     .build();
        
        assertEquals(0, positionRepository.count());
        
        input.accept(MessageBuilder.withPayload(message).build());
        
        assertEquals(1, positionRepository.count());
        assertEquals(id, positionRepository.findAll().get(0).getId());
    }
}
