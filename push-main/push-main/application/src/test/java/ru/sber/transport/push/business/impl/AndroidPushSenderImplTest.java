package ru.sber.transport.push.business.impl;

import com.google.firebase.messaging.*;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.push.business.PushSender;
import ru.sber.transport.push.business.dto.SendHistoryDto;
import ru.sber.transport.push.business.providers.SendHistoryProvider;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_push")
@DisplayName("Проверка отправки пуша Android")
class AndroidPushSenderImplTest {
    
    private final FirebaseMessaging client = mock(FirebaseMessaging.class);

    private final SendHistoryProvider historyProvider = mock(SendHistoryProvider.class);
    
    private final PushSender sender = new AndroidPushSenderImpl(client, historyProvider);
    
    @Test
    @DisplayName("Отправка")
    void test_send() throws FirebaseMessagingException, NoSuchFieldException, IllegalAccessException {
        when(client.send(any(Message.class))).thenReturn("Message ID");

        var messageId = sender.send(new SendHistoryDto(), "Token-Token", null, "Text", Collections.singletonMap("Key", "Value"));

        assertThat(messageId).isEqualTo("Message ID");
        
        var notificationCaptor = ArgumentCaptor.forClass(Message.class);
        
        verify(client).send(notificationCaptor.capture());

        var value = notificationCaptor.getValue();
        var actual = new MessageTestUtil(value);

        var dataField = Message.class.getDeclaredField("data");
        dataField.trySetAccessible();
        var data = dataField.get(value);

        assertThat(actual.getToken()).isEqualTo("Token-Token");
        assertThat(data)
            .isInstanceOf(Map.class)
            .hasFieldOrPropertyWithValue("Key", "Value")
            .hasFieldOrPropertyWithValue("body", "Text");
    }

    @Test
    @DisplayName("Отправка c типом")
    void test_send_withType() throws FirebaseMessagingException, NoSuchFieldException, IllegalAccessException {
        when(client.send(any(Message.class))).thenReturn("Message ID");

        var messageId = sender.send(new SendHistoryDto(), "Token-Token", "Type", "Text", Collections.singletonMap("Key", "Value"));

        assertThat(messageId).isEqualTo("Message ID");

        var notificationCaptor = ArgumentCaptor.forClass(Message.class);

        verify(client).send(notificationCaptor.capture());

        var value = notificationCaptor.getValue();
        var actual = new MessageTestUtil(value);

        var dataField = Message.class.getDeclaredField("data");
        dataField.trySetAccessible();
        var data = dataField.get(value);

        assertThat(actual.getToken()).isEqualTo("Token-Token");
        assertThat(data)
            .isInstanceOf(Map.class)
            .hasFieldOrPropertyWithValue("Key", "Value")
            .hasFieldOrPropertyWithValue("msgType", "Type")
            .hasFieldOrPropertyWithValue("body", "Text")
        ;
    }

    @Test
    @DisplayName("Отправка. Неверный токен")
    void test_send_wrong_token() throws FirebaseMessagingException {
        when(client.send(any(Message.class))).thenThrow(ExceptionFactory.createFirebaseMessagingException(MessagingErrorCode.INVALID_ARGUMENT, "Message"));

        assertThat(sender.send(new SendHistoryDto(), "Token-Token", "Message", "Text", Collections.singletonMap("Key", "Value")))
            .isNull();
    }

    @Test
    @DisplayName("Отправка. Другая ошибка")
    void test_send_other_error() throws FirebaseMessagingException {
        when(client.send(any(Message.class))).thenThrow(ExceptionFactory.createFirebaseMessagingException(MessagingErrorCode.THIRD_PARTY_AUTH_ERROR, "Message"));

        assertThat(sender.send(new SendHistoryDto(), "Token-Token", "Message", "Text", Collections.singletonMap("Key", "Value")))
            .isNull();
    }

    @Test
    @DisplayName("Отправка. Отсутствующий токен")
    void test_send_unexists_token() throws FirebaseMessagingException {
        when(client.send(any(Message.class))).thenThrow(ExceptionFactory.createFirebaseMessagingException(MessagingErrorCode.UNREGISTERED, "Message"));

        assertThat(sender.send(new SendHistoryDto(), "Token-Token", "Message", "Text", Collections.singletonMap("Key", "Value")))
            .isNull();
    }

    @Test
    @DisplayName("Отправка. Нет токена")
    void test_send_no_token() throws FirebaseMessagingException {
        sender.send(new SendHistoryDto(), null, null, "Text", Collections.singletonMap("Key", "Value"));

        verify(client, never()).send(any());
    }
}