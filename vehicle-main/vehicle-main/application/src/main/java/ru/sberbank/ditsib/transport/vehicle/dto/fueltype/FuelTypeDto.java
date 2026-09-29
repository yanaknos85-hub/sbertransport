package ru.sberbank.ditsib.transport.vehicle.dto.fueltype;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.sberbank.ditsib.transport.vehicle.dto.enginetype.EngineTypeDto;

import java.util.List;
import java.util.UUID;

/**
 * @author skakun-a
 */
@Schema(title = "Вид топлива ТС", description = "Вид топлива траспортного средства")
public record FuelTypeDto(

        @NotNull
        @Schema(description = "ID Вида топлива ТС", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,
        @NotBlank(message = "Наименование Вида топлива ТС не может быть пустым")
        @Size(min = 1, max = 255)
        @Schema(description = "Наименование Вида топлива ТС", minLength = 1, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
        String title,
        @Schema(description = "Наименование топлива от контрагента (для взаиморасчётов)",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        List<String> possibleTitles,
        @NotNull(message = "Вид топлива ТС должен относится к какому-либо Типу двигателя ТС")
        @Schema(description = "Тип двигателя ТС", minLength = 1, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
        EngineTypeDto engineType

) {
}