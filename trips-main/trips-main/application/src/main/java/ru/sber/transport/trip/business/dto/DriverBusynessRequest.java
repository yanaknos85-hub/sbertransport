package ru.sber.transport.trip.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record DriverBusynessRequest(

        @NotNull
        @Schema(title = "Время", description = "Время начала периода")
        OffsetDateTime startTime,

        @NotNull
        @Schema(title = "Время", description = "Время окончания периода")
        OffsetDateTime endTime,

        @Schema(title = "Список идентификаторов", description = "Список идентификаторов поездки")
        List<UUID> driverIds,

        @Schema(title = "Признак", description = "Отобразить только запланированные поездки")
        boolean onlyPlanning,

        @Schema(title = "Признак", description = "Дополнительно отобразить в ответе поездки с забронированным автомобилем")
        boolean includeOrderedVehicles,

        @Schema(title = "Контрагент", description = "Идентификатор контрагента")
        UUID contractorId
) {
}
