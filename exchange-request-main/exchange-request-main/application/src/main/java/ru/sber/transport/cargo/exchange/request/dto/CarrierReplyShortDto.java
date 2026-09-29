package ru.sber.transport.cargo.exchange.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Краткая информация об отклика грузоперевозчика на заявку")
public record CarrierReplyShortDto (
        @Schema(description = "Уникальный идентификатор отклика", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Наименование грузоперевозчика", example = "ООО ТрансСервис")
        String carrier,

        @Schema(description = "Рейтинг грузоперевозчика", example = "4.8", minimum = "0", maximum = "5")
        Double rate,

        @Schema(description = "Контактный телефон грузоперевозчика", example = "+79001234567")
        String phone,

        @Schema(description = "Информация об автомобиле")
        Auto auto,

        @Schema(description = "Предлагаемая стоимость перевозки", example = "5000.0", requiredMode = Schema.RequiredMode.REQUIRED)
        Double cost,

        @Schema(description = "Комментарий к отклику", maxLength = 500)
        String comment
) {
    @Schema(description = "Краткая информация об автомобиле")
    public record Auto (
            @Schema(description = "Тип автомобиля", example = "Фура 15тонн")
            String type,

            @Schema(description = "Наименование автомобиля", example = "Камаз 1979")
            String name
    ) {}
}