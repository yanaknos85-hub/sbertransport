package ru.sber.transport.request.external.messaging.senders.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.SimpleObjectProvider;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.request.external.messaging.exceptions.BrokerException;
import ru.sber.transport.request.external.messaging.message.ReceiptMessage;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@UnitTest
@IsolatedTest
@Feature("app_platform_request_external")
class ReceiptScannerSenderImplTest {

    private final OutputBridge kafkaBridge = mock(OutputBridge.class);
    private final OutputBridge kafkaSslBridge = mock(OutputBridge.class);

    private final ReceiptScannerSenderImpl sender = new ReceiptScannerSenderImpl(
            new SimpleObjectProvider<>(kafkaBridge), new SimpleObjectProvider<>(kafkaSslBridge)
    );

    @Test
    @DisplayName("Отправка сообщения")
    void send_success() {
        var message = Instancio.create(ReceiptMessage.class);

        sender.send(message);

        verify(kafkaBridge, timeout(1000).times(1)).send(message);
        verify(kafkaSslBridge, timeout(1000).times(1)).send(message);
    }

    @Test
    @DisplayName("Отправка сообщения при ошибке в первом мосте")
    void send_bridgeThrows() {
        final var message = Instancio.create(ReceiptMessage.class);
        var errorMsg = UUID.randomUUID().toString();
        doThrow(new RuntimeException(errorMsg)).when(kafkaBridge).send(message);

        assertThatThrownBy(() -> sender.send(message))
                .isInstanceOf(BrokerException.class)
                .hasMessageContaining(errorMsg);

        verify(kafkaBridge, timeout(1000).times(1)).send(message);
        verify(kafkaSslBridge, never()).send(any());
    }

    @Test
    @DisplayName("Отправка сообщения при ошибке во втором мосте")
    void send_sslBridgeThrows() {
        final var message = Instancio.create(ReceiptMessage.class);
        var errorMsg = UUID.randomUUID().toString();
        doThrow(new RuntimeException(errorMsg)).when(kafkaSslBridge).send(message);

        assertThatThrownBy(() -> sender.send(message))
                .isInstanceOf(BrokerException.class)
                .hasMessageContaining(errorMsg);

        verify(kafkaBridge, timeout(1000).times(1)).send(message);
        verify(kafkaSslBridge, timeout(1000).times(1)).send(message);
    }
}