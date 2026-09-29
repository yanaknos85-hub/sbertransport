package ru.sberbank.ditsib.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Данные одной заявки (лида) для основного сервиса
 */
public record LeadRequestMainDto(
        @NotNull
        UUID leadId,
        @Size(max = 255)
        @NotBlank
        String startAddress,
        @Size(max = 255)
        @NotBlank
        String endAddress,
        CoordinateDto startCoordinates,
        CoordinateDto endCoordinates,
        LocalDateTime desiredDatetime,
        Boolean isPersonal,
        Integer maxTimeTravel
) {
}