package ru.sber.transport.notifications.messaging.listeners.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.dispatcher.messages.DriverMessage;
import ru.sber.transport.notifications.database.model.contractor.Driver;
import ru.sber.transport.notifications.services.DriverService;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@EmbeddedPostgres
@DisplayName("Получатель водителей")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@SpringBootTest
class DriverListenerImplTest {

    @MockitoBean
    private DriverService service;

    @Autowired
    @Qualifier("driverInput")
    private Consumer<Message<DriverMessage>> driverMessageInput;

    @DisplayName("Новый")
    @Test
    void test_new() {
        var message = Instancio.of(DriverMessage.class)
                .set(field(DriverMessage::active), true)
                .create();

        driverMessageInput.accept(MessageBuilder.withPayload(message).build());

        var captor = ArgumentCaptor.forClass(Driver.class);

        verify(service).save(captor.capture());
        verify(service, never()).deleteById(any(UUID.class));

        var actual = captor.getValue();

        assertThat(actual)
                .matches(act -> act.getPhone().equals(message.contactPhone()), "phone")
                .matches(act -> act.getEmail().equals(message.email()), "email")
                .matches(act -> act.getId().equals(message.id()), "ID")
                .matches(act -> act.isPhoneConfirmed() == message.phoneConfirmed(), "phoneConfirmed");
    }

    @DisplayName("Удален")
    @Test
    void test_delete() {
        var message = Instancio.of(DriverMessage.class)
                .set(field(DriverMessage::active), false)
                .create();

        driverMessageInput.accept(MessageBuilder.withPayload(message).build());

        verify(service, never()).save(any(Driver.class));
        verify(service).deleteById(message.id());
    }

}