package ru.sberbank.ditsib.transport.request.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.request.messaging.message.CarLocationMessage;
import ru.sberbank.ditsib.transport.request.messaging.senders.CarLocationSender;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CarLocationSenderImpl implements CarLocationSender {

    @Qualifier("carLocationRequestOutput")
    private final ObjectProvider<OutputBridge> carLocationRequestOutput;

    @Override
    public void send(CarLocationMessage message) {
        Optional.ofNullable(carLocationRequestOutput.getIfAvailable())
                .ifPresent(outputBridge -> outputBridge.send(message));
    }
}
