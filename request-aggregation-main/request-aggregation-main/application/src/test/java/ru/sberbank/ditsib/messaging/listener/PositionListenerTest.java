package ru.sberbank.ditsib.messaging.listener;

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
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.database.dao.PositionRepository;
import ru.sberbank.ditsib.database.model.Organization;
import ru.sberbank.ditsib.database.model.Position;
import ru.sberbank.ditsib.enumerate.TransportClass;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@EmbeddedPostgres
@SpringBootTest
@DisplayName("Проверка получения должностей")
@MockitoBean(types = JwtDecoder.class)
class PositionListenerTest extends KafkaTest {
    
    @Autowired
    private PositionRepository repository;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
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
    void handlePosition_new() {
        var id = UUID.randomUUID();
        var message = PositionMessage.builder()
                                     .id(id)
                                     .organizationId(organization.getId())
                                     .availableClasses(Set.of(TransportClass.COMFORT.toString()))
                                     .positionName("Boss")
                                     .selfApproved(true)
                                     .deleted(false)
                                     .build();

        positionsInput.accept(MessageBuilder.withPayload(message).build());

        var res = repository.findAll();
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getId()).isEqualTo(id);
    }
    
    @Test
    @DisplayName("Удаление")
    void handlePosition_delete() {
        var id = UUID.randomUUID();
        repository.save(Position.builder().id(id).organization(organization).positionName("Pawn").build());
        var message = PositionMessage.builder()
                                     .id(id)
                                     .organizationId(organization.getId())
                                     .availableClasses(Set.of(TransportClass.COMFORT.toString()))
                                     .positionName("Boss")
                                     .selfApproved(true)
                                     .deleted(true)
                                     .build();
        
        positionsInput.accept(MessageBuilder.withPayload(message).build());

        var res = repository.findAll().stream().filter(Position::isActive).toList();
        assertThat(res).isEmpty();
    }
}