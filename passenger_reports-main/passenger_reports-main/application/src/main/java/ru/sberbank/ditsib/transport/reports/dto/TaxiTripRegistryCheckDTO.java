package ru.sberbank.ditsib.transport.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TaxiTripRegistryCheckDTO {
  
    @Schema(description = "Проверка 0 - Проверка совпадения ID поездки")
    private Boolean validTaxiId0;
    
    @Schema(description = "Проверка 1 - Проверка статуса поездки")
    private Boolean validTripStatus1;
    
    @Schema(description = "Проверка 2 - Проверка даты поездки")
    private Boolean validTripDate2;
    
    @Schema(description = "Проверка 3 - Проверка стоимость = 0 для отмененной поездки")
    private Boolean validCancelledTripCost3;
    
    @Schema(description = "Проверка 4 - Сравнение протяженности маршрута с расчетной")
    private Boolean validCalcDistance4;
    
    @Schema(description = "Проверка 4a - Сравнение протяженности маршрута с фактической")
    private Boolean validFactDistance4a;
    
    @Schema(description = "Проверка 5 - Проверка тарифа")
    private Boolean validTariff5;
    
    @Schema(description = "Проверка 6 - Сравнение стоимости поездки с расчетной")
    private Boolean validCalcCost6;
    
    @Schema(description = "Проверка 7 - Сравнение стоимости поездки с фактической")
    private Boolean validFactCost7;
    
    @Schema(description = "Проверка 8 - Проверка времени ожидания")
    private Boolean validWaitTime8;
}
