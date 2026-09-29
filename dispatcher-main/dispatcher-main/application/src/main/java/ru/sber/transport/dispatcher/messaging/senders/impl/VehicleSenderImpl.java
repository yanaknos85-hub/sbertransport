package ru.sber.transport.dispatcher.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.dispatcher.database.model.Vehicle;
import ru.sber.transport.dispatcher.mappers.VehicleMapper;
import ru.sber.transport.dispatcher.messaging.senders.VehicleSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

@RequiredArgsConstructor
@Component
public class VehicleSenderImpl implements VehicleSender {

    @Qualifier("vehicleOutput")
    private final ObjectProvider<OutputBridge> vehicleOutput;

    @Qualifier("vehicleOutputSsl")
    private final ObjectProvider<OutputBridge> vehicleOutputSsl;

    private final VehicleMapper vehicleMapper;

    @Override
    public void send(Vehicle vehicle) {
        var message = vehicleMapper.toVehicleMessage(vehicle);
        vehicleOutput.ifAvailable(ob -> ob.send(message));
        vehicleOutputSsl.ifAvailable(ob -> ob.send(message));
    }
}
