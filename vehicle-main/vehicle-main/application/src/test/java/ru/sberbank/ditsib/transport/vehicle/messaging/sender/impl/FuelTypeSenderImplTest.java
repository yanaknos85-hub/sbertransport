package ru.sberbank.ditsib.transport.vehicle.messaging.sender.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.vehicle.messaging.message.FuelTypeMessage;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FuelTypeSenderImplTest {

    @Mock
    private ObjectProvider<OutputBridge> fuelTypeOutputBridge;
    @Mock
    private OutputBridge outputBridge;
    @InjectMocks
    private FuelTypeSenderImpl fuelTypeSender;

    @Test
    void testSend() {
        var message = Instancio.create(FuelTypeMessage.class);
        doReturn(outputBridge).when(fuelTypeOutputBridge).getIfAvailable();

        fuelTypeSender.send(message);
        verify(outputBridge, times(2)).send(message);
    }

    @Test
    void testNotSend() {
        var message = Instancio.create(FuelTypeMessage.class);
        when(fuelTypeOutputBridge.getIfAvailable()).thenReturn(null);

        fuelTypeSender.send(message);
        verify(outputBridge, never()).send(message);
    }
}