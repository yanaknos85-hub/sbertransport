package ru.sber.transport.trip.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.sber.transport.trip.business.model.TripStatus;

@Schema(title = "Поездка", description = "Данные по завершенной поездке")
public record TripFactInfoDto(

        @Schema(title = "Статус", description = "Статус поездки")
        TripStatus status,

        @Schema(title = "Время", description = "Начало временного промежутка")
        Long startTime,

        @Schema(title = "Время", description = "Конец временного промежутка")
        Long endTime,

        @Schema(title = "Дистанция", description = "Пройденная дистанция в рамках временного промежутка")
        Double passedDistance
) {
}
