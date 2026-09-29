package ru.sber.transport.authentication.messaging.senders.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.SimpleObjectProvider;
import ru.sber.transport.authentication.messaging.senders.EmailSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.messaging.messages.EmailMessage;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@UnitTest
@IsolatedTest
@Feature("app_platform_authentication")
@DisplayName("Проверка отправки почты")
class EmailSenderImplTest {

    private final OutputBridge outputBridge = mock(OutputBridge.class);

    private final EmailSender sender = new EmailSenderImpl(new SimpleObjectProvider<>(outputBridge), new SimpleObjectProvider<>(null));
    
    @Test
    @DisplayName("Проверка")
    void test_send() {
        var emails = List.of("Email0", "Email1", "Email2");
        var subject = "Subject";
        var template = "Template";
        Map<String, Object> data = Map.of("Key1", "Value1", "Key2", "Value2");
        
        sender.send(emails, subject, template, data);


        var messageCaptor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(outputBridge).send(messageCaptor.capture());

        var actual = messageCaptor.getValue();
        assertThat(actual.getEmails()).hasSize(emails.size());
        assertThat(actual.getSubject()).isEqualTo(subject);
        assertThat(actual.getTemplate()).isEqualTo(template);
    }
    
}