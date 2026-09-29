package ru.sber.transport.telemechanic.enumerate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "Статусная модель авто")
@RequiredArgsConstructor
@Getter
public enum TransportStatus {
    @Schema(description = "В эксплуатации")
    IN_USE("В эксплуатации"),
    @Schema(description = "Выведен из эксплуатации")
    NOT_IN_USE("Выведен из эксплуатации");

    private final String description;
}
