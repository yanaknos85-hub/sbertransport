package ru.sber.transport.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@SuperBuilder
@AllArgsConstructor
@Schema(title = "Ответ на запрос на получение смен", description = "Ответ на запрос на получение смен")
public class ShiftResponseDTO {

    @Schema(description = "Идентификатор смены")
    @NotNull
    private UUID id;

    @Schema(description = "Идентификатор водителя")
    @NotNull
    private DriverDTO driver;

    @Schema(description = "Идентификатор автомобиля")
    @NotNull
    private VehicleDTO vehicle;

    @Schema(description = "Дата начала смены")
    @NotNull
    private LocalDateTime startDate;

    @Schema(description = "Дата окончания смены")
    @NotNull
    private LocalDateTime endDate;

    @Schema(description = "Показатель активности смены")
    @NotNull
    private boolean active;

    @Schema(description = "ID ряда смен")
    @NotNull
    private UUID rowId;
}
