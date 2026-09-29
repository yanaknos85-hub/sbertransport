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
import ru.sberbank.ditsib.transport.messaging.messages.TripPurposeMessage;
import ru.sberbank.ditsib.transport.reports.dao.SharedTest;
import ru.sberbank.ditsib.transport.reports.dao.TripPurposeRepository;
import ru.sberbank.ditsib.transport.reports.mappers.TripPurposeMapper;
import ru.sberbank.ditsib.transport.reports.model.TripPurpose;
import ru.sberbank.ditsib.transport.reports.service.TripPurposeService;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({ "OptionalGetWithoutIsPresent" })
@SpringBootTest
@EmbeddedPostgres
@DisplayName("Проверка получения целей поездок")
@Transactional
@MockitoBean(types = JwtDecoder.class)
public class TripPurposeListenerTest extends SharedTest {
    
    @Autowired
    private Consumer<Message<TripPurposeMessage>> input;
    
    @Autowired
    private TripPurposeMapper mapper;
    
    @Autowired
    private TripPurposeService tripPurposeService;
    
    @Test
    @DisplayName("Получение цели поездки")
    void handleTripPurposeTest(){
        TripPurposeMessage tripPurposeMessage = mapper.toMessage(tripPurpose1);
        input.accept(MessageBuilder.withPayload(tripPurposeMessage).build());
        TripPurpose tripPurpose = tripPurposeService.findById(tripPurposeMessage.getId()).get();
        
        assertEquals(tripPurpose.getId(), tripPurposeMessage.getId());
        assertEquals(tripPurpose.getPurpose(), tripPurposeMessage.getLabel());
        assertEquals(tripPurpose.getOrganization(), tripPurposeMessage.getOrganization());
        assertTrue(tripPurpose.isActive());
    }
    
    @Test
    @DisplayName("Деактивация цели поездки")
    void handleTripPurposeDeleteTest(){
        TripPurposeMessage tripPurposeMessage = mapper.toMessage(tripPurpose1);
        input.accept(MessageBuilder.withPayload(tripPurposeMessage).build());
    
        tripPurposeMessage.setDeleted(true);
        input.accept(MessageBuilder.withPayload(tripPurposeMessage).build());
    
        TripPurpose tripPurpose = tripPurposeService.findById(tripPurposeMessage.getId()).get();
        
        assertEquals(tripPurpose.getId(), tripPurposeMessage.getId());
        assertEquals(tripPurpose.getPurpose(), tripPurposeMessage.getLabel());
        assertEquals(tripPurpose.getOrganization(), tripPurposeMessage.getOrganization());
        assertFalse(tripPurpose.isActive());
    }
}
