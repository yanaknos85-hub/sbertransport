package ru.sber.transport.push.messaging.listeners;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.push.business.PushService;
import ru.sber.transport.push.business.dto.SendHistoryDto;
import ru.sber.transport.text.TextService;
import ru.sber.transport.messaging.messages.PushMessage;

import java.util.*;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@UnitTest
@Transactional
@IsolatedTest
@Isolated
@Feature("app_platform_push")
@DisplayName("Проверка слушателя сообщений PUSH")
@ActiveProfiles("test")
class PushListenerTest {


    private final ListenerConfig config = new ListenerConfig();

    @Test
    @DisplayName("Проверка отправки")
    void test_send() {
        var textService = mock(TextService.class);
        var pushService = mock(PushService.class);

        var input = config.pushInput(textService, pushService);
        var receivers = new ArrayList<UUID>();

        for (var i = 0; i < 3; i++) {
            receivers.add(UUID.randomUUID());
        }

        var message = PushMessage.builder()
                                .id(UUID.randomUUID())
                                 .receivers(receivers)
                                 .type("Type")
                                 .additionalData(Collections.singletonMap("addKey", "addValue"))
                                 .template("Template")
                                 .data(new HashMap<>())
                                 .build();

        when(textService.createFromTemplate(eq("Template"), anyMap())).thenReturn("Text of the email");
        var raw = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId())));

        input.accept(raw);

        var recipientsCaptor = ArgumentCaptor.forClass(List.class);
        var typeCaptor = ArgumentCaptor.forClass(String.class);
        var textCaptor = ArgumentCaptor.forClass(String.class);
        var dataCaptor = ArgumentCaptor.forClass(Map.class);

        verify(pushService).send(any(UUID.class), recipientsCaptor.capture(), typeCaptor.capture(), textCaptor.capture(),
                                 dataCaptor.capture());

        var recipients = recipientsCaptor.getValue();
        var text = textCaptor.getValue();
        var type = typeCaptor.getValue();
        var data = dataCaptor.getValue();

        assertThat(recipients).hasSize(3);
        assertThat(recipients.get(0)).isEqualTo(receivers.get(0));
        assertThat(recipients.get(1)).isEqualTo(receivers.get(1));
        assertThat(recipients.get(2)).isEqualTo(receivers.get(2));
        assertThat(text).isEqualTo("Text of the email");
        assertThat(type).isEqualTo("Type");
        assertThat(data).containsEntry("addKey", "addValue");
    }

    @Test
    @DisplayName("Проверка отправки SSL")
    void test_send_ssl() {
        var textService = mock(TextService.class);
        var pushService = mock(PushService.class);

        var input = config.pushInputSsl(textService, pushService);
        var receivers = new ArrayList<UUID>();

        for (var i = 0; i < 3; i++) {
            receivers.add(UUID.randomUUID());
        }

        var message = PushMessage.builder().id(UUID.randomUUID()).id(UUID.randomUUID())
                                 .receivers(receivers).type("Type").additionalData(Collections.singletonMap("addKey", "addValue")).template("Template").data(new HashMap<>()).build();
        when(textService.createFromTemplate(eq("Template"), anyMap())).thenReturn("Text of the email");

        input.accept(MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId()))));

        var recipientsCaptor = ArgumentCaptor.forClass(List.class);
        var typeCaptor = ArgumentCaptor.forClass(String.class);
        var textCaptor = ArgumentCaptor.forClass(String.class);
        var dataCaptor = ArgumentCaptor.forClass(Map.class);

        verify(pushService).send(any(UUID.class), recipientsCaptor.capture(), typeCaptor.capture(), textCaptor.capture(), dataCaptor.capture());

        var recipients = recipientsCaptor.getValue();
        var text = textCaptor.getValue();
        var type = typeCaptor.getValue();
        var data = dataCaptor.getValue();

        assertThat(recipients).hasSize(3);
        assertThat(recipients.get(0)).isEqualTo(receivers.get(0));
        assertThat(recipients.get(1)).isEqualTo(receivers.get(1));
        assertThat(recipients.get(2)).isEqualTo(receivers.get(2));
        assertThat(text).isEqualTo("Text of the email");
        assertThat(type).isEqualTo("Type");
        assertThat(data).containsEntry("addKey", "addValue");
    }

}