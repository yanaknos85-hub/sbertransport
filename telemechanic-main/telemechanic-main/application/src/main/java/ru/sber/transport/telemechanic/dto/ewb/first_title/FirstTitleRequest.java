package ru.sber.transport.telemechanic.dto.ewb.first_title;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "FirstTitleRequest", title = "Запрос на формирование первого титула ЭПЛ", description = "Запрос на формирование первого титула ЭПЛ")
public record FirstTitleRequest(
        @NotNull
        @Schema(
                description = "Уникальный идентификатор ЭПЛ",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        UUID ewbUuid,
        
        @NotNull
        @Schema(
                description = "Дата начала перевозки",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "2023-08-20"
        )
        LocalDate startDate,
        
        @NotNull
        @Schema(
                description = "Дата окончания перевозки",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "2023-08-20"
        )
        LocalDate finishDate,
        
        @NotBlank
        @Size(min = 1, max = 3)
        @Schema(
                description = "Вид перевозки",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "СН",
                minLength = 1, maxLength = 3
        )
        String transportationType,
        
        @NotBlank
        @Size(min = 1, max = 2)
        @Schema(
                description = "Вид сообщения",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "Г",
                minLength = 1, maxLength = 2
        )
        String communicationType,
        
        @NotNull
        @Schema(
                description = "Идентификатор подразделения водителя",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        UUID tariffDepartmentId,
        
        @NotNull
        @Schema(
                description = "Идентификатор транспортного средства",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        UUID transportId,
        
        @NotNull
        @Schema(
                description = "Идентификатор водителя",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        UUID driverId
) {
}
