package ru.sberbank.ditsib.transport.request.messaging;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterEach;
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
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.messaging.message.OrganizationMessage;
import ru.sberbank.ditsib.transport.request.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;

import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@DisplayName("Проверка получения организаций")
@MockitoBean(types = JwtDecoder.class)
class OrganizationListenerTest extends KafkaTest {
    
    @Autowired
    private OrganizationRepository repository;
    
    @Autowired
    @Qualifier("organizationsInput")
    private Consumer<Message<OrganizationMessage>> organizationsInput;
    
    @AfterEach
    void dropRepository() {
        repository.deleteAllInBatch();
    }
    
    @Test
    @DisplayName("Проверка получения новой организации")
    void handleOrganization_new() {
        var idDigit = 1L;
        var id = UUID.randomUUID();
        var message = OrganizationMessage.builder().id(id)
                                         .address("Address")
                                         .officialName("Official name")
                                         .digitId(idDigit)
                                         .build();
        
        organizationsInput.accept(MessageBuilder.withPayload(message).build());
    
        assertThat(repository.count()).isEqualTo(1);
        Organization actual = repository.findAll().get(0);
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(repository.findById(id).orElse(null)).isNotNull();
        assertThat(actual.getDigitId()).isEqualTo(idDigit);
    }
    
    @Test
    @DisplayName("Удаление")
    void handleOrganization_delete() {
        var id = UUID.randomUUID();
        repository.save(Organization.builder().id(id).digitId(1L).build());
        var message = OrganizationMessage.builder().id(id)
                                         .address("Address")
                                         .officialName("Official name")
                                         .deleted(true).build();
        
        organizationsInput.accept(MessageBuilder.withPayload(message).build());
    
        assertEquals(0, repository.findAllByActive(true).size());
    }
}