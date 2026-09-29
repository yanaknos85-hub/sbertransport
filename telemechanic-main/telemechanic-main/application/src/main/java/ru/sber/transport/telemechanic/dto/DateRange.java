package ru.sber.transport.telemechanic.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;
import ru.sberbank.ditsib.validation.interval.Interval;

import java.time.LocalDateTime;

@Schema(name = "DateRange", title = "Диапазон дат", description = "Диапазон дат")
@Interval(startField = "start", endField = "end", inclusion = Interval.Include.INCLUDE)
public record DateRange(
        @NotNull
        @Schema(description = "Начало периода",
                requiredMode = Schema.RequiredMode.REQUIRED,
                type = "integer", format = "int64", example = "1696616506000")
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        LocalDateTime start,
        @NotNull
        @Schema(description = "Окончание периода",
                requiredMode = Schema.RequiredMode.REQUIRED,
                type = "integer", format = "int64", example = "1696616506000")
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        LocalDateTime end
) {
}
