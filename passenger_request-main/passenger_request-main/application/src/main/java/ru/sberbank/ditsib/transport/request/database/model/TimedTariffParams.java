package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalTime;

/**
 * Модель временных параметров.
 */
@ToString
@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimedTariffParams {
    
    /**
     * Начало утренней тарифной зоны.
     */
    public static final LocalTime MORNING_START = LocalTime.of(7, 0);
    
    /**
     * Начало дневной зоны.
     */
    public static final LocalTime NOON_START = LocalTime.of(10, 0);
    
    /**
     * Начало вечерней тарифной зоны.
     */
    public static final LocalTime EVENING_START = LocalTime.of(18, 0);
    
    /**
     * Начало ночной тарифной зоны.
     */
    public static final LocalTime NIGHT_START = LocalTime.of(22, 0);
    
    /**
     * Коэффициент временного интервала поездки: утро будние дни 07:00-10:00.
     */
    @Positive
    @Max(10)
    @Builder.Default
    @Column(name = "coef_work_day_morning")
    private double coefWorkDayMorning = 1d;
    
    /**
     * Коэффициент временного интервала поездки: день будние дни 10:00-18:00.
     */
    @Positive
    @Max(10)
    @Builder.Default
    @Column(name = "coef_work_day_noon")
    private double coefWorkDayNoon = 1d;
    
    /**
     * Коэффициент временного интервала поездки: вечерний будние дни 18:00-22:00.
     */
    @Positive
    @Max(10)
    @Builder.Default
    @Column(name = "coef_work_day_evening")
    private double coefWorkDayEvening = 1d;
    
    /**
     * Коэффициент временного интервала поездки: ночной будние дни 22:00-07:00.
     */
    @Positive
    @Max(10)
    @Builder.Default
    @Column(name = "coef_work_day_night")
    private double coefWorkDayNight = 1d;
    
    /**
     * Коэффициент выходного дня: СБ, ВСКР.
     */
    @Positive
    @Max(10)
    @Builder.Default
    @Column(name = "coef_day_off")
    private double coefDayOff = 1d;
}
