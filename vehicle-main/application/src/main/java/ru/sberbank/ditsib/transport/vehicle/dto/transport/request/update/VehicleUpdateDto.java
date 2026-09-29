package ru.sberbank.ditsib.transport.vehicle.dto.transport.request.update;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.With;

import java.util.UUID;

@Schema(description = "Автомобиль")
@With
public record VehicleUpdateDto(
        @Schema(description = "ID справочник Автомобиль", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "ID справочник Автомобиль должен быть задан")
        UUID id,
        @Schema(description = "Государственный номер",
                pattern = "^[АВЕКМНОРСТУХ]\\d{3}(?<!000)[АВЕКМНОРСТУХ]{2}\\d{2,3}$",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Номер должен быть задан")
        @Pattern(regexp = "^[АВЕКМНОРСТУХ]\\d{3}(?<!000)[АВЕКМНОРСТУХ]{2}\\d{2,3}$",
                message = "Регистрационный знак не прошел проверку")
        @NotNull(message = "Государственный номер должен быть задан")
        String stateNumber,
        @Schema(description = "VIN-номер", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 17)
        @NotBlank(message = "VIN-номер должен быть задан")
        @Size(max = 17)
        String vinCode,
        @Schema(description = "Номер основного средства", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 50)
        @NotBlank(message = "Номер основного средства должен быть задан")
        @Size(max = 50)
        String assetNumber,
        @Schema(description = "Инвентарный номер", maxLength = 50)
        @Size(max = 50)
        String inventoryNumber,
        @Schema(description = "Номер кузова", maxLength = 17)
        @Size(max = 17)
        String bodyNumber,
        @Schema(description = "Номер шасси", maxLength = 17)
        @Size(max = 17)
        String chassisNumber,
        @Schema(description = "Телематика")
        UUID telematicsId,
        @Schema(description = "Текущий пробег", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Текущий пробег должен быть задан")
        @Min(value = 1, message = "Текущий пробег должен быть больше 0")
        @Max(value = 999_999, message = "Максимальное значение пробега — 999999")
        int currentMileage,
        @Schema(description = "Идентификатор подвида ТС", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Идентификатор подвида ТС должен быть задан")
        UUID subtypeId
) {
}
