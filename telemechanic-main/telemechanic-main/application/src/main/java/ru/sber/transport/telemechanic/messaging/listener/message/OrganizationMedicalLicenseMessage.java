package ru.sber.transport.telemechanic.messaging.listener.message;

import ru.sber.transport.messaging.Message;

import java.time.LocalDate;
import java.util.UUID;

public record OrganizationMedicalLicenseMessage(
        UUID id,
        String series,
        String number,
        LocalDate issueDate,
        LocalDate expiryDate
) implements Message<UUID> {

    @Override
    public UUID getId() {
        return id;
    }
}
