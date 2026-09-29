package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * ДТО для получения списка названий всех активных организаций
 *
 * @param id Идентификатор организации
 * @param name Наименование организации
 */
@Schema(name = "GetAllActiveOrganizationNamesDto", title = "Список наименований активных организаций",
        description = "Ответ для запроса списка названий всех активных организаций")
public record GetAllActiveOrganizationNamesDto(
        
        @NotNull(message = "Идентификатор организации не может быть пустым")
        @Schema(description = "Идентификатор организации",
                requiredMode = Schema.RequiredMode.REQUIRED,
                type = "string",
                format = "uuid",
                example = "94649adc-4f7e-4778-a275-690a3471dc37",
                minLength = 36,
                maxLength = 36)
        UUID id,
        
        @NotBlank(message = "Наименование организации не может быть пустым")
        @Schema(description = "Наименование организации",
                requiredMode = Schema.RequiredMode.REQUIRED,
                type = "string",
                example = "ООО \"Сбербанк\"",
                maxLength = 255)
        String name
) {
}
