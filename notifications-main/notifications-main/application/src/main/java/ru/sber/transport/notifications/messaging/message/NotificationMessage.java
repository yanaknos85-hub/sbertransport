package ru.sber.transport.notifications.messaging.message;

import ru.sber.transport.messaging.Message;

import java.util.List;
import java.util.UUID;

public record NotificationMessage(

        UUID id,
        List<UUID> receivers,
        String messageType,
        String applicationType,
        List<Data> data

) implements Message<UUID> {

    public record Data(

            String key,

            Object value
    ) {
    }

    @Override
    public UUID getId() {
        return id;
    }
}


