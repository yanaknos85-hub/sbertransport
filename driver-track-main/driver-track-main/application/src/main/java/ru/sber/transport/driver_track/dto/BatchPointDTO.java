package ru.sber.transport.driver_track.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Schema(title = "Batch Point DTO", description = "Одна точка координат из батча")
public record BatchPointDTO(
    @NotNull
    @Schema(description = "Широта", example = "55.7558")
    Double latitude,

    @NotNull
    @Schema(description = "Долгота", example = "37.6173")
    Double longitude,

    @NotNull
    @Schema(description = "Время в формате ISO-8601 UTC", example = "2026-07-12T10:00:00Z")
    Instant timestamp
) {}
