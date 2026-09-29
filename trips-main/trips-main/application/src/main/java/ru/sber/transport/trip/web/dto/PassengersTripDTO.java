package ru.sber.transport.trip.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "Информация о пассажирах", description = "Информация о пассажирах")
public record PassengersTripDTO(
        @Schema(description = "Временная зона")
        String timeZone,

        @Schema(description = "Информация о пассажирах")
        String passengers,

        @Schema(description = "Тип поездки индивидуальная/групповая")
        String type,

        @Schema(description = "Человекочитаемый идентификатор")
        String requestHumanReadableIds,

        @Schema(description = "Комментарий")
        String comments,

        @Schema(description = "Количество пассажиров")
        int count
) {
}
