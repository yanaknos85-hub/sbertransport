package ru.sberbank.ditsib.transport.vehicle.dto.fueltype;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;


/**
 * @author skakun-a
 */
@Schema(description = "DTO для создания или изменения Вида топлива ТС")
public record FuelTypeRequestDto(
        @NotBlank(message = "Наименование Вида топлива ТС не может быть пустым")
        @Size(min = 1, max = 255)
        @Schema(description = "Наименование Вида топлива ТС",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 1, maxLength = 255,
                example = "АИ-95")
        String title,

        @NotNull(message = "Идентификатор Типа двигателя ТС должен быть задан")
        @Schema(description = "Идентификатор Типа двигателя ТС", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID engineTypeId,

        @Schema(description = "Наименование топлива от контрагента (для взаиморасчётов)",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "[\"АИ-95\",\"Бензин АИ-95\"]")
        List<String> possibleTitles
) {
}
