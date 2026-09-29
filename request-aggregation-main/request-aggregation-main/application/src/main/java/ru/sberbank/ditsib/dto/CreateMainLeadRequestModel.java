package ru.sberbank.ditsib.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Доменная модель заявки для основного сервиса
 * Содержит данные для отправки во внешний API
 */
public record CreateMainLeadRequestModel(
        @NotNull
        UUID leadId,
        @Size(max = 255)
        @NotBlank
        String startAddress,
        @Size(max = 255)
        @NotBlank
        String endAddress,
        TariffCoordinateModel startCoordinates,
        TariffCoordinateModel endCoordinates,
        LocalDateTime desiredDatetime,
        Boolean isPersonal,
        Integer maxTimeTravel
) {
} 