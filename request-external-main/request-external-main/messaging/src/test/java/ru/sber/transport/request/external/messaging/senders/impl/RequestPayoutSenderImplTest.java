package ru.sber.transport.request.external.messaging.senders.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.SimpleObjectProvider;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.payout.messaging.message.RequestPayoutMessage;
import ru.sber.transport.request.external.messaging.senders.RequestPayoutSender;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка отправки данных в кафку")
class RequestPayoutSenderImplTest {

    private final OutputBridge kafkaBridge = mock(OutputBridge.class);
    private final OutputBridge kafkaSslBridge = mock(OutputBridge.class);

    private final RequestPayoutSender requestPayoutSender = new RequestPayoutSenderImpl(
            new SimpleObjectProvider<>(kafkaBridge),
            new SimpleObjectProvider<>(kafkaSslBridge)
    );

    @Test
    void send() {
        var message = Instancio.create(RequestPayoutMessage.class);

        requestPayoutSender.send(message);

        verify(kafkaBridge).send(message);
        verify(kafkaSslBridge).send(message);
    }

}