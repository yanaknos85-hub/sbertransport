package ru.sber.transport.notifications.services.impl;

import io.qameta.allure.Feature;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.messaging.senders.EmailSender;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelType;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.MessageSender;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SuppressWarnings("unchecked")
@DisplayName("Отправка E-Mail")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
class EmailMessageSenderImplTest {
    
    private final EmailSender mailSender = mock(EmailSender.class);

    private final MessageSender sender = new EmailMessageSenderImpl(mailSender);
    
    @SneakyThrows
    @Test
    @DisplayName("Отправка")
    void test_send() {
        var employee = new Employee();
        employee.setEmail("Email@server.com");
        var map = Collections.singletonMap("key", "value");

        sender.send(UUID.randomUUID(), employee, NotificationType.RESTRICTIONS_EDITED, "Text", Collections.singletonMap("key", "value"));
        
        var recipients = ArgumentCaptor.forClass(List.class);
        var subject = ArgumentCaptor.forClass(String.class);
        var template = ArgumentCaptor.forClass(String.class);
        var data = ArgumentCaptor.forClass(Map.class);
        
        verify(mailSender).send(recipients.capture(), subject.capture(), template.capture(), data.capture());
        
        var actualRecipients = recipients.getValue();
        var actualSubject = subject.getValue();
        var actualTemplate = template.getValue();
        var actualData = data.getValue();
        
        assertThat(actualRecipients).hasSize(1);
        assertThat(actualRecipients.get(0)).isEqualTo(employee.getEmail());
        assertThat(actualSubject).isNull();
        assertThat(actualTemplate).isEqualTo("Text");
        assertThat(actualData).containsEntry("key", "value");
        assertThat(actualData).containsAllEntriesOf(map);
        assertThat(sender.channel()).isEqualTo(ChannelType.EMAIL);
    }
    
}