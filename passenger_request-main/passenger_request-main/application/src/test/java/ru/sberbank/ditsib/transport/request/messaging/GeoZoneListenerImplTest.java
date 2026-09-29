package ru.sberbank.ditsib.transport.request.messaging;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.messaging.message.GeoZoneMessage;
import ru.sberbank.ditsib.transport.request.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.request.service.GeoZoneService;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@DisplayName("Проверка получателя геозон")
@MockitoBean(types = JwtDecoder.class)
class GeoZoneListenerImplTest extends KafkaTest {

    @MockitoBean
    private GeoZoneService geoZoneService;
    
    @Autowired
    @Qualifier("geoZoneInput")
    private Consumer<Message<GeoZoneMessage>> geoZoneInput;
    
    @Test
    @DisplayName("Сообщение с новыми данными")
    void test_message_new() {
        var id = UUID.randomUUID();
        
        var message = new GeoZoneMessage();
        message.setCode(100 + "");
        message.setId(id);
        message.setName("Name");
        message.setParentId(UUID.randomUUID());
        
        when(geoZoneService.get(id)).thenReturn(Optional.empty());
        
        geoZoneInput.accept(MessageBuilder.withPayload(message).build());
        
        var geoZoneCaptor = ArgumentCaptor.forClass(GeoZone.class);
        
        verify(geoZoneService).save(geoZoneCaptor.capture());
        
        var actual = geoZoneCaptor.getValue();
        
        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getCode()).isEqualTo(message.getCode());
        assertThat(actual.getName()).isEqualTo(message.getName());
        assertThat(actual.getParentId()).isEqualTo(message.getParentId());
    }
    
    @Test
    @DisplayName("Сообщение с измененными данными")
    void test_message_edited() {
        var id = UUID.randomUUID();
        
        var message = new GeoZoneMessage();
        message.setCode(1000 + "");
        message.setId(id);
        message.setName("Name");
        message.setParentId(UUID.randomUUID());
        
        var geoZone = new GeoZone();
        geoZone.setId(id);
        message.setCode(10000 + "");
        message.setName("Name2");
        geoZone.setParentId(UUID.randomUUID());
        
        when(geoZoneService.get(id)).thenReturn(Optional.of(geoZone));
        
        geoZoneInput.accept(MessageBuilder.withPayload(message).build());
    
        var geoZoneCaptor = ArgumentCaptor.forClass(GeoZone.class);
    
        verify(geoZoneService).save(geoZoneCaptor.capture());
    
        var actual = geoZoneCaptor.getValue();
    
        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getCode()).isEqualTo(message.getCode());
        assertThat(actual.getName()).isEqualTo(message.getName());
        assertThat(actual.getParentId()).isEqualTo(message.getParentId());
    }
}