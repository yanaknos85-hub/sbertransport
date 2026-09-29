package ru.sberbank.ditsib.transport.request.dto.validation;

import java.util.UUID;

public record ExistingConflictRequestDTO(UUID id, String humanReadableId, String status) {
}
