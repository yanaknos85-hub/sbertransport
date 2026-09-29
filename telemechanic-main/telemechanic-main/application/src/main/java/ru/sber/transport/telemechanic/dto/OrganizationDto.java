package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * ДТО для получения списка названий всех активных организаций
 *
 * @param id Идентификатор организации
 * @param officialName Наименование организации
 */
@Schema(title = "Информация об организации", description = "Данные организации")
public record OrganizationDto(
        
        @NotNull
        @Schema(description = "Идентификатор организации",
                requiredMode = Schema.RequiredMode.REQUIRED,
                type = "string",
                format = "uuid",
                example = "94649adc-4f7e-4778-a275-690a3471dc37",
                minLength = 36,
                maxLength = 36)
        UUID id,
        
        @NotBlank
        @Schema(description = "Наименование организации",
                requiredMode = Schema.RequiredMode.REQUIRED,
                type = "string",
                example = "ООО \"Сбербанк\"",
                maxLength = 255)
        String officialName
) {
}
