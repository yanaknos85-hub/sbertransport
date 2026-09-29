package ru.sber.transport.cargo.exchange.request.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.sber.transport.cargo.exchange.request.enums.VatType;

@Schema(description = "DTO для отклика грузоперевозчика на заявку")
@JsonIgnoreProperties(ignoreUnknown = true)
public record CarrierReplyDto(
        @Schema(description = "Информация об автомобиле", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Необходимо выбрать авто")
        @Valid
        Auto auto,

        @Schema(description = "Информация о прицепе")
        Trailer trailer,

        @Schema(description = "Информация о водителе", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Необходимо выбрать водителя")
        @Valid
        Driver driver,

        @Schema(description = "Предлагаемая стоимость перевозки", example = "5000.0", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Необходимо указать стоимость")
        @Positive(message = "Стоимость должна быть больше нуля")
        Double cost,

        @Schema(description = "Тип НДС", implementation = VatType.class)
        VatType vat,

        @Schema(description = "Комментарий к отклику", maxLength = 500)
        String comment
) {

    @Schema(description = "Водитель грузоперевозчика")
    public record Driver(
            @Schema(description = "ФИО водителя", example = "Иванов Иван Иванович", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "Необходимо указать ФИО водителя")
            String fio,

            @Schema(description = "Номер телефона водителя", example = "+79001234567", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "Необходимо указать номер телефона водителя")
            String phone,

            @Schema(description = "Номер водительских прав", example = "1234 567890")
            String licence
    ) {}

    @Schema(description = "Автомобиль грузоперевозчика")
    public record Auto(
            @Schema(description = "Наименование автомобиля", example = "Грузовик")
            String name,

            @Schema(description = "Марка автомобиля", example = "КамАЗ")
            String mark,

            @Schema(description = "Модель автомобиля", example = "5320")
            String model,

            @Schema(description = "Год выпуска", example = "2020")
            String year,

            @Schema(description = "Грузоподъёмность", example = "15 тонн")
            String capacity,

            @Schema(description = "Регистрационный номер", example = "А123БВ777", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "Необходимо указать регистрационный номер")
            String regNumber
    ) {}

    @Schema(description = "Прицеп грузоперевозчика")
    public record Trailer(
            @Schema(description = "Наименование прицепа", example = "Прицеп")
            String name,

            @Schema(description = "Модель прицепа", example = "ЦП-12")
            String model,

            @Schema(description = "Марка прицепа", example = "Урал")
            String mark,

            @Schema(description = "Регистрационный номер прицепа", example = "К456ЛМ777")
            String regNumber
    ) {}
}