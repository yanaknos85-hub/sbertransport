package ru.sber.transport.telemechanic.messaging.listener.message;

import ru.sber.transport.messaging.Message;

import java.time.LocalDate;
import java.util.UUID;

public record DispatcherMessage(
        UUID id,
        String humanReadableId,
        String lastName,
        String firstName,
        String patronymic,
        String phone,
        boolean phoneConfirmed,
        String email,
        UUID contractorId,
        boolean active,
        Boolean consent,
        UUID oauthId,
        UUID autoparkId,
        boolean ewbCreationPossibility,
        LocalDate issueDate,
        LocalDate expiryDate,
        String creationSystem,
        UUID attorneyNumber
) implements Message<UUID> {
    
    @Override
    public UUID getId() {
        return id;
    }
    
}