package ru.sber.transport.magenta.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO с данными по публикуемому тарифу
 *
 * 	"tariffId": "1123",
 * 	"regionId": "",
 * 	"deptId": "10162311",
 * 	"deptStruct": "",
 * 	"deptName": "",
 * 	"vehicleType": "Такси",
 * 	"rideCostPerMin": 0,
 * 	"rideCostPerKm": 37,
 * 	"waitCostPerMin": 15,
 * 	"carServiceCost": 0,
 * 	"minRideCost": 400,
 * 	"maxCapacity": 3,
 * 	"savingsDeviationPct": 5,
 * 	"distanceDeviationKm": 99,
 * 	"timeDeviationMin": 43200,
 * 	"minCancelTimeMin": 600,
 * 	"historicalTraffic": false,
 * 	"tollRoads": false,
 * 	"timeZone": ""
 */
@Data
@NoArgsConstructor
@Schema(title = "Данные по тарифу для Magenta", description = "Данные по тарифу")
public class MagentaOrgTariffDTO {
    
    public final List<Tariff> tariffs = new ArrayList<>();
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Tariff {
        // ID тарифа
        private String tariffId;
    
        // ID региона
        private String regionId;
    
        //Идентификатор подразделения
        private String deptId = "deptId";
    
        //Полный стуктурный путь до подразделения. Например ПАО Сбербанк/СЗ Банк/Департамент розничного бизнеса
        private String deptStruct;
    
        //Название подразделения
        private String deptName;
    
        //Произвольное значение
        private String vehicleType = "vehicle";
    
        //Стоимость за минуту
        private Double rideCostPerMin;
    
        //Стоимость за километр
        private Double rideCostPerKm;
    
        //Стоимость за минуту ожидания
        private Double waitCostPerMin;
    
        //Стоимость подачи
        private Double carServiceCost = 0d;
    
        //Минимальная стоимость поездки
        private Double minRideCost = 0d;
    
        //Максимальное число пассажирских мест
        private Integer maxCapacity = 3;
    
        //Предельно-допустимое отклонение по минимальной экономии
        private Double savingsDeviationPct;
    
        //Предельно-допустимое отклонение по километражу
        private Double distanceDeviationKm;
    
        //Предельно-допустимое отклонение по времени в минутах
        private Integer timeDeviationMin;
    
        //Триггерное время (За какое время до поездки ее можно отменить)
        private Integer minCancelTimeMin = 45;
    
        //Учёт исторических пробок
        private Boolean historicalTraffic = true;
    
        //Разрешить проезд по платным дорогам
        private Boolean tollRoads = false;
    
        // Временная зона
        private String timeZone;
    }
}
