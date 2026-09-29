package ru.sber.transport.telemechanic.messaging.sender.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.telemechanic.messaging.sender.message.OdometerHistoryValueMessage;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OdometerSenderImplTest {
    
    @Mock
    private ObjectProvider<OutputBridge> odometerValueOutputBridge;
    @Mock
    private OutputBridge outputBridge;
    @InjectMocks
    private OdometerSenderImpl odometerSender;
    
    @Test
    void testSend() {
        var message = Instancio.create(OdometerHistoryValueMessage.class);
        doReturn(outputBridge).when(odometerValueOutputBridge).getIfAvailable();
        
        odometerSender.send(message);
        verify(outputBridge, times(2)).send(message);
    }
    
    @Test
    void testNotSend() {
        var message = Instancio.create(OdometerHistoryValueMessage.class);
        when(odometerValueOutputBridge.getIfAvailable()).thenReturn(null);
        
        odometerSender.send(message);
        verify(outputBridge, never()).send(message);
    }
}