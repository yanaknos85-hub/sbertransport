package ru.sber.transport.trip.business.dto.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.trip.business.dto.CheckinType;
import ru.sber.transport.trip.business.model.TripStatus;
import ru.sber.transport.trip.serializer.LocalDateTimeSerializer;
import ru.sber.transport.trip.serializer.OffsetDateTimeSerializer;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Информация о чек-ине")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckinDtoV2 {

    @Schema(description = "Долгота")
    private double longitude;

    @Schema(description = "Широта")
    private double latitude;

    @Schema(description = "Время")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime time;

    @Schema(description = "Статус поездки")
    private TripStatus status;

    @Schema(description = "Тип чек-ина")
    private CheckinType type;

}
