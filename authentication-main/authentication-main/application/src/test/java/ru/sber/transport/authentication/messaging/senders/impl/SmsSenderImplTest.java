package ru.sber.transport.authentication.messaging.senders.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.SimpleObjectProvider;
import ru.sber.transport.authentication.messaging.senders.SmsSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.messaging.messages.SmsMessage;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@UnitTest
@IsolatedTest
@Feature("app_platform_authentication")
@DisplayName("Проверка отправки СМС")
class SmsSenderImplTest {

    private final OutputBridge outputBridge = mock(OutputBridge.class);

    private final SmsSender smsSender = new SmsSenderImpl(new SimpleObjectProvider<>(null), new SimpleObjectProvider<>(outputBridge));

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("Отправка")
    void test_send() {
        var phone = Instancio.create(String.class);
        var template = Instancio.create(String.class);
        var data = Instancio.ofMap(String.class, Object.class).create();

        smsSender.send(phone, template, data);

        var dataCaptor = ArgumentCaptor.forClass(SmsMessage.class);
        var mapCaptor = ArgumentCaptor.forClass(Map.class);

        verify(outputBridge).send(dataCaptor.capture(), mapCaptor.capture());

        assertThat(dataCaptor.getValue()).isNotNull()
            .satisfies(message -> {
                assertThat(message.getData()).isEqualTo(data);
                assertThat(message.getTemplate()).isEqualTo(template);
                assertThat(message.getPhones()).contains(phone);
            });
    }

}