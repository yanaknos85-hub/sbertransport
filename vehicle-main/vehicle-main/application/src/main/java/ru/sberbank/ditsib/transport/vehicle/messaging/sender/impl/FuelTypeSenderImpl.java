package ru.sberbank.ditsib.transport.vehicle.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.vehicle.messaging.message.FuelTypeMessage;
import ru.sberbank.ditsib.transport.vehicle.messaging.sender.FuelTypeSender;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FuelTypeSenderImpl implements FuelTypeSender {
    @Qualifier("fuelTypeOutput")
    private final ObjectProvider<OutputBridge> fuelTypeOutput;
    @Qualifier("fuelTypeOutputSsl")
    private final ObjectProvider<OutputBridge> fuelTypeOutputSsl;

    @Override
    public void send(FuelTypeMessage message) {
        Optional.ofNullable(fuelTypeOutput.getIfAvailable()).ifPresent(it -> it.send(message));
        Optional.ofNullable(fuelTypeOutputSsl.getIfAvailable()).ifPresent(it -> it.send(message));
    }
}
