package ru.sber.transport.notifications.messaging.senders.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.messaging.messages.PushMessage;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.messaging.senders.PushSender;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Transactional
@EmbeddedPostgres
@SpringBootTest
@DisplayName("Проверка отправки PUSH")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
class PushSenderImplTest {
    
    @Autowired
    private PushSender sender;

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
    @DisplayName("Отправка PUSH")
    void test_send() {
        PushSender pushSender = new PushSenderImpl(bridge, bridge);

        var token1 = UUID.randomUUID();
        var token2 = UUID.randomUUID();
        var token3 = UUID.randomUUID();

        pushSender.send(UUID.randomUUID(), List.of(token1, token2, token3), NotificationType.ALLOCATION,"Template",
                Collections.singletonMap("key", "value"), Collections.singletonMap("additionalKey",
                        "additionalValue")
        );

        var actual = ArgumentCaptor.forClass(PushMessage.class);
        verify(bridge.getIfAvailable(), times(2)).send(actual.capture(), anyMap());

        assertThat(actual.getValue().getReceivers()).hasSize(3)
                .hasSameElementsAs(List.of(token1, token2, token3));
        assertThat(actual.getValue().getTemplate()).isEqualTo("Template");
        assertThat(actual.getValue().getData()).hasSize(1);
        assertThat(actual.getValue().getData()).containsEntry("key", "value");
        assertThat(actual.getValue().getType()).isEqualTo(NotificationType.ALLOCATION.name());
        assertThat(actual.getValue().getAdditionalData()).containsEntry("additionalKey", "additionalValue");
    }
    
}