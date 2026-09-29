package ru.sberbank.ditsib.transport.request.messaging;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.request.messaging.message.PositionMessage;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.request.database.dao.PositionRepository;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.database.model.corp.Position;
import ru.sberbank.ditsib.transport.request.service.corp.OrganizationService;
import ru.sberbank.ditsib.transport.request.service.corp.PositionService;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@DisplayName("Проверка получения должностей")
@MockitoBean(types = JwtDecoder.class)
class PositionListenerTest extends KafkaTest {
    
    @Autowired
    private PositionRepository repository;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @MockitoSpyBean
    private PositionService positionService;
    
    @Autowired
    @Qualifier("positionsInput")
    private Consumer<Message<PositionMessage>> positionsInput;
    
    private Organization organization;
    
    
    @AfterEach
    void dropRepository() {
        repository.deleteAll();
        organizationRepository.deleteAllInBatch();
    }
    
    @BeforeEach
    void fillRepository() {
        var organizationId = UUID.randomUUID();
        organization = organizationRepository.save(Organization.builder().id(organizationId).digitId(1L).build());
    }
    
    @Test
    @DisplayName("Новая")
    void handleOrganization_new() {
        
        var id = UUID.randomUUID();
        var message = PositionMessage.builder()
                                     .id(id)
                                     .organizationId(organization.getId())
                                     .availableClasses(Set.of(TaxiClass.COMFORT.toString()))
                                     .positionName("Boss")
                                     .selfApproved(true)
                                     .deleted(false)
                                     .build();
        
        assertEquals(0, repository.count());
        positionsInput.accept(MessageBuilder.withPayload(message).build());
        
        verify(positionService, atLeastOnce()).save(any(Position.class));
        assertEquals(1, repository.count());
        assertEquals(id, repository.findAll().get(0).getId());
    }
    
    @Test
    @DisplayName("Удаление")
    void handleOrganization_delete() {
        var id = UUID.randomUUID();
        repository.save(Position.builder().id(id).organizationId(organization.getId()).positionName("Pawn").build());
        var message = PositionMessage.builder()
                                     .id(id)
                                     .organizationId(organization.getId())
                                     .availableClasses(Set.of(TaxiClass.COMFORT.toString()))
                                     .positionName("Boss")
                                     .selfApproved(true)
                                     .deleted(true)
                                     .build();
        assertEquals(1, repository.count());
        
        positionsInput.accept(MessageBuilder.withPayload(message).build());
        
        verify(positionService, atLeastOnce()).get(any());
        verify(positionService, atLeastOnce()).delete(any(Position.class));
        assertEquals(0, repository.findAllByActive(true).size());
    }
}