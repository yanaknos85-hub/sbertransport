package ru.sberbank.ditsib.transport.tariff.database.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Getter
@Setter
@Entity
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue(value = TransportTypeEnum.Constants.SCOOTER_STRING)
public class ScooterTariff extends BaseTariffWithContract {
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
    
    //Стоимость брони FIX, коп
    @NotNull
    @Min(0)
    @Max(1000_00)
    @Column(name = "booking_cost")
    private Integer bookingCost;
    
    //Временные параметры
    @Embedded
    @Builder.Default
    private TimedTariffParams timedTariffParams = new TimedTariffParams();
    
    //Коэффициент на страхование
    @Positive
    @Builder.Default
    @Column(name = "coef_insurance")
    private Double coefInsurance = 1d;
}
