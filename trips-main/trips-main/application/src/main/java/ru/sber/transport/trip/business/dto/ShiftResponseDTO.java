package ru.sber.transport.trip.business.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.sber.transport.trip.serializer.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@SuperBuilder
@AllArgsConstructor
@Schema(title = "Ответ на запрос на получение смен", description = "Ответ на запрос на получение смен")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShiftResponseDTO {

    @Schema(description = "Идентификатор смены")
    @NotNull
    private UUID id;

    @Schema(description = "Идентификатор водителя")
    @NotNull
    private UUID driverId;

    @Schema(description = "Идентификатор автомобиля")
    @NotNull
    private VehicleDTO vehicle;

    @Schema(description = "Дата начала смены")
    @NotNull
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime startDate;

    @Schema(description = "Дата окончания смены")
    @NotNull
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime endDate;

    @Schema(description = "Показатель активности смены")
    @NotNull
    private boolean active;
}
