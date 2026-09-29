package ru.sber.transport.telemechanic.dto.telemedicine;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.sber.transport.telemechanic.converter.LocalDateTimeWithZoneDeserializer;
import ru.sber.transport.telemechanic.converter.LocalDateTimeWithZoneSerializer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Данные")
public record ExamInfo(
        @Schema(description = "Статус медицинского осмотра")
        boolean medicRequestStatus,
        @NotNull
        @JsonSerialize(using = LocalDateTimeWithZoneSerializer.class)
        @JsonDeserialize(using = LocalDateTimeWithZoneDeserializer.class)
        @Schema(description = "Дата создания титула №2")
        LocalDateTime creationDateTime,
        @NotNull
        @JsonSerialize(using = LocalDateTimeWithZoneSerializer.class)
        @JsonDeserialize(using = LocalDateTimeWithZoneDeserializer.class)
        @Schema(description = "Дата принятия решения о медицинском осмотре")
        LocalDateTime medicDecisionDateTime,
        @Schema(description = "Систолическое давление")
        int systPressure,
        @Schema(description = "Диастолическое давление")
        int dyastPressure,
        @Schema(description = "Пульс")
        int pulse,
        @NotNull
        @Schema(description = "Температура")
        BigDecimal temperature,
        @NotNull
        @Schema(description = "Алкоголь в выдыхаемом воздухе")
        BigDecimal alcohol,
        @NotBlank
        @Schema(description = "Комментарий")
        String comment
) {
}