package ru.sber.transport.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Данные о смене", description = "Данные о смене")
public class ShiftDTO {

    @Min(0)
    @Schema(description = "Номер строки")
    private int index;

    @Schema(description = "Идентификатор смены")
    private UUID id;

    @NotNull
    @Schema(description = "Идентификатор водителя")
    private UUID driverId;

    @NotNull
    @Schema(description = "Идентификатор автомобиля")
    private UUID vehicleId;

    @NotNull
    @Schema(description = "Дата начала смены")
    private LocalDateTime startDate;

    @NotNull
    @Schema(description = "Дата окончания смены")
    private LocalDateTime endDate;

    @Schema(description = "Активность смены")
    private boolean active;

    @Schema(description = "Удаленность смены")
    private boolean deleted;

    @Schema(description = "ID ряда")
    private UUID rowId;

    @Schema(description = "Идентификатор маршрута")
    private String routeId;

}
