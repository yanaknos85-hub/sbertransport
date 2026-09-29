package ru.sber.transport.driver_track.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

@Schema(title = "Batch Coordinate Request", description = "Пакетная отправка координат")
public record BatchCoordinateRequest(
    @NotNull
    @Schema(description = "Идентификатор поездки")
    UUID tripId,

    @NotEmpty
    @Size(max = 500, message = "Batch size must not exceed 500 points")
    @Schema(description = "Массив точек координат")
    List<@Valid BatchPointDTO> points
) {}
