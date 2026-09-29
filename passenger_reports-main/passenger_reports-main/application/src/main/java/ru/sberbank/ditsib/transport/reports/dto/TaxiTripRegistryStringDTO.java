package ru.sberbank.ditsib.transport.reports.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.sberbank.ditsib.transport.constants.TripType;
import ru.sberbank.ditsib.transport.reports.model.excel.CalculatedTripStatus;

import java.util.UUID;

@Getter
@Setter
@JsonPropertyOrder({ "id" })
@Schema(
        title = "Строка реестра поездок на такси от контрагента за месяц",
        description = "Строка реестра поездок на такси от контрагента за месяц. " +
                      "Содержит данные импортрованной строки результаты проверок"
)
@Builder
public class TaxiTripRegistryStringDTO {
    
    /** ID строки */
    @Schema(description = "ID строки")
    private UUID id;
    
    /** Вычисленный при проверке реестра статус поездки */
    @Schema(description = "Вычисленный при проверке реестра статус поездки")
    private CalculatedTripStatus calculatedTripStatus;
    
    /** Вычисленный при проверке реестра тип поездки */
    @Schema(description = "Вычисленный при проверке реестра тип поездки")
    private TripType calculatedTripType;
    
    /** Проверка 0 - Проверка совпадения ID поездки */
    @Schema(description = "Проверка 0 - Проверка совпадения ID поездки")
    private Boolean validTaxiId0;
    
    /** Проверка 1 - Проверка статуса поездки */
    @Schema(description = "Проверка 1 - Проверка статуса поездки")
    private Boolean validTripStatus1;
    
    /** Проверка 2 - Проверка даты поездки */
    @Schema(description = "Проверка 2 - Проверка даты поездки")
    private Boolean validTripDate2;
    
    /** Проверка 3 - Проверка стоимость = 0 для отмененной поездки */
    @Schema(description = "Проверка 3 - Проверка стоимость = 0 для отмененной поездки")
    private Boolean validCancelledTripCost3;
    
    /** Проверка 4 - Сравнение протяженности маршрута с расчетной */
    @Schema(description = "Проверка 4 - Сравнение протяженности маршрута с расчетной")
    private Boolean validCalcDistance4;
    
    /** Проверка 4a - Сравнение протяженности маршрута с фактической */
    @Schema(description = "Проверка 4a - Сравнение протяженности маршрута с фактической")
    private Boolean validFactDistance4a;
    
    /** Проверка 5 - Проверка тарифа */
    @Schema(description = "Проверка 5 - Проверка тарифа")
    private Boolean validTariff5;
    
    /** Проверка 6 - Сравнение стоимости поездки с расчетной */
    @Schema(description = "Проверка 6 - Сравнение стоимости поездки с расчетной")
    private Boolean validCalcCost6;
    
    /** Проверка 7 - Сравнение стоимости поездки с фактической */
    @Schema(description = "Проверка 7 - Сравнение стоимости поездки с фактической")
    private Boolean validFactCost7;
    
    /** Проверка 8 - Проверка времени ожидания */
    @Schema(description = "Проверка 8 - Проверка времени ожидания")
    private Boolean validWaitTime8;
}
