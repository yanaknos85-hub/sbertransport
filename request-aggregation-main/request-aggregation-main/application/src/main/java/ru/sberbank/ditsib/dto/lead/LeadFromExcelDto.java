package ru.sberbank.ditsib.dto.lead;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.joda.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.joda.deser.LocalTimeDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.sberbank.ditsib.enumerate.TransportClass;
import ru.sberbank.ditsib.enumerate.TransportType;
import ru.sberbank.ditsib.enumerate.TripType;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(name = "LeadFromExcelDto", description = "Пользовательские заявки прогруженные из файла и провалидированные")
public record LeadFromExcelDto(
        @Schema(description = "Табельный номер сотрудника", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String personnelNumber,

        @NotBlank
        @Schema(description = "ФИО сотрудника", requiredMode = Schema.RequiredMode.REQUIRED)
        String fullName,

        @Schema(description = "Адрес посадки", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 255)
        @NotBlank
        @Size(max = 255)
        String addressFrom,

        @Schema(description = "Адрес посадки", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 255)
        @NotBlank
        @Size(max = 255)
        String addressTo,

        @NotNull
        @Schema(description = "Дата заказа", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        LocalDate orderDate,

        @NotNull
        @Schema(description = "Время заказа", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonDeserialize(using = LocalTimeDeserializer.class)
        LocalTime orderTime,

        @NotNull
        @Schema(description = "Тип транспорта", requiredMode = Schema.RequiredMode.REQUIRED)
        TransportType transportType,

        @NotNull
        @Schema(description = "Цель поездки", requiredMode = Schema.RequiredMode.REQUIRED)
        TripType tripType,

        @NotNull
        @Schema(description = "Вид транспорта", requiredMode = Schema.RequiredMode.REQUIRED)
        TransportClass transportClass,
        @Schema(name = "Широта адреса посадки", description = "Координаты из гео, полученные при валидации адреса")
        double addressFromLatitude,
        @Schema(name = "Долгота адреса посадки", description = "Координаты из гео, полученные при валидации адреса")
        double addressFromLongitude,
        @Schema(name = "Широта адреса высадки", description = "Координаты из гео, полученные при валидации адреса")
        double addressToLatitude,
        @Schema(name = "Долгота адреса высадки", description = "Координаты из гео, полученные при валидации адреса")
        double addressToLongitude
) {
}
