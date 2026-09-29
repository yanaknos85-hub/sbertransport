package ru.sberbank.ditsib.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.sberbank.ditsib.converters.LocalDateTimeSerializer;
import ru.sberbank.ditsib.enumerate.MainLeadStatus;
import ru.sberbank.ditsib.enumerate.TransportType;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(name = "AggregatedMainLeadDto", description = "Ответ об основной заявке с точкой отправления и назначения")
public record AggregatedMainLeadDto(

        @Schema(description = "Идентификатор основной заявки")
        UUID mainLeadId,
        @Schema(description = "Тип транспорта")
        TransportType transportType,
        @Schema(description = "Стоимость всей поездки (сумма всех пользовательских заявок)")
        Integer cost,
        @Schema(description = "Количество свободных мест")
        Integer freeSeats,
        @Schema(description = "Время отправления")
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        LocalDateTime departureTime,
        @Schema(description = "Точка отправления")
        String startPoint,
        @Schema(description = "Точка окончания маршрута")
        String endPoint,
        @Schema(description = "Геозона")
        String geozone,
        @Schema(description = "Статус основной заявки")
        MainLeadStatus status
) {
}