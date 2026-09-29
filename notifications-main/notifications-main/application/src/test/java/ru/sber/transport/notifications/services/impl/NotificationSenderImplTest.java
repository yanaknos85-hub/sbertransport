package ru.sber.transport.notifications.services.impl;

import io.qameta.allure.Feature;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.lang.NonNull;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.model.HasContactData;
import ru.sber.transport.notifications.database.model.HasEmail;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelType;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.MessageSender;
import ru.sber.transport.notifications.services.NotificationSender;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Проверка рассылки")
@Slf4j
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
class NotificationSenderImplTest {
    
    private final List<MessageSender> senderList = createSenderList();
    
    private final NotificationSender sender = new NotificationSenderImpl(senderList);

    private final List<String> messages = new ArrayList<>();
    
    private List<MessageSender> createSenderList() {
        return Arrays.stream(ChannelType.values()).map(value -> new MessageSender() {
            
            @Override
            public void send(UUID id, @NonNull HasContactData receiver, NotificationType type, @NonNull String message,
                             Map<String, Object> additionalData) {
                messages.add(String.format( "Message '%s' sent to employee '%s' with '%s'", message,
                        ((HasEmail) receiver).getEmail(),
                         value));
            }
    
            @Override
            public ChannelType channel() {
                return value;
            }
        }).collect(Collectors.toList());
    }
    
    @Test
    @DisplayName("Отправка")
    void test_send() {
        var receiver = new Employee();
        receiver.setEmail("test@email.com");
        
        var messages = new LinkedHashMap<ChannelType, String>();
        messages.put(ChannelType.EMAIL, "Email text");
        messages.put(ChannelType.SMS, "SMS text");

        sender.send(UUID.randomUUID(), receiver, NotificationType.ALLOCATION, messages, new HashMap<>());

        var capturedLogs = this.messages;
        assertThat(capturedLogs).hasSize(2);
        assertThat(capturedLogs.get(1)).isEqualTo("Message 'Email text' sent to employee 'test@email.com' with " +
                                                  "'EMAIL'");
        assertThat(capturedLogs.get(0)).isEqualTo("Message 'SMS text' sent to employee 'test@email.com' with 'SMS'");
    }
}