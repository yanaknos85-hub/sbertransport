package ru.sberbank.ditsib.messaging.listener;

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
import ru.sberbank.ditsib.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.database.model.Organization;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@EmbeddedPostgres
@SpringBootTest
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
        var message = new OrganizationMessage();
        message.setId(id);
        message.setAddress("Address");
        message.setOfficialName("Official name");
        message.setDigitId(idDigit);
        
        organizationsInput.accept(MessageBuilder.withPayload(message).build());
    
        assertThat(repository.count()).isEqualTo(1);
        Organization actual = repository.findAll().get(0);
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(repository.findById(id)).isPresent();
        assertThat(actual.getDigitId()).isEqualTo(idDigit);
    }
    
    @Test
    @DisplayName("Удаление")
    void handleOrganization_delete() {
        var id = UUID.randomUUID();
        repository.save(Organization.builder().id(id).digitId(1L).build());
        var message = new OrganizationMessage();
        message.setId(id);
        message.setAddress("Address");
        message.setOfficialName("Official name");
        message.setDeleted(true);
        
        organizationsInput.accept(MessageBuilder.withPayload(message).build());
        var res = repository.findAll().stream().filter(Organization::isActive).toList();
        assertThat(res).isEmpty();
    }
}