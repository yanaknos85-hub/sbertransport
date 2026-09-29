package ru.sber.transport.dispatcher.messages;

import ru.sber.transport.messaging.Message;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

public record CreateEwbMessage(
        UUID id,
        String fileName,
        String content,
        String signature,
        LocalDateTime creationTime,
        String humanReadableId,
        LocalDate startDate,
        LocalDate finishDate,
        String transportationType,
        String communicationType,
        UUID tariffDepartmentId,
        UUID transportId,
        UUID driverEmployeeId,
        UUID userId,
        UUID ewbUuid
) implements Message<UUID> {

    @Override
    public UUID getId() {
        return id;
    }
}
