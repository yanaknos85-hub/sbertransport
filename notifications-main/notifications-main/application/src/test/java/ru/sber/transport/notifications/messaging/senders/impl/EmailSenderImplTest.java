package ru.sber.transport.notifications.messaging.senders.impl;

import io.qameta.allure.Feature;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.messaging.senders.EmailSender;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.messaging.messages.EmailMessage;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@EmbeddedPostgres
@SpringBootTest
@DisplayName("Проверка отправки почты")
@RequiredArgsConstructor
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
class EmailSenderImplTest {
    
    @Autowired
    private EmailSender sender;

    private final ObjectProvider<OutputBridge> bridge = new ObjectProvider<OutputBridge>() {
        @Override
        public OutputBridge getObject(Object... args) throws BeansException {
            return ob;
        }

        @Override
        public OutputBridge getIfAvailable() throws BeansException {
            return ob;
        }

        @Override
        public OutputBridge getIfUnique() throws BeansException {
            return ob;
        }

        @Override
        public OutputBridge getObject() throws BeansException {
            return ob;
        }

        private final OutputBridge ob = mock(OutputBridge.class);
    };

    @Test
    @DisplayName("Отправка почты")
    void test_send() {
        EmailSender emailSender = new EmailSenderImpl(bridge, bridge);

        emailSender.send(List.of("address1", "address2", "address3"), "Subject", "Template",
                Collections.singletonMap("key", "value")
        );

        var actual = ArgumentCaptor.forClass(EmailMessage.class);
        verify(bridge.getIfAvailable(), times(2)).send(actual.capture(), anyMap());

        assertThat(actual.getValue().getEmails()).hasSize(3)
                .contains("address1")
                .contains("address2")
                .contains("address3");
        assertThat(actual.getValue().getSubject()).isEqualTo("Subject");
        assertThat(actual.getValue().getTemplate()).isEqualTo("Template");
        assertThat(actual.getValue().getData().values()).hasSize(4);
        assertThat(actual.getValue().getData()).containsEntry("key", "value");
        assertThat(actual.getValue().getData()).containsEntry("alphaLink", null);
        assertThat(actual.getValue().getData()).containsEntry("sigmaLink", null);
        assertThat(actual.getValue().getData()).containsEntry("dzoLink", null);
    }
    
}