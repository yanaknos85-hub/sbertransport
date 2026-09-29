package ru.sber.transport.trips.cargo.business.dto.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "Информация о водителе", description = "Координаты водителя")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DriverLocationDTO(

        @Schema(description = "Долгота")
        Double longitude,

        @Schema(description = "Широта")
        Double latitude
) {
}
