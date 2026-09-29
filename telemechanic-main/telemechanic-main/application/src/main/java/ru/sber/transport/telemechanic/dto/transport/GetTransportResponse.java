package ru.sber.transport.telemechanic.dto.transport;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(name = "GetTransportResponse", title = "Ответ на запрос поиска транспорта",
        description = "Ответ на запрос поиска транспорт по гос. номеру")
public record GetTransportResponse(
        
        @NotNull(message = "Идентификатор транспорта не может быть null")
        @Schema(description = "Идентификатор транспорта",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,
        
        @NotBlank(message = "Государственный номер не может быть пустым")
        @Size(max = 50, message = "Государственный номер не должен быть больше 50 символов")
        @Pattern(regexp = "^[АВЕКМНОРСТУХ]\\d{3}(?<!000)[АВЕКМНОРСТУХ]{2}\\d{2,3}$",
                 message = "Государственный номер не прошел проверку")
        @Schema(description = "Государственный номер",
                requiredMode = Schema.RequiredMode.REQUIRED,
                pattern = "^[АВЕКМНОРСТУХ]\\d{3}(?<!000)[АВЕКМНОРСТУХ]{2}\\d{2,3}$",
                example = "А777АА77",
                maximum = "50")
        String stateNumber,
        
        @NotBlank(message = "Марка не может быть пустой")
        @Size(max = 255, message = "Марка не должна быть больше 255 символов")
        @Schema(description = "Марка",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "Toyota",
                maximum = "255")
        String brand,
        
        @NotBlank(message = "Модель не может быть пустой")
        @Size(max = 255, message = "Модель не должна быть больше 255 символов")
        @Schema(description = "Модель",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "Camry",
                maximum = "255")
        String model,
        
        @NotBlank(message = "Тип транспорта не может быть пустой")
        @Size(max = 255, message = "Тип транспорта не должен быть больше 255 символов")
        @Schema(description = "Тип транспорта",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "Служебный",
                maximum = "255")
        String transportType
) {
}
