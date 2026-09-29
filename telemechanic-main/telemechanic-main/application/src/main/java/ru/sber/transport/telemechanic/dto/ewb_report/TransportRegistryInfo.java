package ru.sber.transport.telemechanic.dto.ewb_report;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "TransportRegistryInfo", title = "Данные для реестра по транспортному средству",
        description = "Данные по транспортному средству для ответа формирования реестар ЭПЛ")
public record TransportRegistryInfo(
        @NotBlank(message = "Государственный номер не должен быть пустым")
        @Pattern(regexp = "^[АВЕКМНОРСТУХ]\\d{3}(?<!000)[АВЕКМНОРСТУХ]{2}\\d{2,3}$", message = "Государственный номер не прошел проверку")
        @Schema(description = "Государственный номер",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "А777АА777",
                pattern = "^[АВЕКМНОРСТУХ]\\d{3}(?<!000)[АВЕКМНОРСТУХ]{2}\\d{2,3}$")
        String stateNumber,
        
        @Size(max = 255, message = "Не более 255 символов")
        @Schema(description = "Марка",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "Mazda",
                nullable = true,
                maximum = "255")
        String brand,
        
        @Size(max = 255, message = "Не более 255 символов")
        @Schema(description = "Модель",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "6",
                nullable = true,
                maximum = "255")
        String model,
        
        @Size(max = 255, message = "Не более 255 символов")
        @Schema(description = "Тип ТС",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "Служебный",
                nullable = true,
                maximum = "255")
        String type,
        
        @Size(max = 255, message = "Не более 255 символов")
        @Schema(description = "Подтип ТС",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                example = "СТС",
                nullable = true,
                maximum = "255")
        String subtype
) {
}
