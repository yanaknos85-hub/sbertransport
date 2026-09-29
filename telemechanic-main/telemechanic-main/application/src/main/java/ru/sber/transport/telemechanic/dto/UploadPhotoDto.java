package ru.sber.transport.telemechanic.dto;

import ru.sber.transport.telemechanic.database.model.Check;

import java.util.UUID;

public record UploadPhotoDto(
        UUID requestId,
        Check check
) {
}
