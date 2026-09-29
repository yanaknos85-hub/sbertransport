package ru.sberbank.ditsib.transport.request.database.model.carsharing;

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
import ru.sberbank.ditsib.transport.request.database.model.BaseTariffWithContract;
import ru.sberbank.ditsib.transport.request.database.model.TimedTariffParams;

/**
 * Cущность тарифа каршеринга, получаемая из сообщения
 */
@Getter
@Entity
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue(value = TransportTypeEnum.Constants.CARSHARING_STRING)
public class CarsharingTariff extends BaseTariffWithContract {
    
    /**
     * Цена за км, коп.
     */
    @NotNull
    @Min(0)
    @Max(1000_00)
    @Column(name = "ride_cost_per_km")
    private int rideCostPerKm;
    
    /**
     * Стоимость за минуту, коп.
     */
    @NotNull
    @Min(0)
    @Max(1000_00)
    @Column(name = "ride_cost_per_min")
    private int rideCostPerMin;
    
    /**
     * Стоимость времени ожидания при бронировании и аренде ТС, коп.
     */
    @NotNull
    @Min(0)
    @Max(1000_00)
    @Column(name = "wait_cost_per_min")
    private int waitCostPerMin;
    
    /**
     * Временные параметры.
     */
    @Embedded
    @Builder.Default
    private TimedTariffParams timedTariffParams = new TimedTariffParams();
    
    /**
     * Коэффициент загрузки дорог (пробок, баллы Яндекс, прогнозные) Более 7 баллов.
     */
    @Min(0)
    @Max(10)
    @Column(name = "coef_traffic")
    private double coefTraffic;
    
    /**
     * Коэффициент доплаты за детское кресло.
     */
    @Min(1)
    @Max(10)
    @Builder.Default
    @Column(name = "coef_child_seat")
    private double coefChildSeat = 1d;
    
    /**
     * Коэффициент доплаты за перевозку животного.
     */
    @Min(1)
    @Max(10)
    @Builder.Default
    @Column(name = "coef_pet_transport")
    private double coefPetTransport = 1d;
    
    /**
     * Коэффициент на полное покрытие ответственности КАСКО.
     */
    @Positive
    @Max(10)
    @Builder.Default
    @Column(name = "coef_casco")
    private double coefCasko = 1d;
}
