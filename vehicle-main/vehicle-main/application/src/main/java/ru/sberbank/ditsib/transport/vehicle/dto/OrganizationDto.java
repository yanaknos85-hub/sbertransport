package ru.sberbank.ditsib.transport.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(title = "Информация об организации", description = "Данные организации")
public record OrganizationDto(
        
        @Schema(description = "Идентификатор организации")
        UUID id,
        
        @Schema(description = "Наименование организации")
        String officialName,
        
        @Schema(description = "Уникальный числовой идентификатор")
        Long digitId
) {
}
