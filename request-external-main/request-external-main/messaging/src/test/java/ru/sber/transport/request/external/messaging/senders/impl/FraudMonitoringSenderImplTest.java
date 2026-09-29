package ru.sber.transport.request.external.messaging.senders.impl;

import static org.mockito.Mockito.after;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.qameta.allure.Feature;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.SimpleObjectProvider;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.request.external.messaging.message.FraudMonitoringMessage;
import ru.sber.transport.request.external.messaging.senders.FraudMonitoringSender;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка отправки сообщений в фрод-мониторинг")
class FraudMonitoringSenderImplTest {

    private final OutputBridge kafkaBridge = mock(OutputBridge.class);

    private final FraudMonitoringSender sender = new FraudMonitoringSenderImpl(new SimpleObjectProvider<>(kafkaBridge));

    @Test
    @DisplayName("Отправка сообщения")
    void test_send() {
        final var requestId = UUID.randomUUID();
        final var fraudData = List.of(
                new FraudMonitoringMessage.FraudDataItem("RADIUS", "Test comment", null)
        );

        var message = new FraudMonitoringMessage(requestId, "request", fraudData);

        sender.send(message);

        verify(kafkaBridge, after(33)).send(any(FraudMonitoringMessage.class));
    }
}
