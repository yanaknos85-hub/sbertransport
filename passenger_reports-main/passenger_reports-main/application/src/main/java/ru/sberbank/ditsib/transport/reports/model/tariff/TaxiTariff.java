package ru.sberbank.ditsib.transport.reports.model.tariff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * Сущность тарифа такси
 */
@Getter
@Entity
@DiscriminatorValue(value = TransportTypeEnum.Constants.TAXI_STRING)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
public class TaxiTariff extends BaseTariff {
    //Класс такси
    @Column(name = "taxi_class")
    @Enumerated(EnumType.STRING)
    private TaxiClass taxiClass;
    
    //Цена за км, коп
    @Column(name = "ride_cost_per_km")
    private Integer rideCostPerKm;
    
    //Стоимость минимальной поездки с включенным расстоянием, коп
    @Builder.Default
    @Column(name = "min_ride_distance_cost")
    private final Integer minRideDistanceCost = 0;
    
    //Стоимость за минуту, коп
    @Column(name = "ride_cost_per_min")
    private Integer rideCostPerMin;
    
    //Стоимость минимальной поездки с включенным временем, коп
    @Builder.Default
    @Column(name = "min_ride_time_cost")
    private final Integer minRideTimeCost = 0;
    
    //Стоимость за минуту ожидания в стартовой точке , коп
    @Column(name = "wait_cost_per_min")
    private Integer waitCostPerMin;
    
    //Стоимость подачи такси, коп
    @Column(name = "car_service_cost")
    private Integer carServiceCost;
    
    //Стоимость за минуту ожидания в промежуточной точке, коп.
    @Builder.Default
    @Column(name = "wait_cost_per_min_intermediate")
    private final Integer waitCostPerMinIntermediate = 0;
    
    //Параметры тарифа по допустимым отклонениям ряда параметров от реестра контрагента
    @Embedded
    @Builder.Default
    private final ContractorDeviationsTariffParams contractorDeviationParams = new ContractorDeviationsTariffParams();
    
    @Column(name = "work_group")
    private String workGroup;

    private UUID regionId;
}
