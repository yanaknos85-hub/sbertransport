package ru.sber.transport.dispatcher.messages;

import ru.sber.transport.messaging.Message;

import java.util.UUID;

public record IntegrationClientMessage(
        UUID id,
        /**
         * ID контрагента
         */
        UUID contractorId,
        boolean active
) implements Message<UUID> {
    @Override
    public UUID getId() {
        return id;
    }
}
