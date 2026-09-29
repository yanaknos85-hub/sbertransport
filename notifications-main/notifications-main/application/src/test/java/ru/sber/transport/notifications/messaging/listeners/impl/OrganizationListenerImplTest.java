package ru.sber.transport.notifications.messaging.listeners.impl;

import io.qameta.allure.Feature;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.dao.messages.corp.OrganizationRepository;
import ru.sber.transport.notifications.database.model.coprorate.Organization;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.messages.corporate.avro.OrganizationMessage;

import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@EmbeddedPostgres
@Transactional
@DisplayName("Проверка слушателя организаций")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@SpringBootTest
class OrganizationListenerImplTest {
    
    @Autowired
    private OrganizationRepository repository;

    @Autowired
    @Qualifier("organizations")
    private Consumer<Message<OrganizationMessage>> organizationMessageInput;

    @Test
    @DisplayName("Получение нового")
    void test_new() {
        var message = new OrganizationMessage();
        message.setId(UUID.randomUUID());
        message.setDeleted(false);
        message.setAddress("Address");
        message.setDigitId(1L);
        message.setName("Official name");
        
        assertThat(repository.count()).isZero();

        organizationMessageInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(repository.count()).isEqualTo(1);
        var actual = repository.findAll().get(0);
        assertThat(actual.getId()).isEqualTo(message.getId());
    }
    
    @Test
    @DisplayName("Удаление")
    void test_delete() {
        var organization = Organization.builder().id(UUID.randomUUID()).build();
        
        repository.save(organization);

        var message = new OrganizationMessage();
        message.setId(organization.getId());
        message.setDeleted(true);
        message.setAddress("Address");
        message.setDigitId(1L);
        message.setName("Official name");
    
        assertThat(repository.count()).isEqualTo(1);
    
        organizationMessageInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(repository.count()).isZero();
    }
    
}