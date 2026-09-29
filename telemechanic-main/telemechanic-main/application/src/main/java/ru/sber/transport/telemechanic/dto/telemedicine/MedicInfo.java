package ru.sber.transport.telemechanic.dto.telemedicine;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.sber.transport.telemechanic.converter.LocalDateTimeWithZoneDeserializer;
import ru.sber.transport.telemechanic.converter.LocalDateTimeWithZoneSerializer;

import java.time.LocalDateTime;

@Schema(description = "Информация о враче")
public record MedicInfo(
        @NotBlank
        @Schema(description = "ФИО")
        String fio,
        @NotBlank
        @Schema(description = "Табельный номер")
        String personalNumber,
        @NotBlank
        @Schema(description = "Организация")
        String organization,
        @NotBlank
        @Schema(description = "Подразделение")
        String department,
        @NotBlank
        @Schema(description = "Должность")
        String position,
        @NotBlank
        @Schema(description = "Номер открытого ключа электронной подписи")
        String serialNumber,
        @NotNull
        @JsonSerialize(using = LocalDateTimeWithZoneSerializer.class)
        @JsonDeserialize(using = LocalDateTimeWithZoneDeserializer.class)
        @Schema(description = "Дата окончания действия электронной подписи")
        LocalDateTime serialEndDateTime
) {}
