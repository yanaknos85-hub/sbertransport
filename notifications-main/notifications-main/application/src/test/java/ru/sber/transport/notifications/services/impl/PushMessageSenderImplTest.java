package ru.sber.transport.notifications.services.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.test.context.support.WithMockUser;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.messaging.senders.PushSender;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.MessageSender;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@DisplayName("Проверка отправителя PUSH")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
class PushMessageSenderImplTest {
    
    private final PushSender pushSender = mock(PushSender.class);

    private final MessageSender sender = new PushMessageSenderImpl(pushSender);
    
    @Test
    @DisplayName("Проверка отправки")
    @WithMockUser(roles = "GUEST")
    void test_send() {
        var employeeId = UUID.randomUUID();
        
        var receiver = new Employee();
        receiver.setId(employeeId);
        
        var tokens = new ArrayList<UUID>();
        var count = 100;
        for (var i = 0; i < count; i++) {
            tokens.add(employeeId);
        }
    
        var message = "text";
        sender.send(UUID.randomUUID(), receiver, NotificationType.ADDITIONAL_WAYPOINTS_APPROVAL, message,
                    Collections.singletonMap("Key", "value"));
        
        var tokenCaptor = ArgumentCaptor.forClass(List.class);
        var typeCaptor = ArgumentCaptor.forClass(NotificationType.class);
        var messageCaptor = ArgumentCaptor.forClass(String.class);
        var dataCaptor = ArgumentCaptor.forClass(Map.class);
        var additionalDataCaptor = ArgumentCaptor.forClass(Map.class);
        
        verify(pushSender).send(any(UUID.class), tokenCaptor.capture(), typeCaptor.capture(), messageCaptor.capture(),
                                dataCaptor.capture(), additionalDataCaptor.capture());
        
        var actualTokens = tokenCaptor.getValue();
        var actualMessages = messageCaptor.getValue();
        var actualDatas = dataCaptor.getValue();
        var actualType = typeCaptor.getValue();
        var actualAddData = additionalDataCaptor.getValue();
        
        assertThat(actualTokens).hasSize(1);
        assertThat(actualMessages).isEqualTo(message);
        assertThat(actualType).isEqualTo(NotificationType.ADDITIONAL_WAYPOINTS_APPROVAL);
        assertThat(actualAddData.keySet()).hasSize(1);
        assertThat(actualAddData).containsEntry("Key", "value");
        assertThat(actualDatas).isNull();
        
        for (var i = 0; i < actualTokens.size(); i++) {
            var actualToken = (UUID) actualTokens.get(i);
            
            assertThat(actualToken).isEqualTo(tokens.get(i));
        }
    }

    @Test
    @DisplayName("Проверка отправки")
    @WithMockUser(roles = "GUEST")
    void test_send_null_id() {
        var employeeId = UUID.randomUUID();

        var receiver = new Employee();

        var tokens = new ArrayList<UUID>();
        var count = 100;
        for (var i = 0; i < count; i++) {
            tokens.add(employeeId);
        }

        var message = "text";
        assertDoesNotThrow(() -> {
            sender.send(UUID.randomUUID(), receiver, NotificationType.ADDITIONAL_WAYPOINTS_APPROVAL, message,
                    Collections.singletonMap("Key", "value"));
        });
    }
    
}