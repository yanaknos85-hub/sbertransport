package ru.sber.transport.trip.business.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.trip.business.model.TripStatus;
import ru.sber.transport.trip.serializer.LocalDateTimeSerializer;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Информация о чек-ине")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckinDTO {

    @Schema(description = "Долгота")
    private double longitude;

    @Schema(description = "Широта")
    private double latitude;

    @Schema(description = "Время")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime time;

    @Schema(description = "Временная зона")
    private String timeZone;

    @Schema(description = "Статус поездки")
    private TripStatus status;

    @Schema(description = "Тип чек-ина")
    private CheckinType type;
}
