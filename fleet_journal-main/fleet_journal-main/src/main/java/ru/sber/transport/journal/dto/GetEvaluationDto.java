package ru.sber.transport.journal.dto;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO Оценка (чтение)
 *
 * @param rating Оценка
 * @param comment Комментарий
 * @param receivingTime Дата и время получения оценки
 * @param reasons Причины
 */
@Schema(title = "Оценка (чтение)", description = "Данные оценки (чтение)")

public record GetEvaluationDto(
        @Schema(description = "Оценка", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1", maximum = "5")
        @NotNull
        @Min(1)
        @Max(5)
        Integer rating,
        @Schema(description = "Комментарий", nullable = true, maxLength = 255)
        @Size(max = 255)
        String comment,
        @Schema(description = "Дата и время получения оценки", requiredMode = Schema.RequiredMode.REQUIRED,
                type = "integer", format = "int64", example = "1696616506000")
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        @NotNull
        LocalDateTime receivingTime,
        @Schema(description = "Причины", nullable = true)
        List<String> reasons
) {
}