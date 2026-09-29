package ru.sberbank.ditsib.transport.request.messaging.message;

import ru.sber.transport.messaging.Message;

import java.util.UUID;

public record ResourceIdMessage(
        UUID id
) implements Message<UUID> {

    @Override
    public UUID getId() {
        return id;
    }
}
