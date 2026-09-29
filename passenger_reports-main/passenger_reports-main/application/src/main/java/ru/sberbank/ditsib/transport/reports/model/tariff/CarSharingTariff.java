package ru.sberbank.ditsib.transport.reports.model.tariff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;

@Getter
@Entity
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue(value = TransportTypeEnum.Constants.CARSHARING_STRING)
public class CarSharingTariff extends BaseTariff {
    //Цена за км, коп
    @Column(name = "ride_cost_per_km")
    private Integer rideCostPerKm;
    
    //Стоимость за минуту, коп
    @Column(name = "ride_cost_per_min")
    private Integer rideCostPerMin;
    
    //Стоимость времени ожидания при бронировании и аренде ТС, коп.
    @Column(name = "wait_cost_per_min")
    private Integer waitCostPerMin;
    
    //Временные параметры
    @Embedded
    @Builder.Default
    private final TimedTariffParams timedTariffParams = new TimedTariffParams();
    
    //Коэффициент загрузки дорог (пробок, баллы Яндекс, прогнозные) Более 7 баллов
    @Builder.Default
    @Column(name = "coef_traffic")
    private final Double coefTraffic = 0d;
    
    //Коэффициент доплаты за детское кресло
    @Builder.Default
    @Column(name = "coef_child_seat")
    private final Double coefChildSeat = 1d;
    
    //Коэффициент доплаты за перевозку животного
    @Builder.Default
    @Column(name = "coef_pet_transport")
    private final Double coefPetTransport = 1d;
    
    //Коэффициент на полное покрытие ответственности КАСКО
    @Builder.Default
    @Column(name = "coef_casco")
    private final Double coefCasko = 1d;
}
