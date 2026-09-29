package ru.sberbank.ditsib.transport.vehicle.dto.vehicle;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(title = "Сокращенный состав аттрибутов", description = "Сокращенный состав аттрибутов для отображения при получении всех автомобилей")
public record VehicleShortDto(
        @NotNull
        @Schema(description = "Идентификатор Автомобиля")
        UUID id,
        @NotBlank(message = "Наименование марки ТС не может быть пустым")
        @Size(min = 1, max = 255)
        @Schema(description = "Наименование марки ТС", minLength = 1, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
        String brand,
        @NotBlank(message = "Наименование модели ТС не может быть пустым")
        @Size(min = 1, max = 255)
        @Schema(description = "Наименование модели ТС", minLength = 1, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
        String model,
        @NotBlank(message = "Наименование типа двигятеля ТС не может быть пустым")
        @Size(min = 1, max = 255)
        @Schema(description = "Наименование типа двигателя ТС", minLength = 1, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
        String engineType,
        @NotBlank(message = "Наименование Вида топлива ТС не может быть пустым")
        @Size(min = 1, max = 255)
        @Schema(description = "Наименование Вида топлива ТС", minLength = 1, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
        String fuelType,
        @Positive
        @Max(99999)
        @Schema(description = "Объем двигателя", requiredMode = Schema.RequiredMode.REQUIRED)
        int engineCapacity,
        @Positive
        @Schema(description = "Мощность, лс", requiredMode = Schema.RequiredMode.REQUIRED)
        @Digits(integer = 4, fraction = 2)
        BigDecimal enginePower,
        @Positive
        @Max(999999)
        @Schema(description = "Объем топливного бака", requiredMode = Schema.RequiredMode.REQUIRED)
        int fuelTankVolume,
        @NotBlank(message = "Наименование привода ТС не может быть пустым")
        @Size(min = 1, max = 255)
        @Schema(description = "Наименование привода ТС", minLength = 1, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
        String drive,
        @Schema(description = "Признак наличия держателя запасного колеса", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean spareWheelHolderInstalled,
        @Schema(description = "Признак наличия брызговиков", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean mudguardInstalled,
        @Schema(description = "Кузов", requiredMode = Schema.RequiredMode.REQUIRED)
        String bodyType,
        @Schema(description = "Трансмиссия", requiredMode = Schema.RequiredMode.REQUIRED)
        String transmissionType,
        @Schema(description = "Масса", requiredMode = Schema.RequiredMode.REQUIRED)
        int weight,
        @Schema(description = "Габариты ДxШxВ", requiredMode = Schema.RequiredMode.REQUIRED)
        String dimensions,
        @Schema(description = "Период производства/Поколение", requiredMode = Schema.RequiredMode.REQUIRED)
        String manufacturePeriod

) {
}
