package ru.sberbank.ditsib.transport.vehicle.dto.vehicle;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;


/**
 * @author skakun-a
 */
@Schema(description = "DTO для создания или изменения справочной записи траспортного средства")
@Builder(toBuilder = true)
public record VehicleRequestDto(
        @NotNull(message = "Идентификатор модели ТС должен быть заполнен")
        @Schema(description = "Идентификатор модели ТС", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID modelId,
        @NotBlank(message = "Организация изготовитель (страна) не может быть пустым")
        @Size(min = 1, max = 50)
        @Schema(description = "Организация изготовитель (страна)", requiredMode = Schema.RequiredMode.REQUIRED)
        String manufacturer,
        @NotBlank(message = "Экологический класс не может быть пустым")
        @Size(min = 1, max = 1)
        @Pattern(regexp = "\\d")
        @Schema(description = "Экологический класс", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 1)
        String ecologicalClass,
        @Positive
        @Max(999999)
        @NotNull(message = "Мощность (лс) должен быть заполнен")
        @Schema(description = "Мощность, лс", requiredMode = Schema.RequiredMode.REQUIRED)
        @Digits(integer = 4, fraction = 2)
        BigDecimal enginePower,
        @Positive
        @Max(99999)
        @NotNull(message = "Объем двигателя должен быть заполнен")
        @Schema(description = "Объем двигателя", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer engineCapacity,
        @Positive
        @Max(999999)
        @NotNull(message = "Объем топливного бака должен быть заполнен")
        @Schema(description = "Объем топливного бака", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer fuelTankVolume,
        @NotEmpty(message = "Идентификаторы типа топлива ТС должны быть заполнены")
        @Schema(description = "Идентификаторы типа топлива ТС", requiredMode = Schema.RequiredMode.REQUIRED)
        Set<UUID> fuelTypeIds,
        @NotNull(message = "Идентификатор типа двигателя должен быть заполнен")
        @Schema(description = "Идентификатор типа двигателя", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID engineTypeId,

        @NotNull(message = "Идентификатор категории ТС должен быть заполнен")
        @Schema(description = "Идентификатор категории ТС", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID categoryId,
        @NotNull(message = "Идентификатор привода ТС должен быть заполнен")
        @Schema(description = "Идентификатор привода ТС", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID driveId,

        @NotNull(message = "Идентификатор типа кузова должен быть заполнен")
        @Schema(description = "Идентификатор типа кузова", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID bodyTypeId,

        @NotNull(message = "Идентификатор типа трансмиссии должен быть заполнен")
        @Schema(description = "Идентификатор типа трансмиссии", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID transmissionTypeId,

        @NotNull(message = "Размер переднего колеса должен быть заполнен")
        @Schema(description = "Идентификатор размера переднего колеса", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID frontWheelSizeId,

        @NotNull(message = "Размер заднего колеса должен быть заполнен")
        @Schema(description = "Идентификатор размера заднего колеса", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID rearWheelSizeId,

        @NotNull(message = "Признак наличия брызговиков должен быть заполнен")
        @Schema(description = "Признак наличия брызговиков", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean mudguardInstalled,
        @NotNull(message = "Признак наличия держателя запасного колеса должен быть заполнен")
        @Schema(description = "Признак наличия держателя запасного колеса", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean spareWheelHolderInstalled,
        @Positive
        @NotNull(message = "Масса без нагрузки (кг) должна быть заполнена")
        @Schema(description = "Масса без нагрузки (кг)", requiredMode = Schema.RequiredMode.REQUIRED)
        @Max(999999)
        Integer weight,
        @Positive
        @NotNull(message = "Разрешенная максимальная масса (кг) должна быть заполнена")
        @Schema(description = "Разрешенная максимальная масса (кг)", requiredMode = Schema.RequiredMode.REQUIRED)
        @Max(999999)
        Integer maxWeight,

        @Positive
        @NotNull(message = "Высота кузова полная (мм) должен быть заполнен")
        @Schema(description = "Высота кузова полная (мм)", requiredMode = Schema.RequiredMode.REQUIRED)
        @Max(99999)
        Integer height,
        @Positive
        @NotNull(message = "Ширина кузова полная (мм) должен быть заполнен")
        @Schema(description = "Ширина кузова полная (мм)", requiredMode = Schema.RequiredMode.REQUIRED)
        @Max(99999)
        Integer width,
        @Positive
        @NotNull(message = "Длина кузова полная (мм) должен быть заполнен")
        @Schema(description = "Длина кузова полная (мм)", requiredMode = Schema.RequiredMode.REQUIRED)
        @Max(99999)
        Integer length,
        @Positive
        @NotNull(message = "Межсервисный интервал по пробегу должен быть заполнен")
        @Schema(description = "Межсервисный интервал по пробегу", requiredMode = Schema.RequiredMode.REQUIRED)
        @Max(99999)
        Integer serviceIntervalMileage,
        @Positive
        @NotNull(message = "Межсервисный интервал по времени должен быть заполнен")
        @Schema(description = "Межсервисный интервал по времени", requiredMode = Schema.RequiredMode.REQUIRED)
        @Max(999)
        Integer serviceIntervalDays,
        @Positive
        @NotNull(message = "Допуск по времени должен быть заполнен")
        @Schema(description = "Допуск по времени", requiredMode = Schema.RequiredMode.REQUIRED)
        @Max(999)
        Integer serviceAuthorizationDays,
        @Positive
        @NotNull(message = "Допуск по пробегу должен быть заполнен")
        @Schema(description = "Допуск по пробегу", requiredMode = Schema.RequiredMode.REQUIRED)
        @Max(9999)
        Integer serviceAuthorizationMileage,
        @NotNull(message = "Год начала производства должен быть заполнен")
        @Schema(description = "Год начала производства", requiredMode = Schema.RequiredMode.REQUIRED)
        @Positive
        @Max(9999)
        @Min(1900)
        int yearManufactureBegin,
        @Schema(description = "Год снятия с производства")
        @Positive
        @Max(9999)
        @Min(1900)
        Integer yearManufactureEnd,
        @Schema(description = "Городской расход", example = "10.99", requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                minimum = "1", maximum = "999.99")
        @Min(1)
        @DecimalMax(value = "999.99")
        @Nullable
        BigDecimal cityConsumptionRate,
        @Schema(description = "Загородный расход", example = "10.99", requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                minimum = "1", maximum = "999.99")
        @Min(1)
        @DecimalMax(value = "999.99")
        @Nullable
        BigDecimal countryConsumptionRate,
        @Schema(description = "Смешанный расход", example = "10.99", requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                minimum = "1", maximum = "999.99")
        @Min(1)
        @DecimalMax(value = "999.99")
        @Nullable
        BigDecimal hybridConsumptionRate

) {
}
