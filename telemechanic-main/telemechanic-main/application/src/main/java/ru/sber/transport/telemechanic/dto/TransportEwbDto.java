package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(title = "Транспортное средство с полями для ЭПЛ", description = "Транспортное средство с полями для ЭПЛ")
public record TransportEwbDto(
        @Schema(description = "Идентификатор транспортного средства")
        UUID id,
        @Schema(description = "Показания одометра")
        Integer mileage,
        @Schema(description = "Гос.номер транспортного средства")
        String stateNumber,
        @Schema(description = "Наименование транспортного средства")
        String brand,
        @Schema(description = "Модель транспортного средства")
        String model
) {
}
