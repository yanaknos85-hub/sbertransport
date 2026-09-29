package ru.sber.transport.journal.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.With;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;

import java.time.LocalDateTime;

/**
 * DTO история изменения заявок
 *
 * @param date Дата и время изменения заявки
 * @param status Статус заявки
 * @param type Тип статуса (прошедший/текущий)
 * @param initiatorName ФИО сотрудника инициатора изменения
 */
@With
@Schema(title = "История изменения заявок", description = "Данные по истории изменения заявок")
public record GetStatusDto(
        @Schema(description = "Дата и время изменения заявки", requiredMode = Schema.RequiredMode.REQUIRED,
                type = "integer", format = "int64", example = "1696616506000")
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        @NotNull
        LocalDateTime date,
        @Schema(description = "Статус заявки", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 255)
        @NotBlank
        @Size(max = 255)
        String status,
        @Schema(description = "Тип статуса (прошедший/текущий)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        TypeEnum type,
        @Schema(description = "ФИО сотрудника инициатора изменения", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String initiatorName
) {
    /**
     * Тип статуса
     */
    public enum TypeEnum {
        /**
         * Прошедший
         */
        PAST,
        /**
         * Текущий
         */
        NOW
    }
}
