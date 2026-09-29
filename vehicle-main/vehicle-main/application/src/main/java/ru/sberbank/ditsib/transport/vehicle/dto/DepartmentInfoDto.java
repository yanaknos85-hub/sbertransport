package ru.sberbank.ditsib.transport.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * ДТО для информации о департаменте
 *
 * @param id Идентификатор департамента
 * @param departmentName Наименование департамента
 * @param parentId Идентификатор родительского департамента
 */
@Schema(name = "DepartmentInfoDto", title = "Данные о департаменте")
public record DepartmentInfoDto(
        @NotNull(message = "Идентификатор департамента не может быть пустым")
        @Schema(description = "Идентификатор департамента",
                requiredMode = Schema.RequiredMode.REQUIRED,
                type = "string",
                format = "uuid",
                example = "94649adc-4f7e-4778-a275-690a3471dc37",
                minLength = 36,
                maxLength = 36)
        UUID id,
        
        @NotBlank(message = "Наименование департамента не может быть пустым")
        @Schema(description = "Наименование департамента", example = "Дополнительный офис № 8606/0107")
        String departmentName,
        
        @Schema(description = "Идентификатор родительского департамента",
                type = "string",
                format = "uuid",
                example = "94649adc-4f7e-4778-a275-690a3471dc37",
                minLength = 36,
                maxLength = 36)
        UUID parentId
) {
}
