package ru.sberbank.ditsib.transport.tariff.database.model;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Дополнительные параметры тарифа, используемые для подбора совместных поездок
 */
@ToString
@Embeddable
@Data
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoopTariffParams {
    public static final int DEFAULT_MIN_CANCEL_TIME = 30;
    
    //Отклонение экономии от поездки в процентах
    @Min(0)
    @Max(100)
    @Builder.Default
    @Column(name = "savings_deviation_pct")
    private double savingsDeviationPct = 0d;
    
    //Предельно-допустимое отклонение по километражу, км
    @Min(0)
    @Max(2000)
    @Builder.Default
    @Column(name = "distance_deviation_km")
    private double distanceDeviationKm = 0d;
    
    //Отклонение по времени прибытия во вторую точку в минутах
    @Min(0)
    @Max(43_200)
    @Builder.Default
    @Column(name = "time_deviation_min")
    private int timeDeviationMin = 0;
    
    //Триггерное время (За какое время до поездки ее можно отменить)
    @Min(0)
    @Max(60)
    @Builder.Default
    @Column(name = "min_cancel_time_min")
    private int minCancelTimeMin = DEFAULT_MIN_CANCEL_TIME;
}
