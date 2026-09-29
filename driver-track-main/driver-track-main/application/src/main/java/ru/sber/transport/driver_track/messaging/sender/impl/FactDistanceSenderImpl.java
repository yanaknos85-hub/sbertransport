package ru.sber.transport.driver_track.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.driver_track.dto.RouteDTO;
import ru.sber.transport.driver_track.dto.RouteSource;
import ru.sber.transport.driver_track.messaging.TripFactDistanceMessage;
import ru.sber.transport.driver_track.messaging.sender.TripFactDistanceSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FactDistanceSenderImpl implements TripFactDistanceSender {

    @Qualifier("factDistanceOutput")
    private final ObjectProvider<OutputBridge> factDistanceOutput;

    @Qualifier("factDistanceOutputSsl")
    private final ObjectProvider<OutputBridge> factDistanceOutputSsl;

    @Override
    public void send(Map<RouteSource, RouteDTO> routes, UUID tripId) {
        var distances = routes
                .entrySet()
                .stream()
                .map(kvp -> Map.entry(kvp.getKey().name(), kvp.getValue().getDistance()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        var message = new TripFactDistanceMessage(tripId, distances);
        factDistanceOutput.ifAvailable(ob -> ob.send(message));
        factDistanceOutputSsl.ifAvailable(ob -> ob.send(message));
    }
}
