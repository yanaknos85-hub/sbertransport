package ru.sber.transport.telemechanic.messaging.listener.message;

import ru.sber.transport.messaging.Message;

import java.util.UUID;

public record FleetOwnerOrganizationMessage(
        UUID id,
        UUID organizationId,
        String edfOperatorId,
        String edfCode
) implements Message<UUID> {

    @Override
    public UUID getId() {
        return id;
    }
}
