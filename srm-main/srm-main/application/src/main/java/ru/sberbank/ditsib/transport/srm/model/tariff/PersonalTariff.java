package ru.sberbank.ditsib.transport.srm.model.tariff;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.validation.interval.Interval;

import java.time.LocalDate;

/**
 * Сущность тарифа на компенсацию личного транспорта
 */
@ToString
@Getter
@Setter
@Entity
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Interval(startField = "seasonStart", endField = "seasonEnd")
@DiscriminatorValue(value = TransportTypeEnum.Constants.PERSONAL_STRING)
public class PersonalTariff extends BaseTariff {
    
    //Цена за км, коп
    @NotNull
    @Min(0)
    @Max(1000_00)
    @Column(name = "ride_cost_per_km")
    private Integer rideCostPerKm;
    
    //Стоимость за минуту, коп
    @NotNull
    @Min(0)
    @Max(1000_00)
    @Column(name = "ride_cost_per_min")
    private Integer rideCostPerMin;
    
    //Бесплатных минут пути, включенных в тариф
    @Min(0)
    @Max(60)
    @Builder.Default
    @Column(name = "minutes_included")
    private Integer timeIncluded = 0;
    
    //Бесплатных километров пути, включенных в тариф
    @Min(0)
    @Max(100)
    @Builder.Default
    @Column(name = "distance_included")
    private Double distanceIncluded = 0d;
    
    //Стоимость минимальной поездки с включенным расстоянием, коп
    @Min(0)
    @Max(1000_00)
    @Builder.Default
    @Column(name = "min_ride_distance_cost")
    private Integer minRideDistanceCost = 0;
    
    //Стоимость минимальной поездки с включенным временем, коп
    @Min(0)
    @Max(1000_00)
    @Builder.Default
    @Column(name = "min_ride_time_cost")
    private Integer minRideTimeCost = 0;
    
    //Стоимость за минуту ожидания
    @Min(0)
    @Max(1000_00)
    @Builder.Default
    @Column(name = "wait_cost_per_min")
    private Integer waitCostPerMin = 0;

    /**
     * Сезонный коэффициент
     */
    @Positive
    @Builder.Default
    @Column(name = "seasonal_coefficient", nullable = false)
    private Double seasonalCoefficient = 1d;
    
    /**
     * Дата начала действия тарифа
     */
    @Builder.Default
    @Column(name = "season_start", nullable = false)
    private LocalDate seasonStart = LocalDate.parse("2000-01-01");
    
    /**
     * Дата завершения действия тарифа
     */
    @Builder.Default
    @Column(name = "season_end", nullable = false)
    private LocalDate seasonEnd = LocalDate.parse("2000-12-31");
    
    //Коэффициент загрузки дорог (пробок, баллы Яндекс, прогнозные) Более N баллов(настраиваемый параметр)
    // (каждый балл больше N увеличивает на x%)
    @Min(0)
    @Max(10)
    @Builder.Default
    @Column(name = "coef_traffic")
    private Double coefTraffic = 0d;
    
    //K перевозки ТМЦ
    @Min(1)
    @Max(10)
    @Builder.Default
    @Column(name = "coef_material_assets")
    private Double coefMaterialAssets = 1d;
    
    //Временные коэффициенты
    @Embedded
    @Builder.Default
    private TimedTariffParams timedTariffParams = new TimedTariffParams();
    
    //Параметры для подбора совместной поездки
    @Embedded
    @Builder.Default
    private CoopTariffParams coopTariffParams = new CoopTariffParams();
    
    //Коэффициенты за объем двигателя-
    @Embedded
    @Builder.Default
    private EngineTariffParams engineTariffParams = new EngineTariffParams();
    
    //Параметры тарифа за чертой города
    //@Embedded
    //@Builder.Default
    //private final SuburbTariffParams suburbTariffParams = new SuburbTariffParams();
    
    // максимальное количество пассажиров
    @Column(name = "max_capacity")
    private Integer maxCapacity;
}
