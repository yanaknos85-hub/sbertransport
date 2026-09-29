package ru.sberbank.ditsib.transport.vehicle.dto.transport.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;

import java.time.LocalDateTime;

@Schema(description = "Документы")
public record DocumentsResponseDto(

        @Schema(description = "Номер ПТС", example = "АА33367896", maxLength = 15)
        String passportNumber,

        @Schema(description = "Дата выдачи ПТС")
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        LocalDateTime passportIssuedDate,

        @Schema(description = "Марка по ПТС", example = "Форд", maxLength = 150)
        String brandByPassport,

        @Schema(description = "Модель по ПТС", example = "Фокус", maxLength = 150)
        String modelByPassport,

        @Schema(description = "Свидетельство о регистрации", example = "ВА5566435", maxLength = 15)
        String certificateNumber,

        @Schema(description = "Дата выдачи СТС")
        @JsonSerialize(using = LocalDateTimeMillisConverter.class)
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        LocalDateTime certificateIssuedDate,

        @Schema(description = "Тип ТС", example = "Легковой", maxLength = 150)
        String vehicleType

) {
}
