package ru.sber.transport.telemechanic.dto.telemedicine;

import java.util.UUID;

public record TelemedicFirstTitleRequest(
        String name,
        String content,
        UUID ewbUuid,
        String driverSnils
) {
}
