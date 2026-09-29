package ru.sber.transport.trip.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.sber.transport.trip.business.model.TripStatus;

import java.util.UUID;

@Schema(title = "Поездка V2", description = "Данные поездки, доступные для редактирования")
public record EditTripV2Dto(
        TripStatus status,
        UUID driverId,
        Double factDistance,
        Long driverWaitingTime
) implements EditTripDataDto {
}
