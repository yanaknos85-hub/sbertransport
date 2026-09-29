package ru.sber.transport.cargo.exchange.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(name = "RequestSummary", description = "Краткая информация о заявках для отображения на главной странице")
public record MarketplaceRequestDto(
        @Schema(description = "Идентификатор заявки", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,
        @Schema(description = "Человекочитаемый номер заявки: формат ОП-ГГГГММ-ННННННН, например ОП-202601-0000001", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("humanreadableId")
        String humanReadableId,
        @Schema(description = "Адрес погрузки")
        String addressFrom,
        @Schema(description = "Адресс выгрузки")
        String addressTo,
        @Schema(description = "Дата погрузки", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025-09-12")
        LocalDate loadingDate,
        @Schema(description = "Дата доставки", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025-09-13")
        LocalDate deliveryDate,
        @Schema(
                description = "Тип кузова (например, тент, рефрижератор). Ссылка на справочник vehicle_body_types.id",
                maxLength = 100
        )
        List<String> vehicleBodyType,
        @Schema(description = "Расстояние перевозки в километрах, округлённое по правилам математики")
        Integer distance,
        @Schema(description = "Общий вес груза в тоннах, округлённый")
        BigDecimal weight,
        @Schema(description = "Общий объём груза в кубометрах, округлённый")
        BigDecimal volume,
        @Schema(description = "Ставка за километр в рублях", type = "string", example = "67.00")
        BigDecimal ratePerKm,
        @Schema(description = "Стоимость заявки", example = "500000.0")
        Double costRequest,
        @Schema(description = "Флаг включения НДС")
        Boolean vatInclude,
        @Schema(description = "Флаг наличия собственного отклика (пользователь, инициировавший запрос, откликнулся на данную заявку)")
        Boolean selfReplied
) {
}