package ru.sber.transport.request.external.messaging.senders.impl;

import static org.mockito.Mockito.after;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import io.qameta.allure.Feature;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.SimpleObjectProvider;
import ru.sber.transport.messages.notification_900.avro.NotificationMessage;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.request.external.messaging.TestEmployee;
import ru.sber.transport.request.external.messaging.TestOrder;
import ru.sber.transport.request.external.messaging.mapper.NotificationMapper;
import ru.sber.transport.request.external.messaging.senders.NotificationSender;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка отправки сообщений нотификации")
class NotificationSenderImplTest {

    private final OutputBridge kafkaBridge = mock(OutputBridge.class);
    private final OutputBridge kafkaSslBridge = mock(OutputBridge.class);
    private final OutputBridge avroBridge = mock(OutputBridge.class);
    private final NotificationMapper mapper = mock(NotificationMapper.class);


    private final NotificationSender sender = new NotificationSenderImpl(new SimpleObjectProvider<>(kafkaBridge),
            new SimpleObjectProvider<>(kafkaSslBridge),
            new SimpleObjectProvider<>(avroBridge), mapper);

    @Test
    @DisplayName("Отправка сообщения")
    void test_send() {
        final var order = Instancio.of(TestOrder.class)
                .set(Select.field(TestOrder::getApprover), new TestEmployee(UUID.randomUUID(), null, null, null, null, null, null, null, null))
                .create();
        final var messageType = Instancio.of(String.class).create();
        final var receiverId = UUID.randomUUID();
        var kafkaMessage = Instancio.create(ru.sber.transport.request.external.messaging.message.NotificationMessage.class);
        var avroMessage = Instancio.create(ru.sber.transport.messages.notification_900.avro.NotificationMessage.class);
        doReturn(kafkaMessage).when(mapper).toMessage(order, messageType, receiverId);
        doReturn(avroMessage).when(mapper).toAvroMessage(order, messageType, receiverId);

        sender.send(order, messageType, receiverId);

        verify(kafkaBridge, org.mockito.Mockito.timeout(1000).times(1))
                .send(org.mockito.ArgumentMatchers.any(ru.sber.transport.request.external.messaging.message.NotificationMessage.class));
        verify(kafkaSslBridge, org.mockito.Mockito.timeout(1000).times(1))
                .send(org.mockito.ArgumentMatchers.any(ru.sber.transport.request.external.messaging.message.NotificationMessage.class));
        verify(avroBridge, org.mockito.Mockito.timeout(1000).times(1))
                .send(org.mockito.ArgumentMatchers.any(ru.sber.transport.messages.notification_900.avro.NotificationMessage.class));
    }

}