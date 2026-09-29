package ru.sberbank.ditsib.transport.vehicle.dto.transport.request.create;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.With;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;

import java.time.LocalDateTime;

@Schema(description = "Документы")
@With
public record DocumentsCreateDto(

        @Schema(description = "Номер ПТС", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 15)
        @NotBlank(message = "Номер ПТС должен быть задан")
        @Size(min = 1, max = 15)
        String passportNumber,

        @Schema(description = "Дата выдачи ПТС", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Дата выдачи ПТС должен быть задан")
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        LocalDateTime passportIssuedDate,

        @Schema(description = "Марка по ПТС", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 150)
        @NotBlank(message = "Марка по ПТС должна быть задана")
        @Size(min = 1, max = 150)
        String brandByPassport,

        @Schema(description = "Модель по ПТС", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 150)
        @NotBlank(message = "Модель по ПТС должна быть задана")
        @Size(min = 1, max = 150)
        String modelByPassport,

        @Schema(description = "Свидетельство о регистрации (СТС)", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 15)
        @NotBlank(message = "Свидетельство о регистрации (СТС) должно быть задано")
        @Size(min = 1, max = 15)
        String certificateNumber,

        @Schema(description = "Дата выдачи СТС", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Дата выдачи СТС должен быть задан")
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        LocalDateTime certificateIssuedDate,

        @Schema(description = "Тип ТС", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Тип ТС должен быть заполнен")
        @Size(max = 150)
        @Pattern(regexp = "[^a-zA-Z]*", message = "Тип ТС не должен содержать английские буквы")
        String vehicleType

) {
}
