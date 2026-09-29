package ru.sberbank.ditsib.transport.request.dto.validation;

import java.util.UUID;

public record TripSplitCheckDTO(
        long desiredDate,
        String timeZone,
        UUID employeeId,
        long expectedCost,
        long expectedDuration
) {
}
