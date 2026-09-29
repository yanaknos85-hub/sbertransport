package ru.sber.transport.telemechanic.messaging.listener.message;

import ru.sber.transport.messaging.Message;
import ru.sber.transport.telemechanic.enumerate.InspectionType;

import java.time.LocalDate;
import java.util.UUID;

public record EwbContractMessage(
        UUID id,
        UUID contractId,
        UUID organizationId,
        InspectionType inspectionType,
        String edfOperatorId,
        String edfCode,
        UUID organizationMedicalLicenseId,
        LocalDate start,
        LocalDate end,
        boolean active
) implements Message<UUID> {

    @Override
    public UUID getId() {
        return id;
    }
}
