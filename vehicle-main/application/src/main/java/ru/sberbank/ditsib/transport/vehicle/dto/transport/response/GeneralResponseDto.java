package ru.sberbank.ditsib.transport.vehicle.dto.transport.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Общие характеристики")
public record GeneralResponseDto(
        @Schema(description = "Высота кузова полная, мм", example = "2557", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 5)
        Integer height,
        @Schema(description = "Ширина кузова полная, мм", example = "2070", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 5)
        Integer width,
        @Schema(description = "Длина кузова полная, мм", example = "6848", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 5)
        Integer length,
        @Schema(description = "Масса без нагрузки, кг", example = "1750", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 6)
        Integer weight,
        @Schema(description = "Разрешенная максимальная масса, кг", example = "2100", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 6)
        Integer maxWeight,
        @Schema(description = "Цвет кузова", example = "Коричневый", maxLength = 50)
        String bodyColor,
        @Schema(description = "Телематика", example = "САНТЕЛ НАВИГАЦИЯ", maxLength = 50)
        String telematics,
        @Schema(description = "ИД телематики", example = "8B111167-5F9C-4E37-8705-1C8E4C6A06D6", maxLength = 50)
        UUID telematicsId,
        @Schema(description = "Категория ТС", example = "B", maxLength = 5)
        String category,
        @Schema(description = "Наименование ТС", example = "Легковые автомобили", maxLength = 255)
        String categoryName,
        @Schema(description = "Объем топливного бака", example = "2500", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 6)
        Integer fuelTankVolume,
        @Schema(description = "Держатель запасного колеса", example = "true")
        boolean spareWheelHolderInstalled,
        @Schema(description = "Наличие брызговиков", example = "true")
        boolean mudguardInstalled,
        @Schema(description = "Размер передних колес", example = "235/40R18")
        String frontWheelSize,
        @Schema(description = "Размер задних колес", example = "235/40R18")
        String rearWheelSize
) {
}
