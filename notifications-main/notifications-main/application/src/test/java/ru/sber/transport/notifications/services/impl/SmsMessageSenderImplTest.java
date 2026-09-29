package ru.sber.transport.notifications.services.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.messaging.senders.SmsSender;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelType;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.MessageSender;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@DisplayName("Проверка отправителя СМС")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
class SmsMessageSenderImplTest {
    
    private final SmsSender smsSender = mock(SmsSender.class);

    private final MessageSender sender = new SmsMessageSenderImpl(smsSender);
    
    @Test
    @DisplayName("Проверка отправки")
    void test_send() {
        var receiver = new Employee();
        
        receiver.setPhone("Phone");
        receiver.setPhoneConfirmed(true);

        var message = "Message";
        
        sender.send(UUID.randomUUID(), receiver, NotificationType.RESTRICTIONS_EDITED_TYPE, message,
                    Collections.singletonMap("key", "value"));
        
        var phonesCaptor = ArgumentCaptor.forClass(List.class);
        var messageCaptor = ArgumentCaptor.forClass(String.class);
        var objectCaptor = ArgumentCaptor.forClass(Map.class);
        
        verify(smsSender).send(phonesCaptor.capture(), messageCaptor.capture(), objectCaptor.capture());
        
        assertThat(phonesCaptor.getValue()).hasSize(1);
        assertThat(phonesCaptor.getValue().get(0)).isEqualTo(receiver.getPhone());
        assertThat(sender.channel()).isEqualTo(ChannelType.SMS);
    }

    @Test
    @DisplayName("Проверка отправки")
    void test_send_not_sende_phone_not_confirmed() {
        var receiver = new Employee();

        receiver.setPhone("Phone");
        receiver.setPhoneConfirmed(false);

        var message = "Message";

        sender.send(UUID.randomUUID(), receiver, NotificationType.RESTRICTIONS_EDITED_TYPE, message,
                Collections.singletonMap("key", "value"));

        verify(smsSender, never())
                .send(any(), any(), any());
    }
    
}