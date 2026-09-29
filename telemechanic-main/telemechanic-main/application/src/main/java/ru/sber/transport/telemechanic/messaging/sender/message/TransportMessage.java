package ru.sber.transport.telemechanic.messaging.sender.message;

import ru.sber.transport.messaging.Message;

import java.util.List;
import java.util.UUID;

public record TransportMessage(
        UUID id,
        String stateNumber,
        String brand,
        String model,
        String transportType,
        int year,
        String vin,
        int currentMileage,
        String type,
        String subtype,
        List<UUID> organizationIds,
        Integer fuelTankVolume,
        boolean deleted,
        UUID contractorId,
        UUID autoparkId
) implements Message<UUID> {
    
    @Override
    public UUID getId() {
        return id;
    }
}
