package ru.sber.transport.journal.dto;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Заявка, журнал
 *
 * @param id ID заявки
 * @param humanReadableId Человекочитаемый идентификатор
 * @param creationTime Дата и время создания заявки
 * @param changeTime Дата и время изменения заявки
 * @param status Статус заявки
 * @param stateNumber Государственный номер
 * @param brandByPassport Марка по ПТС
 * @param modelByPassport Модель по ПТС
 * @param year Год выпуска
 * @param evaluation Оценка сервиса
 * @param items Причины обращения
 * @param isRevisable Есть ли возможность вернуть заявку на доработку
 */
@Schema(title = "Заявка, журнал", description = "Данные заявки для отображения в журнале")
public record GetRequestJournalDto(
        @Schema(description = "ID заявки", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID id,
        @Schema(description = "Человекочитаемый идентификатор", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 255)
        @NotBlank
        @Size(max = 255)
        String humanReadableId,
        @Schema(description = "Дата и время создания заявки", requiredMode = Schema.RequiredMode.REQUIRED,
                type = "integer", format = "int64", example = "1696616506000")
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        @NotNull
        LocalDateTime creationTime,
        @Schema(description = "Дата и время изменения заявки", requiredMode = Schema.RequiredMode.REQUIRED,
                type = "integer", format = "int64", example = "1696616506000")
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        @NotNull
        LocalDateTime changeTime,
        @Schema(description = "Статус заявки", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 255)
        @NotBlank
        @Size(max = 255)
        String status,
        @Schema(description = "Государственный номер", nullable = true)
        String stateNumber,
        @Schema(description = "Марка по ПТС", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 150)
        @NotBlank
        @Size(max = 150)
        String brandByPassport,
        @Schema(description = "Модель по ПТС", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 150)
        @NotBlank
        @Size(max = 150)
        String modelByPassport,
        @Schema(description = "Год выпуска", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1900", maximum = "9999")
        @NotNull
        @Positive
        @Min(value = 1900)
        @Max(value = 9999)
        Integer year,
        @Schema(description = "Оценка сервиса", nullable = true, minimum = "1", maximum = "5")
        @Min(1)
        @Max(5)
        Integer evaluation,
        @Schema(description = "Причины обращения", requiredMode = Schema.RequiredMode.REQUIRED, minContains = 1)
        @NotEmpty
        List<String> items,
        @Schema(description = "Есть ли возможность вернуть заявку на доработку", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        boolean isRevisable
) {
}
