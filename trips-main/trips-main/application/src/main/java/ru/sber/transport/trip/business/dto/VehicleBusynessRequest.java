package ru.sber.transport.trip.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record VehicleBusynessRequest(

        @NotNull
        @Schema(title = "Время", description = "Время начала периода")
        OffsetDateTime startTime,

        @NotNull
        @Schema(title = "Время", description = "Время окончания периода")
        OffsetDateTime endTime,

        @Schema(title = "Список идентификаторов", description = "Список идентификаторов автомобилей")
        List<UUID> vehicleIds,

        @Schema(title = "Контрагент", description = "Идентификатор контрагента")
        UUID contractorId
) {
}
