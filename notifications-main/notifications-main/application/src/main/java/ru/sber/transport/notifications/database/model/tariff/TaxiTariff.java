package ru.sber.transport.notifications.database.model.tariff;

import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import jakarta.persistence.*;

/**
 * Сущность тарифа такси
 */
@Setter
@Getter
@Entity
@DiscriminatorValue(value = TransportTypeEnum.Constants.TAXI_STRING)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TaxiTariff extends BaseTariff {
    
    //Класс такси
    @Column(name = "taxi_class")
    @Enumerated(EnumType.STRING)
    private TaxiClass taxiClass;
    
    //Цена за км, коп
    @Column(name = "ride_cost_per_km")
    private Integer rideCostPerKm;
    
    //Стоимость минимальной поездки с включенным расстоянием, коп
    @Column(name = "min_ride_distance_cost")
    private int minRideDistanceCost;
    
    //Стоимость за минуту, коп
    @Column(name = "ride_cost_per_min")
    private Integer rideCostPerMin;
    
    //Стоимость минимальной поездки с включенным временем, коп
    @Column(name = "min_ride_time_cost")
    private int minRideTimeCost;
    
    //Стоимость за минуту ожидания в стартовой точке , коп
    @Column(name = "wait_cost_per_min")
    private Integer waitCostPerMin;
    
    //Стоимость подачи такси, коп
    @Column(name = "car_service_cost")
    private Integer carServiceCost;
    
    //Стоимость за минуту ожидания в промежуточной точке, коп.
    @Column(name = "wait_cost_per_min_intermediate")
    private int waitCostPerMinIntermediate;
    
}
