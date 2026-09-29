package ru.sberbank.ditsib.transport.vehicle.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.vehicle.messaging.message.EngineTypeMessage;
import ru.sberbank.ditsib.transport.vehicle.messaging.sender.EngineTypeSender;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EngineTypeSenderImpl implements EngineTypeSender {
    @Qualifier("engineTypeOutput")
    private final ObjectProvider<OutputBridge> engineTypeOutput;
    @Qualifier("engineTypeOutputSsl")
    private final ObjectProvider<OutputBridge> engineTypeOutputSsl;
    @Override
    public void send(EngineTypeMessage message) {
        Optional.ofNullable(engineTypeOutput.getIfAvailable()).ifPresent(it -> it.send(message));
        Optional.ofNullable(engineTypeOutputSsl.getIfAvailable()).ifPresent(it -> it.send(message));
    }
}
