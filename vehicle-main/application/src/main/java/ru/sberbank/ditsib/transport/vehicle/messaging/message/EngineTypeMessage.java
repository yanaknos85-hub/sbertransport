package ru.sberbank.ditsib.transport.vehicle.messaging.message;

import ru.sber.transport.messaging.Message;

import java.util.UUID;

public record EngineTypeMessage(
        UUID id,
        String title,
        boolean deleted
) implements Message<UUID> {
    @Override
    public UUID getId() {
        return id;
    }
}
