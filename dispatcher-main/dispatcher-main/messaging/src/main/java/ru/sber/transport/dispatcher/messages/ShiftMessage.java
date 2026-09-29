package ru.sber.transport.dispatcher.messages;

import ru.sber.transport.messaging.Message;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShiftMessage(
        UUID id,
        UUID contractorId,
        UUID driverId,
        UUID vehicleId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        boolean deleted,
        boolean active,
        UUID ewbId
) implements Message<UUID> {
    @Override
    public UUID getId() {
        return id;
    }
}