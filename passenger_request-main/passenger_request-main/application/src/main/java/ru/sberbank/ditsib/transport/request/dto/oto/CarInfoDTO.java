package ru.sberbank.ditsib.transport.request.dto.oto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

/**
 * Данные по транспортному средству
 */
@Schema(title = "Данные по транспортному средству", description = "Данные по транспортному средству")
@Value
@Builder
public class CarInfoDTO {

    /**
     * Марка тс
     */
    @Schema(description = "Марка", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 128)
    @Size(max = 128)
    String brandName;

    /**
     * Модель тс
     */
    @Schema(description = "Модель", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 128)
    @Size(max = 128)
    String model;

    /**
     * Цвет тс
     */
    @Schema(description = "Цвет", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 128)
    @Size(max = 128)
    String color;

    /**
     * Государственный номер
     */
    @Schema(description = "Гос. Номер", requiredMode = Schema.RequiredMode.REQUIRED, pattern = "\\w\\d{3}\\w{2} \\d{3} \\w{3}")
    @Pattern(regexp = "\\w\\d{3}\\w{2} \\d{3} \\w{3}")
    String registrationNumber;
}
