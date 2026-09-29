package ru.sberbank.ditsib.transport.vehicle.messaging.sender.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.vehicle.messaging.message.EngineTypeMessage;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EngineTypeSenderImplTest {

    @Mock
    private ObjectProvider<OutputBridge> engineTypeOutputBridge;
    @Mock
    private OutputBridge outputBridge;
    @InjectMocks
    private EngineTypeSenderImpl engineTypeSender;

    @Test
    void testSend() {
        var message = Instancio.create(EngineTypeMessage.class);
        doReturn(outputBridge).when(engineTypeOutputBridge).getIfAvailable();

        engineTypeSender.send(message);
        verify(outputBridge, times(2)).send(message);
    }

    @Test
    void testNotSend() {
        var message = Instancio.create(EngineTypeMessage.class);
        when(engineTypeOutputBridge.getIfAvailable()).thenReturn(null);
        engineTypeSender.send(message);
        verify(outputBridge, never()).send(message);
    }
}