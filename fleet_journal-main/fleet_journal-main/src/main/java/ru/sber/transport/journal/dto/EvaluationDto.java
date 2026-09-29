package ru.sber.transport.journal.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO Оценка (запись)
 *
 * @param rating Оценка
 * @param comment Комментарий
 * @param reasons Причины
 */
@Schema(title = "Оценка (запись)", description = "Данные оценки (запись)")
public record EvaluationDto(
        @Schema(description = "Оценка", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1", maximum = "5")
        @NotNull
        @Min(1)
        @Max(5)
        Integer rating,
        @Schema(description = "Комментарий", nullable = true, maxLength = 255)
        @Size(max = 255)
        String comment,
        @Schema(description = "Причины", nullable = true)
        List<String> reasons
) {
}