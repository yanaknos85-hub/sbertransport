package ru.sberbank.ditsib.transport.vehicle.messaging.sender.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.vehicle.database.model.Transport;
import ru.sberbank.ditsib.transport.vehicle.mapper.TransportMapperImpl;
import ru.sberbank.ditsib.transport.vehicle.messaging.message.TransportMessage;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransportSenderImplTest {

    @Mock
    private ObjectProvider<OutputBridge> odometerValueOutputBridge;
    @Mock
    private OutputBridge outputBridge;
    @Mock
    private TransportMapperImpl transportMapper;
    @InjectMocks
    private TransportSenderImpl transportSender;

    @Test
    void testSend() {
        var message = Instancio.create(TransportMessage.class);
        doReturn(outputBridge).when(odometerValueOutputBridge).getIfAvailable();

        transportSender.send(message);
        assertNotNull(message.cityConsumptionRate());
        assertNotNull(message.countryConsumptionRate());
        assertNotNull(message.hybridConsumptionRate());
        verify(outputBridge, times(2)).send(message);
    }

    @Test
    void testNotSend() {
        var message = Instancio.create(TransportMessage.class);
        when(odometerValueOutputBridge.getIfAvailable()).thenReturn(null);

        transportSender.send(message);
        verify(outputBridge, never()).send(message);
    }
    
    @Test
    void createMessageAndSend() {
        var transport = Instancio.create(Transport.class);
        var message = Instancio.create(TransportMessage.class);

        doReturn(outputBridge).when(odometerValueOutputBridge).getIfAvailable();
        when(transportMapper.transportToTransportMessage(eq(transport),anyBoolean())).thenReturn(message);

        transportSender.send(transport, false);

        verify(outputBridge, times(2)).send(eq(message));
    }

}