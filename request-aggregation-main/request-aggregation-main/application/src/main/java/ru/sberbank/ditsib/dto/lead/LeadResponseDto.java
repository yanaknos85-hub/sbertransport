package ru.sberbank.ditsib.dto.lead;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.sberbank.ditsib.converters.LocalDateTimeSerializer;
import ru.sberbank.ditsib.dto.EmployeeDto;
import ru.sberbank.ditsib.dto.point.PointLeadResponseDto;
import ru.sberbank.ditsib.enumerate.LeadStatus;
import ru.sberbank.ditsib.enumerate.TransportType;
import ru.sberbank.ditsib.enumerate.TripType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Ответ о сохраненной пользовательской заявке
 */
@Schema(name = "LeadResponseDto", description = "Ответ о сохраненной пользовательской заявке")
public record LeadResponseDto(

        @Schema(description = "Идентификатор заявки")
        UUID id,
        @Schema(description = "Статус заявки")
        LeadStatus status,
        @Schema(description = "Дата и время создания заявки")
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        LocalDateTime createDateTime,
        @Schema(description = "Тип поездки", example = "DAYTIME_TRIP")
        TripType tripType,
        @Schema(description = "Тип транспорта", example = "TAXI")
        TransportType transportType,
        @Schema(description = "Комментарий")
        String comment,
        @Schema(description = "Время и дата отправления")
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        LocalDateTime departureTime,
        @Schema(description = "Сотрудник, который создал заявку")
        EmployeeDto employee,
        @Schema(description = "Точки маршрута")
        List<PointLeadResponseDto> points
) {
}