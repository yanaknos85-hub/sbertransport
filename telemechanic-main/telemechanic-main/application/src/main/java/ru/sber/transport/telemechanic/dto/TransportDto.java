package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.With;

import java.util.UUID;

/**
 * Транспортное средство
 *
 * @param stateNumber Автомобильный номер
 * @param brand       Марка
 * @param model       Модель
 */
@With
@Schema(title = "Транспортное средство", description = "Информация о транспортном средстве")
public record TransportDto(
        @NotNull
        @Schema(description = "Идентификатор автомобиля")
        UUID id,
        @NotBlank(message = "Номер должен быть задан")
        @Pattern(regexp = "^[АВЕКМНОРСТУХ]\\d{3}(?<!000)[АВЕКМНОРСТУХ]{2}\\d{2,3}$",
                message = "Регистрационный знак не прошел проверку")
        @Schema(description = "Автомобильный номер",
                pattern = "^[АВЕКМНОРСТУХ]\\d{3}(?<!000)[АВЕКМНОРСТУХ]{2}\\d{2,3}$",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String stateNumber,
        @NotBlank(message = "Марка должна быть задана")
        @Schema(description = "Марка", requiredMode = Schema.RequiredMode.REQUIRED)
        @Size(max = 50)
        String brand,
        @NotBlank(message = "Модель должна быть задана")
        @Schema(description = "Модель", requiredMode = Schema.RequiredMode.REQUIRED)
        @Size(max = 50)
        String model,
        @Schema(description = "Тип транспорта")
        String transportType,
        @Schema(description = "Показания одометра при выезде")
        Integer odometerOut,
        @Schema(description = "Показания одометра при заезде")
        Integer odometerIn
) {
}
