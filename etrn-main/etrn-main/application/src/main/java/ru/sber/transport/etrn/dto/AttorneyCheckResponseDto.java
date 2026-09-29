package ru.sber.transport.etrn.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;
import ru.sberbank.ditsib.converters.LocalDateDeserializer;
import ru.sberbank.ditsib.converters.LocalDateSerializer;

import java.time.LocalDate;

/**
 * Ответ проверки доверенности сотрудника.
 *
 * @param attorneyNumber номер доверенности
 * @param issueDate      дата выдачи доверенности
 * @param expiryDate     дата окончания срока действия доверенности
 */
@Schema(title = "Ответ проверки доверенности", description = "Результат проверки доверенности сотрудника")
public record AttorneyCheckResponseDto(

        @Schema(
                title = "Номер доверенности",
                description = "Номер доверенности сотрудника",
                example = "TRN-2025-001"
        )
        String attorneyNumber,

        @Schema(
                title = "Дата выдачи",
                description = "Дата выдачи доверенности",
                example = "2025-01-15"
        )
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        LocalDate issueDate,

        @Schema(
                title = "Дата окончания",
                description = "Дата окончания срока действия доверенности",
                example = "2026-01-15"
        )
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        LocalDate expiryDate
) {
}
