package ru.sber.transport.cargo.exchange.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "RequestSummary", description = "Краткая информация о заявках для отображения на главной странице")
public record ShipperRequestDto(
        @Schema(description = "Идентификатор заявки", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,
        @Schema(description = "Человекочитаемый номер заявки: формат ОП-ГГГГММ-ННННННН, например ОП-202601-0000001", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("humanreadableId")
        String humanReadableId,
        @Schema(description = "Адрес погрузки")
        String addressFrom,
        @Schema(description = "Адрес выгрузки")
        String addressTo,
        @Schema(description = "Дата погрузки", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025-09-12")
        LocalDate loadingDate,
        @Schema(description = "Дата доставки", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025-09-13")
        LocalDate deliveryDate,
        @Schema(description = "Стоимость заявки", example = "500000.0")
        Double costRequest,
        @Schema(description = "Статус")
        String status,
        @Schema(description = "Номер заказа в системе грузовладельца")
        String internalId
) {
}