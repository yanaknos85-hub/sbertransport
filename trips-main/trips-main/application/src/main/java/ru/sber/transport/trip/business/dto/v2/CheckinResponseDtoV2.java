package ru.sber.transport.trip.business.dto.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.trip.business.dto.TripDurationDTO;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Информация о всех чек-инах поездки")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckinResponseDtoV2 {

    @Schema(description = "Список чек-инов")
    private List<CheckinDtoV2> chekins;

    @Schema(description = "Длительность поездки")
    private TripDurationDTO tripDuration;

    @Schema(description = "Признак полноты выполнения заказа")
    private boolean isComplete;

    @Schema(description = "Водитель")
    private DriverLocationDTO driverLocation;
}
