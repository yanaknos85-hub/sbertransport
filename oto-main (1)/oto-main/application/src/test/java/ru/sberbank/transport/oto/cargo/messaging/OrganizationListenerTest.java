package ru.sberbank.transport.oto.cargo.messaging;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;
import ru.sberbank.transport.oto.cargo.database.dao.OrganizationGroupRepository;
import ru.sberbank.transport.oto.cargo.database.dao.OrganizationRepository;
import ru.sberbank.transport.oto.cargo.database.model.Organization;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

@UnitTest
@Isolated
@Feature("app_passenger_oto")
@EmbeddedPostgres
@SpringBootTest
@DisplayName("Проверка получения организаций")
@MockitoBean(types = JwtDecoder.class)
@ActiveProfiles({"test", "kafka"})
class OrganizationListenerTest extends KafkaTest {
    
    @Autowired
    private OrganizationRepository repository;
    
    @Autowired
    private OrganizationGroupRepository groupRepository;
    
    @AfterEach
    void dropRepository() {
        repository.deleteAllInBatch();
        groupRepository.deleteAllInBatch();
    }
    
    @Test
    @DisplayName("Проверка получения новой организации с группой")
    void handleOrganization_new() {
        var idDigit = 1L;
        var id = UUID.randomUUID();
        var message = OrganizationMessage.builder()
                                         .id(id)
                                         .digitId(idDigit)
                                         .officialName("Official name")
                                         .address("Address")
                                         .organizationGroup(new OrganizationMessage.OrganizationGroup(id, "organizationGroupName", false))
                                         .build();
        
        
        produceMessage("service.organization", MessageBuilder.withPayload(message).build());
        
        assertThat(repository.count()).isEqualTo(1);
        Organization actual = repository.findAll().getFirst();
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(repository.findById(id)).isPresent();
        assertThat(actual.getOrganizationGroup().getId()).isEqualTo(id);
    }
    
    @Test
    @DisplayName("Проверка получения новой организации без группы")
    void handleOrganization_new2() {
        var idDigit = 1L;
        var id = UUID.randomUUID();
        var message = OrganizationMessage.builder()
                                         .id(id)
                                         .digitId(idDigit)
                                         .officialName("Official name")
                                         .address("Address")
                                         .build();
        
        
        produceMessage("service.organization", MessageBuilder.withPayload(message).build());
        
        assertThat(repository.count()).isEqualTo(1);
        Organization actual = repository.findAll().getFirst();
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(repository.findById(id)).isPresent();
        assertNull(actual.getOrganizationGroup());
    }
}