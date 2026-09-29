package ru.sber.transport.deadline.grpc.common.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

public record ProlongateRequestDto(
        @NotNull
        LocalDateTime dateTime,
        @NotNull
        Unit unit,
        @PositiveOrZero
        int count
) {
}
