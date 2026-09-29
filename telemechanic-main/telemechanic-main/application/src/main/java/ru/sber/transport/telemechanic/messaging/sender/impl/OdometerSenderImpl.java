package ru.sber.transport.telemechanic.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.telemechanic.messaging.sender.OdometerSender;
import ru.sber.transport.telemechanic.messaging.sender.message.OdometerHistoryValueMessage;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OdometerSenderImpl implements OdometerSender {
    
    @Qualifier("odometerValueOutput")
    private final ObjectProvider<OutputBridge> odometerValueOutput;
    @Qualifier("odometerValueOutputSsl")
    private final ObjectProvider<OutputBridge> odometerValueOutputSsl;
    
    @Override
    public void send(OdometerHistoryValueMessage message) {
        Optional.ofNullable(odometerValueOutput.getIfAvailable()).ifPresent(ob -> ob.send(message));
        Optional.ofNullable(odometerValueOutputSsl.getIfAvailable()).ifPresent(ob -> ob.send(message));
    }
}
