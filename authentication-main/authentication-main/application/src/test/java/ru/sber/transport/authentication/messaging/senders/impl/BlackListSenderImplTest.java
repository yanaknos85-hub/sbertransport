package ru.sber.transport.authentication.messaging.senders.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.AbstractContextedTest;
import ru.sber.transport.SimpleObjectProvider;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.messaging.messages.BlackListMessage;
import ru.sber.transport.authentication.messaging.senders.BlackListSender;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_authentication")
@DisplayName("Проверка отправки токена в ЧС")
class BlackListSenderImplTest {

    private final OutputBridge outputBridge = mock(OutputBridge.class);

    private final BlackListSender sender = new BlackListSenderImpl(new SimpleObjectProvider<>(outputBridge), new SimpleObjectProvider<>(null));
    
    @DisplayName("Отправка")
    @Test
    void test_send() {
        sender.send("Token");

        var messageCaptor = ArgumentCaptor.forClass(BlackListMessage.class);
        verify(outputBridge).send(messageCaptor.capture());
        
        var message = messageCaptor.getValue();
        
        assertThat(message.getToken()).isEqualTo("Token");
    }
    
}