package ru.sberbank.ditsib.transport.vehicle.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.vehicle.database.model.Transport;
import ru.sberbank.ditsib.transport.vehicle.mapper.TransportMapper;
import ru.sberbank.ditsib.transport.vehicle.messaging.message.TransportMessage;
import ru.sberbank.ditsib.transport.vehicle.messaging.sender.TransportSender;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TransportSenderImpl implements TransportSender {

    @Qualifier("transportOutput")
    private final ObjectProvider<OutputBridge> transportOutput;
    @Qualifier("transportOutputSsl")
    private final ObjectProvider<OutputBridge> transportOutputSsl;

    private final TransportMapper transportMapper;

    @Override
    public void send(TransportMessage message) {
        Optional.ofNullable(transportOutput.getIfAvailable()).ifPresent(ob -> ob.send(message));
        Optional.ofNullable(transportOutputSsl.getIfAvailable()).ifPresent(ob -> ob.send(message));
    }

    @Override
    public void send(Transport transport, boolean isDeleted) {
        send(transportMapper.transportToTransportMessage(transport, isDeleted));
    }
}
