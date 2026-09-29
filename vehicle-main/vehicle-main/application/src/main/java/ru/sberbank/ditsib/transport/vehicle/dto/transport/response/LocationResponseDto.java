package ru.sberbank.ditsib.transport.vehicle.dto.transport.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;

import java.time.LocalDateTime;

@Schema(description = "Данные о местоположении транспорта")
public record LocationResponseDto(
        @Schema(description = "Дата начала эксплуатации")
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        LocalDateTime exploitationStart,
        @Schema(description = "Дата окончания эксплуатации")
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        LocalDateTime exploitationEnd,
        @Schema(description = "Адрес места базирования",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "г. Москва, ул. Мясницкая, д. 12",
                maximum = "255")
        @Size(max = 255, message = "Адрес места базирования не должен превышать 255 символов")
        @NotBlank(message = "Адрес места базирования должен быть задан")
        String locationAddress,
        @Schema(description = "Адрес стоянки автомобиля",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "г. Москва, ул. Мясницкая, д. 12",
                maximum = "255"
        )
        @Size(max = 255, message = "Адрес стоянки автомобиля не должен превышать 255 символов")
        @NotBlank(message = "Адрес стоянки автомобиля должен быть задан")
        String parkingAddress
) {
}
