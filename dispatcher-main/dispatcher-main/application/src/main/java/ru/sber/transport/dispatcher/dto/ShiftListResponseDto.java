package ru.sber.transport.dispatcher.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;
import ru.sber.transport.dispatcher.serde.TimestampSerializer;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.UUID;

@Setter
@Getter
@SuperBuilder
@AllArgsConstructor
@Schema(title = "Ответ на запрос на получение списка смен", description = "Ответ на запрос на получение списка смен")
public class ShiftListResponseDto {

    @Schema(description = "Идентификатор смены")
    @NotNull
    private UUID id;

    @Schema(description = "ФИО водителя")
    @NotNull
    private String driverName;

    @Schema(description = "Марка автомобиля")
    @NotNull
    private String vehicleBrand;

    @Schema(description = "Модель автомобиля")
    @NotNull
    private String vehicleModel;

    @Schema(description = "Госномер автомобиля")
    @NotNull
    private String vehicleStateNumber;

    @Schema(description = "Дата начала смены")
    @NotNull
    @JsonSerialize(using = TimestampSerializer.class)
    private Timestamp startDate;

    @Schema(description = "Дата окончания смены")
    @NotNull
    @JsonSerialize(using = TimestampSerializer.class)
    private Timestamp endDate;
}
