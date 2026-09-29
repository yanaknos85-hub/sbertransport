package ru.sber.transport.journal.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;

import java.time.LocalDateTime;

/**
 * DTO Возврат на доработку
 *
 * @param comment Комментарий
 * @param creationTime Дата и время создания
 */
@Schema(title = "Возврат на доработку", description = "Возврат на доработку")
public record RevisionDto(
        @Schema(description = "Комментарий", nullable = true, minimum = "10", maximum = "255")
        @Size(min = 10, max = 255)
        String comment,
        @Schema(description = "Дата и время создания", requiredMode = Schema.RequiredMode.REQUIRED,
                type = "integer", format = "int64", example = "1696616506000")
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        @NotNull
        LocalDateTime creationTime
) {
}
