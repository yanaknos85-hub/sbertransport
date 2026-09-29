package ru.sber.transport.telemechanic.messaging.listener.message;

import ru.sber.transport.messaging.Message;

import java.util.UUID;

public record EwbTariffMessage(
        UUID id,
        UUID tariffId,
        UUID contractId,
        UUID organizationId,
        UUID departmentId,
        boolean active
) implements Message<UUID> {

    @Override
    public UUID getId() {
        return id;
    }
}
