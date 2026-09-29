package ru.sberbank.ditsib.transport.reports.messaging.listeners.impl;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.messaging.messages.trip.CargoRequestMessage;
import ru.sberbank.ditsib.transport.reports.dao.RequestForCargoRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EmbeddedPostgres
@DisplayName("Проверка получения контрактов")
@Transactional
@MockitoBean(types = JwtDecoder.class)
class CargoRequestListenerImplTest extends KafkaTest {
    
    @Autowired
    private CargoRequestListenerImpl cargoRequestListener;
    
    @Autowired
    private RequestForCargoRepository repository;
    
    @Test
    @Disabled("Требуется переработка")
    void handleCargo() {
        var id = UUID.fromString("e002f10b-05fb-485b-9316-1e9d5e9713bd");
        var id1 = UUID.fromString("e002f10b-05fb-485b-9316-1e9d5e9714bd");
        var id2 = UUID.fromString("e002f10b-05fb-485b-9316-1e9d5e9715bd");
        var id3 = UUID.fromString("e002f10b-05fb-485b-9316-1e9d5e9716bd");
        CargoRequestMessage message = CargoRequestMessage.builder()
                .id(id)
                .cargoTypeId(UUID.randomUUID())
                .approvalDate(LocalDateTime.now())
                .approvalId(UUID.randomUUID())
                .transportType("DEDICATED")
                .commentForDriver("commentForDriver")
                .senderId(id1)
                .senderOrganization("орг1")
                .recipientId(id2)
                .recipientOrganization("орг2")
                .length(40.0)
                .volume(28000.0)
                .weight(15.0)
                .height(23.0)
                .width(67.0)
                .occupiedPlacesCount(5)
                .cargoDetails(Collections.singletonList(CargoRequestMessage.CargoDetail.builder()
                           .id(id3)
                           .cargoName("nameCargo")
                           .cargoCategory("CAT")
                           .cargoType("TYPE").build()))
                .build();
        cargoRequestListener.handleCargo(message);
        var result = repository.findAll().get(0);
        var detail = result.getCargoDetails().get(0);
        assertEquals(result.getId(), id);
        assertEquals(result.getSender().getId(), id1);
        assertEquals(result.getRecipient().getId(), id2);
        assertEquals(5, result.getOccupiedPlacesCount());
        assertEquals(23.0, result.getHeight());
        assertEquals("орг2", result.getRecipientOrganization());
        assertEquals("орг1", result.getSenderOrganization());
        assertEquals(15.0, result.getWeight());
        assertEquals(67.0, result.getWidth());
        assertEquals(28000.0, result.getVolume());
        assertEquals("commentForDriver", result.getComment());
        assertEquals("DEDICATED", result.getTransportType());
        assertTrue(result.isActive());
        assertEquals(detail.getId(), id3);
        assertEquals("CAT", detail.getCargoCategory());
        assertEquals("TYPE", detail.getCargoType());
        assertEquals("nameCargo", detail.getCargoName());
        assertEquals(detail.getRequest().getId(), id);
    }
}