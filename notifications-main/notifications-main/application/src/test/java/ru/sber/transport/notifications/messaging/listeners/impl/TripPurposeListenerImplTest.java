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
import ru.sber.transport.notifications.database.dao.messages.trip_request.TripPurposeRepository;
import ru.sber.transport.notifications.database.model.TripPurpose;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.messaging.messages.TripPurposeMessage;

import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@EmbeddedPostgres
@Transactional
@DisplayName("Проверка слушателя целей поездки")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@SpringBootTest
class TripPurposeListenerImplTest {
    
    @Autowired
    private TripPurposeRepository repository;

    @Autowired
    @Qualifier("tripPurposeInput")
    private Consumer<Message<TripPurposeMessage>> tripPurposeMessageInput;

    @Test
    @DisplayName("Получение нового")
    void test_new() {
        var message = TripPurposeMessage.builder()
                                        .id(UUID.randomUUID())
                                        .organization(UUID.randomUUID())
                                        .label("Label")
                                        .deleted(false)
                                        .build();
        
        assertThat(repository.count()).isZero();

        tripPurposeMessageInput.accept(MessageBuilder.withPayload(message).build());
        
        assertThat(repository.count()).isEqualTo(1);
        var actual = repository.findAll().get(0);
        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getOrganizationId()).isEqualTo(message.getOrganization());
        assertThat(actual.getPurpose()).isEqualTo(message.getLabel());
    }
    
    @Test
    @DisplayName("Удаление")
    void test_delete() {
        var tripPurpose = new TripPurpose();
        tripPurpose.setPurpose("Purpose");
        tripPurpose.setOrganizationId(UUID.randomUUID());
        tripPurpose.setId(UUID.randomUUID());
        
        repository.save(tripPurpose);
        
        var message = TripPurposeMessage.builder()
                                        .id(tripPurpose.getId())
                                        .organization(UUID.randomUUID())
                                        .label("Label")
                                        .deleted(true)
                                        .build();
    
        assertThat(repository.count()).isEqualTo(1);

        tripPurposeMessageInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(repository.count()).isZero();
    }
    
}