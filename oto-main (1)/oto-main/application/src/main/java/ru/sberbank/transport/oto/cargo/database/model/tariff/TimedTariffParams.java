package ru.sberbank.transport.oto.cargo.database.model.tariff;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;


@ToString
@Embeddable
@Getter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimedTariffParams {
    
    /**
     * Коэффициент временного интервала поездки: утро будние дни 07:00-10:00
     */
    @Positive
    @Max(10)
    @Builder.Default
    @Column(name = "coef_work_day_morning")
    private final Double coefWorkDayMorning = 1d;
    
    /**
     * Коэффициент временного интервала поездки: день будние дни 10:00-18:00
     */
    @Positive
    @Max(10)
    @Builder.Default
    @Column(name = "coef_work_day_noon")
    private final Double coefWorkDayNoon = 1d;
    
    /**
     * Коэффициент временного интервала поездки: вечерний будние дни 18:00-22:00
     */
    @Positive
    @Max(10)
    @Builder.Default
    @Column(name = "coef_work_day_evening")
    private final Double coefWorkDayEvening = 1d;
    
    /**
     * Коэффициент временного интервала поездки: ночной будние дни 22:00-07:00
     */
    @Positive
    @Max(10)
    @Builder.Default
    @Column(name = "coef_work_day_night")
    private final Double coefWorkDayNight = 1d;
    
    /**
     * Коэффициент выходного дня: СБ, ВСКР
     */
    @Positive
    @Max(10)
    @Builder.Default
    @Column(name = "coef_day_off")
    private final Double coefDayOff = 1d;
}
