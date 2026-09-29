package ru.sber.transport.cargo.exchange.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "CarrierRequest", description = "Информация о заявке для грузоперевозчика в списке 'Мои заявки'")
public record CarrierRequestDto(
        @Schema(description = "Идентификатор заявки", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,

        @Schema(description = "Человеко-читаемый номер заявки: формат ОП-ГГГГММ-ННННННН, например ОП-202601-0000001", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("humanreadableId")
        String humanReadableId,

        @Schema(description = "Адрес погрузки", requiredMode = Schema.RequiredMode.REQUIRED)
        String addressFrom,

        @Schema(description = "Адрес выгрузки", requiredMode = Schema.RequiredMode.REQUIRED)
        String addressTo,

        @Schema(description = "Статус заявки", requiredMode = Schema.RequiredMode.REQUIRED)
        String status,

        @Schema(description = "Дата и время погрузки", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-02-02T08:00:00")
        LocalDate loadingDate,

        @Schema(description = "Дата и время разгрузки", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-02-04T10:00:00")
        LocalDate deliveryDate,

        @Schema(description = "Ставка за перевозку")
        BigDecimal costRequest,

        @Schema(description = "Флаг использования ЕТРН", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean etrn
) {
}