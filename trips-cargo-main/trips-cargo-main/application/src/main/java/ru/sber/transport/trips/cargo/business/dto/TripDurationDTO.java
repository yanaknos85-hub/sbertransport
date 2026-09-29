package ru.sber.transport.trips.cargo.business.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Информация о длительности поездки")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TripDurationDTO {

    @Schema(description = "Дни")
    private long days;

    @Schema(description = "Часы")
    private long hours;

    @Schema(description = "Минуты")
    private long minutes;

    @Schema(description = "Секунды")
    private long seconds;
}
