package ru.sberbank.ditsib.transport.request.database.dao.projection;

import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public record RequestForPersonalSplitCheckProjection(
        UUID id,
        String humanReadableId,
        LocalDateTime desiredDate,
        Duration expectedDuration,
        TripRequestStatus status,
        String employeeDeviceTimeZone
) {
}
