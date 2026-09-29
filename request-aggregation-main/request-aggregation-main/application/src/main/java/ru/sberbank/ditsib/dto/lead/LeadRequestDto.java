package ru.sberbank.ditsib.dto.lead;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.sberbank.ditsib.converters.LocalDateTimeDeserializer;
import ru.sberbank.ditsib.dto.point.PointLeadRequestDto;
import ru.sberbank.ditsib.enumerate.TransportClass;
import ru.sberbank.ditsib.enumerate.TransportType;
import ru.sberbank.ditsib.enumerate.TripType;

import java.time.LocalDateTime;
import java.util.List;

@Schema(name = "LeadRequestDtoApi", description = "Запрос на создание заявки (лида)")
public record LeadRequestDto(

        @NotNull
        @Schema(description = "Тип транспорта", requiredMode = Schema.RequiredMode.REQUIRED)
        TransportType transportType,

        @NotNull
        @Schema(description = "Цель поездки", requiredMode = Schema.RequiredMode.REQUIRED)
        TripType tripType,

        @NotNull
        @Schema(description = "Вид транспорта", requiredMode = Schema.RequiredMode.REQUIRED)
        TransportClass transportClass,

        @Schema(description = "Комментарий к поездке", maxLength = 1000)
        @Size(max = 1000)
        String comment,

        @NotNull
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @Schema(description = "Дата/Время отправления", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime departureTime,

        @Schema(description = "Точки маршрута", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty
        List<PointLeadRequestDto> points,

        @NotNull
        @Schema(description = "Является ли водителем", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean isDriver
) {
}

