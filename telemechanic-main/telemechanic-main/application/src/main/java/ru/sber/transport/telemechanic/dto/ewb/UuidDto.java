package ru.sber.transport.telemechanic.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(title = "Уникальный идентификатор ПЛ от системы КОРУС")
public record UuidDto(
        
        @NotNull
        @Schema(description = "Уникальный идентификатор ПЛ")
        UUID uuid
) {
}
