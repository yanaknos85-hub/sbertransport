package ru.sberbank.ditsib.transport.request.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sberbank.ditsib.transport.request.database.model.FraudType;
import ru.sberbank.ditsib.transport.request.messaging.message.FraudMonitoringMessage;
import ru.sberbank.ditsib.transport.request.messaging.senders.FraudMonitoringSender;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudMonitoringServiceImplTest {

    @InjectMocks
    private FraudMonitoringServiceImpl fraudMonitoringService;
    @Mock
    private FraudMonitoringSender fraudMonitoringSender;
    @Captor
    private ArgumentCaptor<FraudMonitoringMessage> messageCaptor;

    @Test
    void send_shouldSendMessageToSender() {
        var requestId = UUID.randomUUID();

        fraudMonitoringService.send(requestId, FraudType.ABSENCE, "test comment");

        verify(fraudMonitoringSender, times(1)).send(messageCaptor.capture());
        FraudMonitoringMessage capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.id()).isEqualTo(requestId);
        assertThat(capturedMessage.source()).isEqualTo("request");
        assertThat(capturedMessage.fraudData()).hasSize(1);
        assertThat(capturedMessage.fraudData().getFirst().type()).isEqualTo("ABSENCE");
        assertThat(capturedMessage.fraudData().getFirst().comment()).isEqualTo("test comment");
    }

    @Test
    void send_withNullFraudData_shouldSendMessageWithEmptyList() {
        var requestId = UUID.randomUUID();

        fraudMonitoringService.send(requestId, null, null);

        verify(fraudMonitoringSender, times(1)).send(messageCaptor.capture());
        FraudMonitoringMessage capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.getId()).isEqualTo(requestId);
        assertThat(capturedMessage.source()).isEqualTo("request");
        assertThat(capturedMessage.fraudData()).hasSize(1);
        assertThat(capturedMessage.fraudData().getFirst().type()).isNull();
        assertThat(capturedMessage.fraudData().getFirst().comment()).isNull();
    }
}
