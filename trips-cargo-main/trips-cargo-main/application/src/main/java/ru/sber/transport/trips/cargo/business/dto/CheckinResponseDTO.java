package ru.sber.transport.trips.cargo.business.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Информация о всех чек-инах поездки")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckinResponseDTO {

    @Schema(description = "Список чек-инов")
    private List<CheckinDTO> chekins;

    @Schema(description = "Длительность поездки")
    private TripDurationDTO tripDuration;

    @Schema(description = "Признак полноты выполнения заказа")
    private boolean isComplete;
}
