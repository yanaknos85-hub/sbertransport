package ru.sber.transport.telemechanic.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.telemechanic.messaging.sender.EwbClosedSender;
import ru.sber.transport.telemechanic.messaging.sender.message.EwbClosedMessage;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EwbClosedSenderImpl implements EwbClosedSender {
    
    @Qualifier("ewbClosedOutput")
    private final ObjectProvider<OutputBridge> ewbClosedOutput;
    @Qualifier("ewbClosedOutputSsl")
    private final ObjectProvider<OutputBridge> ewbClosedOutputSsl;
    
    @Override
    public void send(EwbClosedMessage message) {
        Optional.ofNullable(ewbClosedOutput.getIfAvailable()).ifPresent(ob -> ob.send(message));
        Optional.ofNullable(ewbClosedOutputSsl.getIfAvailable()).ifPresent(ob -> ob.send(message));
    }
}
