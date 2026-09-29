package ru.sberbank.ditsib.transport.tariff.database.model;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Параметры тарифа за чертой города
 */
@ToString
@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SuburbTariffParams {
    
    //Цена за км  за чертой города, коп
    @Min(0)
    @Max(1000_00)
    @Builder.Default
    @Column(name = "cost_per_km_suburb")
    private int costPerKmSuburb = 0;
    
    //Цена за минуту  за чертой города, коп
    @Min(0)
    @Max(1000_00)
    @Builder.Default
    @Column(name = "cost_per_min_suburb")
    private int costPerMinSuburb = 0;
    
    //Стоимость 1 км платной подачи за чертой города, коп.
    @Min(0)
    @Max(1000_00)
    @Builder.Default
    @Column(name = "suburb_service_cost_per_km")
    private int suburbServiceCostPerKm = 0;
    
    //Стоимость 1 мин платной подачи за чертой города, коп.
    @Min(0)
    @Max(1000_00)
    @Builder.Default
    @Column(name = "suburb_service_cost_per_min")
    private int suburbServiceCostPerMin = 0;
    
    //Стоимость пробега 1 км межрегиональной поездки, коп.
    @Min(0)
    @Max(1000_00)
    @Builder.Default
    @Column(name = "cost_per_min_inter_region")
    private int costPerKmInterRegion = 0;
    
    //Стоимость 1 минуты межрегиональной поездки, коп
    @Min(0)
    @Max(1000_00)
    @Builder.Default
    @Column(name = "cost_per_km_inter_region")
    private int costPerMinInterRegion = 0;
}
