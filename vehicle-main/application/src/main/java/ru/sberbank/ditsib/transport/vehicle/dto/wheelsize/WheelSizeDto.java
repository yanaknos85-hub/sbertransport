package ru.sberbank.ditsib.transport.vehicle.dto.wheelsize;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Schema(title = "Размер колеса ТС", description = "Размер колеса транспортного средства")
public record WheelSizeDto(

        @NotNull
        @Schema(description = "Идентификатор размера колеса ТС", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,

        @NotBlank(message = "Наименование размера колеса ТС должен быть заполнен")
        @Size(min = 1, max = 50)
        @Schema(description = "Наименование размера колеса ТС", minLength = 1, maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
        String title

) {
}