package ru.sber.transport.authentication.messaging.senders.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.support.KafkaHeaders;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.SimpleObjectProvider;
import ru.sber.transport.authentication.business.dto.AccountDto;
import ru.sber.transport.authentication.business.exceptions.AccountNotFoundException;
import ru.sber.transport.authentication.business.providers.AccountProvider;
import ru.sber.transport.authentication.messaging.senders.UserAgentSender;
import ru.sber.transport.user.agent.messages.UserAgentMessage;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_authentication")
@DisplayName("Проверка отправителя user agent'а")
class UserAgentSenderImplTest {

    private final OutputBridge userAgentOutput = mock(OutputBridge.class);

    private final AccountProvider accountProvider = mock(AccountProvider.class);

    private final UserAgentSender userAgentSender = new UserAgentSenderImpl(new SimpleObjectProvider<>(null), new SimpleObjectProvider<>(userAgentOutput), accountProvider);

    @Test
    @DisplayName("Проверка отправки")
    void test_send() throws AccountNotFoundException {
        var login = Instancio.create(String.class);
        var userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36";
        var type = Instancio.create(String.class);
        var account = Instancio.create(AccountDto.class);

        when(accountProvider.get(login)).thenReturn(Optional.of(account));

        userAgentSender.send(login, userAgent, type);

        var dataCaptor = ArgumentCaptor.forClass(UserAgentMessage.class);
        verify(userAgentOutput).send(dataCaptor.capture(), eq(Map.of(KafkaHeaders.KEY, account.getId())));

        var actual = dataCaptor.getValue();
        assertThat(actual)
            .isNotNull()
            .satisfies(message -> {
                assertThat(message.getAuthType()).isEqualTo("BASIC");
                assertThat(message.getId()).isEqualTo(account.getId());
                assertThat(message.getBrowser()).isEqualTo("Chrome");
                assertThat(message.getOperatingSystem()).isEqualTo("OS X");
                assertThat(message.getDevice()).isEqualTo("Personal computer");
                assertThat(message.getClientType()).isEqualTo(type);
                assertThat(message.getUserAgentValue()).isEqualTo(userAgent);
            });
    }

}