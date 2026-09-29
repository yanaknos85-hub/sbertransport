package ru.sberbank.ditsib.transport.vehicle.dto.transport.request.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.With;

import java.util.UUID;

@Schema(description = "Автомобиль")
@With
@Builder
public record VehicleCreateDto(
        @Schema(description = "ID справочник Автомобиль", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "ID справочник Автомобиль должен быть задан")
        UUID id,
        @Schema(description = "Государственный номер",
                pattern = "^[АВЕКМНОРСТУХ]\\d{3}(?<!000)[АВЕКМНОРСТУХ]{2}\\d{2,3}$",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Государственный номер должен быть задан")
        @Pattern(regexp = "^[АВЕКМНОРСТУХ]\\d{3}(?<!000)[АВЕКМНОРСТУХ]{2}\\d{2,3}$",
                message = "Регистрационный знак не прошел проверку")
        String stateNumber,
        @Schema(description = "Год выпуска", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Год выпуска должен быть задан")
        @Min(value = 1900, message = "Год выпуска должен быть позже 1900-го года")
        @Max(value = 9999, message = "Год выпуска должен быть не позже 9999-го года")
        Integer year,
        @Schema(description = "Текущий пробег", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Текущий пробег должен быть задан")
        @PositiveOrZero(message = "Текущий пробег должен быть положительным числом, либо 0")
        @Max(value = 999999, message = "Текущий пробег должен быть не больше 999999")
        Integer currentMileage,
        @Schema(description = "VIN-номер", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 17)
        @NotBlank(message = "VIN-номер должен быть задан")
        @Size(max = 17, message = "VIN-номер не может быть больше 17 символов")
        String vinCode,
        @Schema(description = "Номер основного средства", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 50)
        @NotBlank(message = "Номер основного средства должен быть задан")
        @Size(max = 50, message = "Номер основного средства не может быть больше 50 символов")
        String assetNumber,
        @NotBlank(message = "Инвентарный номер должен быть задан")
        @Schema(description = "Инвентарный номер", maxLength = 50)
        @Size(max = 50, message = "Инвентарный номер не может быть больше 50 символов")
        String inventoryNumber,
        @Schema(description = "Номер кузова", maxLength = 17)
        @Size(max = 17, message = "Номер кузова не может быть больше 17 символов")
        String bodyNumber,
        @Schema(description = "Номер шасси", maxLength = 17)
        @Size(max = 17, message = "Номер шасси не может быть больше 17 символов")
        String chassisNumber,
        @Schema(description = "ID подвид", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "ID подвида должен быть задан")
        UUID subtypeId,
        @Schema(description = "Телематика")
        UUID telematicsId,
        @Schema(description = "Цвет кузова", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 50)
        @NotBlank(message = "Цвет кузова должен быть задан")
        @Size(max = 50, message = "Цвет кузова не может быть больше 50 символов")
        String bodyColor
) {
}
