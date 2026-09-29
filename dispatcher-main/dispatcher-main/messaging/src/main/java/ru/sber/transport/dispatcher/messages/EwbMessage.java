package ru.sber.transport.dispatcher.messages;

import ru.sber.transport.messaging.Message;

import java.util.UUID;

public record EwbMessage(
        UUID id,
        String errorText
) implements Message<UUID> {
    @Override
    public UUID getId() {
        return id;
    }
}
