package ru.sber.transport.driver_track.service.gpsspoofing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.driver_track.config.GpsSpoofingFilterProperties;

/**
 * Политика расчёта допустимого радиуса возврата к фактическому маршруту.
 * Формула: R = properties + increment × (intervalCount - 1)  (значения в км)
 */
@Component
@RequiredArgsConstructor
public final class ReturnRadiusPolicy {

    private static final double BASE_RADIUS_KM = 0.150;

    private final GpsSpoofingFilterProperties properties;

    public double selectIncrement(double averageSpeedKmh) {
        if (averageSpeedKmh < 30) {
            return properties.lowSpeedIncrementKm();
        } else if (averageSpeedKmh <= 60) {
            return properties.mediumSpeedIncrementKm();
        } else {
            return properties.highSpeedIncrementKm();
        }
    }

    public double calculateRadiusWithIncrement(int intervalCount, double incrementKm) {
        if (intervalCount <= 0) {
            return BASE_RADIUS_KM;
        }
        return BASE_RADIUS_KM + incrementKm * (intervalCount - 1);
    }

    public double calculateRadius(int intervalCount, double averageSpeedKmh) {
        var increment = selectIncrement(averageSpeedKmh);
        return calculateRadiusWithIncrement(intervalCount, increment);
    }
}
