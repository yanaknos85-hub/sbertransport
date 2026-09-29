package ru.sber.transport.contractor.messages;

import ru.sber.transport.messaging.Message;

import java.util.UUID;

public record VehicleMessage(
        UUID id,

        String brand,

        String model,

        String stateNumber,

        String color,

        UUID contractorId,

        boolean deleted

) implements Message<UUID> {
    @Override
    public UUID getId() {
        return id;
    }
}
