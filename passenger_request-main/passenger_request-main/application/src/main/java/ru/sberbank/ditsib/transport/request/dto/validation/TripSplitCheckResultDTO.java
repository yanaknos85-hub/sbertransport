package ru.sberbank.ditsib.transport.request.dto.validation;

public record TripSplitCheckResultDTO(boolean isValid, ExistingConflictRequestDTO existingConflictRequest) {
}

