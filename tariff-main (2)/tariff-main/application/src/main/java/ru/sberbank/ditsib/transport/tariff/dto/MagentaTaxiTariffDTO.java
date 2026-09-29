package ru.sberbank.ditsib.transport.tariff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO с данными по публикуемому тарифу такси
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@Schema(title = "Данные по тарифу такси для Magenta", description = "Данные по тарифу")
public class MagentaTaxiTariffDTO {
    
    
    /**
     * ID тарифа из системы Банка
     */
    @NotNull
    @Schema(description = "Копия идентификатора", requiredMode = Schema.RequiredMode.REQUIRED)
    private final String tariffId;
    
    /**
     * Transport type ID.
     */
    @NotNull
    @Min(0)
    @Schema(description = "Цена за КМ", requiredMode = Schema.RequiredMode.REQUIRED)
    private final Double rideCostPerKm;
    
    
    //Идентификатор подразделения
    @Builder.Default
    @Schema(description = "Идентификатор подразделения", requiredMode = Schema.RequiredMode.REQUIRED)
    private final String deptId = "deptId";
    
    //Произвольное значение
    @Builder.Default
    @Schema(description = "Тип машины", requiredMode = Schema.RequiredMode.REQUIRED)
    private final String vehicleType = "vehicle";
    
    //Триггерное время (За какое время до поездки ее можно отменить)
    @Builder.Default
    @Schema(description = "Триггерное время (За какое время до поездки ее можно отменить)", required = false)
    private final Integer minCancelTimeMin = 45;
    
    
    //Полный стуктурный путь до подразделения. Например ПАО Сбербанк/СЗ Банк/Департамент розничного бизнеса
    @Schema(description = "Полный стуктурный путь до подразделения. Например ПАО Сбербанк/СЗ Банк/Департамент розничного бизнеса",
            required = false)
    private final String deptStruct;
    
    //Название подразделения
    @Schema(description = "Название подразделения", required = false)
    private final String deptName;
    
    //Стоимость за минуту
    @Min(0)
    @Schema(description = "Стоимость за минуту", requiredMode = Schema.RequiredMode.REQUIRED)
    private final Double rideCostPerMin;
    
    //Стоимость за минуту ожидания
    @Min(0)
    @NotNull
    @Schema(description = "Стоимость минуты ожидания", requiredMode = Schema.RequiredMode.REQUIRED)
    private final Double waitCostPerMin;
    
    //Стоимость подачи
    @Min(0)
    @NotNull
    @Builder.Default
    @Schema(description = "Стоимость подачи", requiredMode = Schema.RequiredMode.REQUIRED)
    private final Double carServiceCost = 0d;
    
    //Минимальная стоимость поездки
    @Min(0)
    @NotNull
    @Builder.Default
    @Schema(description = "Минимальная стоимость поездки", required = false)
    private final Double minRideCost = 0d;
    
    //Предельно-допустимое отклонение по минимальной экономии
    @Min(0)
    @Max(100)
    @Schema(description = "Предельно-допустимое отклонение по минимальной экономии, процентов", required = false)
    private final Double savingsDeviationPct;
    
    //Предельно-допустимое отклонение по километражу
    @Min(0)
    @Schema(description = "Предельно-допустимое отклонение по километражу", required = false)
    private final Double distanceDeviationKm;
    
    //Предельно-допустимое отклонение по времени в минутах
    @Min(0)
    @Schema(description = "Отклонение по времени прибытия во вторую точку в минутах", required = false)
    private final Integer timeDeviationMin;
    
    //Максимальное число пассажирских мест
    @Builder.Default
    @Schema(description = "Максимальное число пассажирских мест", required = false)
    private final Integer maxCapacity = 4;
    
    //Разрешить проезд по платным дорогам
    @Builder.Default
    @Schema(description = "Разрешить проезд по платным дорогам)", required = false)
    private final Boolean tollRoads = false;
    
    //Учёт исторических пробок
    @Builder.Default
    @Schema(description = "Учёт исторических пробок", required = false)
    private final Boolean historicalTraffic = true;

    @Builder.Default
    @Schema(description = "Клиент")
    private final String senderService = "TRANSPORT_AS";

    @Builder.Default
    @Schema(description = "Тип сервиса")
    private final String senderTransportType = "TRANSPORT_TAXI";

    @Schema(description = "Стоимость минимальной поездки с включенным расстоянием, коп", required = false)
    private final Double minRideCostKm;

    @Schema(description = "Стоимость минимальной поездки с включенным временем, коп", required = false)
    private final Double minRideCostMin;

    @Schema(description = "Коэффициент временного интервала поездки: утро будние дни 07:00-10:00")
    @Builder.Default
    private final Double rateWeekdayMorning = 1d;

    @Schema(description = "Коэффициент временного интервала поездки: день будние дни 10:00-18:00")
    @Builder.Default
    private final Double rateWeekdayDay = 1d;

    @Schema(description = "Коэффициент временного интервала поездки: день будние дни 18:00-22:00")
    @Builder.Default
    private final Double rateWeekdayEvening = 1d;

    @Schema(description = "Коэффициент временного интервала поездки: день будние дни 22:00-07:00")
    @Builder.Default
    private final Double rateWeekdayNight = 1d;

    @Schema(description = "Коэффициент выходного дня: СБ")
    @Builder.Default
    private final Double rateWeekendSaturday = 1d;

    @Schema(description = "Коэффициент выходного дня: ВСКР")
    @Builder.Default
    private final Double rateWeekendSunday = 1d;

}
