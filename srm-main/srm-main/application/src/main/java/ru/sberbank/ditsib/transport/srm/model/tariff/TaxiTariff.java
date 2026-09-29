package ru.sberbank.ditsib.transport.srm.model.tariff;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

/**
 * Сущность тарифа такси
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@DiscriminatorValue(value = TransportTypeEnum.Constants.TAXI_STRING)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TaxiTariff extends BaseTariff {
    //Класс такси
    @Column(name = "taxi_class")
    @Enumerated(EnumType.STRING)
    private TaxiClass taxiClass;
    
    //Цена за км, коп
    @Column(name = "ride_cost_per_km")
    private Integer rideCostPerKm;
    
    //Бесплатных километров пути, включенных в тариф
    @Builder.Default
    @Column(name = "distance_included")
    private Double distanceIncluded = 0d;
    
    //Стоимость минимальной поездки с включенным расстоянием, коп
    @Builder.Default
    @Column(name = "min_ride_distance_cost")
    private Integer minRideDistanceCost = 0;
    
    //Стоимость за минуту, коп
    @Column(name = "ride_cost_per_min")
    private Integer rideCostPerMin;
    
    //Бесплатных минут пути, включенных в тариф
    @Builder.Default
    @Column(name = "minutes_included")
    private Integer timeIncluded = 0;
    
    //Стоимость минимальной поездки с включенным временем, коп
    @Builder.Default
    @Column(name = "min_ride_time_cost")
    private Integer minRideTimeCost = 0;
    
    //Стоимость за минуту ожидания в стартовой точке , коп
    @Column(name = "wait_cost_per_min")
    private Integer waitCostPerMin;
    
    //Стоимость подачи такси, коп
    @Column(name = "car_service_cost")
    private Integer carServiceCost;
    
    //Стоимость за минуту ожидания в промежуточной точке, коп.
    @Builder.Default
    @Column(name = "wait_cost_per_min_intermediate")
    private Integer waitCostPerMinIntermediate = 0;
    
    //Бесплатных минут ожидания, включенных в тариф
    @Builder.Default
    @Column(name = "free_waiting_time")
    private Integer freeWaitingTime = 0;
    
    //Временные параметры
    @Embedded
    @Builder.Default
    private TimedTariffParams timedTariffParams = new TimedTariffParams();
    
    //Параметры для подбора совместной поездки
    @Embedded
    @Builder.Default
    private CoopTariffParams coopTariffParams = new CoopTariffParams();
    
    //Параметры тарифа по допустимым отклонениям ряда параметров от реестра контрагента
    @Embedded
    @Builder.Default
    private ContractorDeviationsTariffParams contractorDeviationParams = new ContractorDeviationsTariffParams();
    
    //Параметры тарифа за чертой города
    //@Embedded
    //@Builder.Default
    //private final SuburbTariffParams suburbTariffParams = new SuburbTariffParams();
    
    // максимальное количество пассажиров
    @Column(name = "max_capacity")
    private Integer maxCapacity;
}
