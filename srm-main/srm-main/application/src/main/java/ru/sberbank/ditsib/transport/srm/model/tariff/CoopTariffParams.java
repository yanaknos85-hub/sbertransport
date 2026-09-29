package ru.sberbank.ditsib.transport.srm.model.tariff;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

/**
 * Дополнительные параметры тарифа, используемые для подбора совместных поездок
 */

@Embeddable
@Data
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CoopTariffParams {
    public static final int DEFAULT_MIN_CANCEL_TIME = 30;
    public static final double DEFAULT_SAVINGS_PCT = 20d;
    public static final double DEFAULT_DISTANCE_DEVIATION = 5d;
    public static final int DEFAULT_TIME_DEVIATION = 10;
    
    //Отклонение экономии от поездки в процентах
    @Min(0)
    @Max(100)
    @Builder.Default
    @Column(name = "savings_deviation_pct")
    private Double savingsDeviationPct = DEFAULT_SAVINGS_PCT;
    
    //Предельно-допустимое отклонение по километражу, км
    @Min(0)
    @Max(2000)
    @Builder.Default
    @Column(name = "distance_deviation_km")
    private Double distanceDeviationKm = DEFAULT_DISTANCE_DEVIATION;
    
    //Отклонение по времени прибытия во вторую точку в минутах
    @Min(0)
    @Builder.Default
    @Column(name = "time_deviation_min")
    private Integer timeDeviationMin = DEFAULT_TIME_DEVIATION;
    
    //Триггерное время (За какое время до поездки ее можно отменить)
    @Min(0)
    @Max(60)
    @Builder.Default
    @Column(name = "min_cancel_time_min")
    private Integer minCancelTimeMin = DEFAULT_MIN_CANCEL_TIME;
}
