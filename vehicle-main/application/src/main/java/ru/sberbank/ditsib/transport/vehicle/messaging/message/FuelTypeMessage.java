package ru.sberbank.ditsib.transport.vehicle.messaging.message;

import ru.sber.transport.messaging.Message;

import java.util.List;
import java.util.UUID;

public record FuelTypeMessage(
        UUID id,
        String title,
        UUID engineTypeId,
        List<String> possibleTitles,
        boolean deleted
) implements Message<UUID> {
    @Override
    public UUID getId() {
        return id;
    }
}
