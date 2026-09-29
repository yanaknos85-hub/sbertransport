package ru.sber.transport.dispatcher.messages;

import ru.sber.transport.messaging.Message;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

public record DriverMessage (
        UUID id,
        String lastName,
        String firstName,
        String patronymic,
        String passport,
        UUID contractorId,
        boolean active,
        int rating,
        String driverLicenseNumber,
        String cargoLicenceNumber,
        String serviceLicenseNumber,
        Double latitude,
        Double longitude,
        ZonedDateTime pointTime,
        String timeZone,
        boolean serving,
        boolean online,
        UUID activeTripId,
        UUID activeShiftId,
        Set<String> licenseClasses,
        String experience,
        String contactPhone,
        boolean phoneConfirmed,
        String email,
        Boolean consent,
        String humanReadableId,
        String driverSpeciality,
        UUID oauthId,
        UUID autoparkId,
        String tin,
        String snils,
        LocalDate issueDate,
        LocalDate expiryDate
) implements Message<UUID> {
    @Override
    public UUID getId() {
        return id;
    }
}
