package ru.sberbank.ditsib.dto.lead;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sberbank.ditsib.dto.file.FieldWithValidation;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO, содержащий результаты валидации одной строки Excel-файла загрузки лидов.
 * Каждое поле содержит значение и флаг наличия ошибки.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidatedLeadDto {

    @Schema(description = "Табельный номер сотрудника")
    private FieldWithValidation<String> personnelNumber;
    @Schema(description = "ФИО сотрудника")
    private FieldWithValidation<String> fullName;
    @Schema(description = "Адрес посадки")
    private FieldWithValidation<String> addressFrom;
    @Schema(description = "Адрес высадки")
    private FieldWithValidation<String> addressTo;
    @Schema(description = "Дата заказа")
    private FieldWithValidation<LocalDate> orderDate;
    @Schema(description = "Время заказа")
    private FieldWithValidation<LocalTime> orderTime;
    @Schema(description = "Вид транспорта")
    private FieldWithValidation<String> transportType;
    @Schema(description = "Тип транспорта")
    private FieldWithValidation<String> transportClass;
    @Schema(description = "Цель поездки")
    private FieldWithValidation<String> tripType;
    @Schema(name = "Широта адреса посадки",
            description = "Не отображается на превью, заполняется только если адрес корректен")
    private Double addressFromLatitude;
    @Schema(name = "Долгота адреса посадки",
            description = "Не отображается на превью, заполняется только если адрес корректен")
    private Double addressFromLongitude;
    @Schema(name = "Широта адреса высадки",
            description = "Не отображается на превью, заполняется только если адрес корректен")
    private Double addressToLatitude;
    @Schema(name = "Долгота адреса высадки",
            description = "Не отображается на превью, заполняется только если адрес корректен")
    private Double addressToLongitude;
}
