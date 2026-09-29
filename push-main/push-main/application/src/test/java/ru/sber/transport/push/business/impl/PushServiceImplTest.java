package ru.sber.transport.push.business.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.push.business.PushSender;
import ru.sber.transport.push.business.PushService;
import ru.sber.transport.push.business.dto.SendHistoryDto;
import ru.sber.transport.push.business.providers.TokenProvider;
import ru.sber.transport.push.business.dto.PlatformType;
import ru.sber.transport.push.business.dto.TokenData;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_push")
@DisplayName("Проверка отправки на PUSH")
class PushServiceImplTest {
    
    private final PushSender androidSender = mock(PushSender.class);

    private final PushSender iosSender = mock(PushSender.class);

    private final PushSender webSender = mock(PushSender.class);

    private final Map<PlatformType, PushSender> senders = new HashMap<>()
    {{
        put(PlatformType.ANDROID, androidSender);
        put(PlatformType.IOS, iosSender);
        put(PlatformType.WEB, webSender);
    }};

    private final TokenProvider tokenProvider = mock(TokenProvider.class);
    
    private final PushService service = new PushServiceImpl(senders, tokenProvider);
    
    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Отправка")
    void test_send() {
        var recipients = new ArrayList<TokenData>();
        var recipient = List.of(UUID.randomUUID());
        
        for (var i = 0; i < 3; i++) {
            var token = String.format("Email%s@email.com", i);
            
            var tokenData = new TokenData();
            tokenData.setValue(token);
            tokenData.setPlatformType(PlatformType.values()[i % PlatformType.values().length]);
            
            recipients.add(tokenData);
        }

        when(tokenProvider.get(recipient.get(0))).thenReturn(recipients);
        
        var text = "Text";
        
        service.send(UUID.randomUUID(), recipient, "type", text, Collections.emptyMap());
        
        var tokenCaptor = ArgumentCaptor.forClass(String.class);
        var typeCaptor = ArgumentCaptor.forClass(String.class);
        var textCaptor = ArgumentCaptor.forClass(String.class);
        var mapCaptor = ArgumentCaptor.forClass(Map.class);
        
        verify(androidSender, times(1)).send(any(SendHistoryDto.class), tokenCaptor.capture(), typeCaptor.capture(),
                                             textCaptor.capture(), mapCaptor.capture());
        verify(iosSender, times(1)).send(any(SendHistoryDto.class), tokenCaptor.capture(), typeCaptor.capture(),
                                         textCaptor.capture(), mapCaptor.capture());
        verify(webSender, times(1)).send(any(SendHistoryDto.class), tokenCaptor.capture(), typeCaptor.capture(),
                                             textCaptor.capture(), mapCaptor.capture());
        
        var tokens = tokenCaptor.getAllValues();
        var types = typeCaptor.getAllValues();
        var messages = textCaptor.getAllValues();
        var maps = mapCaptor.getAllValues();
        
        assertThat(tokens).hasSize(3);
        assertThat(messages).hasSize(3);
        assertThat(types).hasSize(3);
        assertThat(maps).hasSize(3);
        
        for (var i = 0; i < 3; i++) {
            var token = tokens.get(i);
            var message = messages.get(i);
            
            assertThat(token).isEqualTo(recipients.get(i).getValue());
            assertThat(message).isEqualTo(text);
        }
    }
    
}