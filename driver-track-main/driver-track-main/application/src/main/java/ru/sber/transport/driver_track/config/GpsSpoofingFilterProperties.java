package ru.sber.transport.driver_track.config;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Конфигурация фильтра GPS-спуфинга.
 *
 * <p>Фильтр выявляет и отбрасывает подозрительные GPS-координаты, которые физически
 * недостижимы за указанное время (признак спуфинга или сбоя GPS-модуля).</p>
 *
 * <p>Работа фильтра:</p>
 * <ul>
 *   <li><b>Нормальный режим</b> — точка принимается, если расстояние от последней валидной
 *       точки не превышает {@code jumpThresholdKm} и не ближе {@code minDistanceBetweenPointsKm}.</li>
 *   <li><b>Режим SPOOFING</b> — при резком скачке фильтр переходит в режим обнаружения ложного
 *       маршрута. Новые точки отбрасываются, пока не произойдет возврат в допустимый радиус
 *       от последней валидной точки (радиус растёт с количеством интервалов).</li>
 * </ul>
 *
 * @param enabled                     Включён ли фильтр. При {@code false} все координаты проходят без изменений.
 * @param intervalSeconds             Время между интервалами в секундах. Используется в {@link ru.sber.transport.driver_track.service.gpsspoofing.SpeedWindow}.
 * @param maxIntervals                Максимальное количество интервалов для расчёта средней скорости. Количество сохраняемых точек = {@code maxIntervals} + 1.
 * @param jumpThresholdKm             Порог резкого скачка координат (в км). При превышении — точка считается началом спуфинга.
 * @param minDistanceBetweenPointsKm  Минимальное расстояние между принимаемыми точками (в км). Убирает GPS-дрожание.
 * @param lowSpeedIncrementKm         Прибавка к радиусу возврата за интервал при низкой скорости (&lt; 30 км/ч).
 * @param mediumSpeedIncrementKm      Прибавка к радиусу возврата за интервал при средней скорости (30–60 км/ч).
 * @param highSpeedIncrementKm        Прибавка к радиусу возврата за интервал при высокой скорости (&gt; 60 км/ч).
 */
@Validated
@ConfigurationProperties(prefix = "driver-track.gps-spoofing-filter")
public record GpsSpoofingFilterProperties(

    @DefaultValue("true")
    boolean enabled,

    @DefaultValue("5")
    @Positive
    int intervalSeconds,

    @DefaultValue("6")
    @Positive
    int maxIntervals,

    @DefaultValue("0.150")
    @Positive
    double jumpThresholdKm,

    @DefaultValue("0.005")
    @Positive
    double minDistanceBetweenPointsKm,

    @DefaultValue("0.060")
    @Positive
    double lowSpeedIncrementKm,

    @DefaultValue("0.100")
    @Positive
    double mediumSpeedIncrementKm,

    @DefaultValue("0.150")
    @Positive
    double highSpeedIncrementKm
) {
}
