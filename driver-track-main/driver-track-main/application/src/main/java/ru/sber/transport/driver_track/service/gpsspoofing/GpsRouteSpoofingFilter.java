package ru.sber.transport.driver_track.service.gpsspoofing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sber.transport.driver_track.config.GpsSpoofingFilterProperties;
import ru.sber.transport.driver_track.database.driver_track.tables.records.CoordinateRecord;
import ru.sber.transport.driver_track.util.GeoDistanceUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Фильтр GPS-спуфинга — stateless Spring singleton.
 * Все расстояния внутри фильтра обрабатываются в километрах.
 * {@link GeoDistanceUtils#calculateDistance} возвращает километры — прямое сравнение с порогами в км.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GpsRouteSpoofingFilter {

    private final GpsSpoofingFilterProperties properties;
    private final ReturnRadiusPolicy returnRadiusPolicy;

    /**
     * Фильтрует GPS-трек, удаляя физически недостижимые точки (спуфинг).
     * Если фильтр отключён — возвращает исходный список без изменений.
     *
     * @param coords входной список GPS-точек (упорядоченный)
     * @return очищенный список GPS-точек
     */
    public List<CoordinateRecord> filter(List<CoordinateRecord> coords) {
        if (!properties.enabled()) {
            return coords != null ? new ArrayList<>(coords) : List.of();
        }

        if (coords == null || coords.isEmpty()) {
            return List.of();
        }

        var result = new ArrayList<CoordinateRecord>();
        result.add(coords.getFirst());

        var speedWindow = new SpeedWindow(properties);
        speedWindow.addPoint(coords.getFirst());

        var context = GpsFilterContext.normal(coords.getFirst());

        var acceptedCount = 0;
        var rejectedCount = 0;

        for (int i = 1; i < coords.size(); i++) {
            var point = coords.get(i);
            var decision = processPoint(context, point, speedWindow);

            if (decision.accepted) {
                result.add(point);
                speedWindow.addPoint(point);
                acceptedCount++;
            } else {
                rejectedCount++;
            }
            context = decision.context;
        }

        if (rejectedCount > 0) {
            log.info("GpsRouteSpoofingFilter applied: accepted={}, rejected={}", acceptedCount, rejectedCount);
        }

        return result;
    }

    private FilterDecision processPoint(GpsFilterContext context, CoordinateRecord point, SpeedWindow speedWindow) {
        var state = context.state();

        if (state == GpsFilterContext.State.NORMAL) {
            return processNormal(context, point, speedWindow);
        } else {
            return processSpoofing(context, point);
        }
    }

    private FilterDecision processNormal(GpsFilterContext context, CoordinateRecord point, SpeedWindow speedWindow) {
        var v = context.lastValidPoint();
        var distanceKm = GeoDistanceUtils.calculateDistance(v, point);

        if (distanceKm <= properties.jumpThresholdKm()) {
            if (distanceKm < properties.minDistanceBetweenPointsKm()) {
                return new FilterDecision(false, context);
            }
            return new FilterDecision(true, GpsFilterContext.normal(point));
        }

        var avgSpeedKmh = speedWindow.calculateAverageSpeedKmh();

        var newContext = GpsFilterContext.spoofing(v, point, avgSpeedKmh);
        return new FilterDecision(false, newContext);
    }

    private FilterDecision processSpoofing(GpsFilterContext context, CoordinateRecord point) {
        var sprev = context.previousFalsePoint();
        var v = context.lastValidPoint();
        var intervalCount = context.intervalCount();
        var avgSpeedKmh = context.averageSpeedKmh();

        var distanceFromSprevKm = GeoDistanceUtils.calculateDistance(sprev, point);

        if (distanceFromSprevKm <= properties.jumpThresholdKm()) {
            var newContext = context.withNewFalsePoint(point);
            return new FilterDecision(false, newContext);
        }

        var distanceFromVKm = GeoDistanceUtils.calculateDistance(v, point);

        var radiusKm = returnRadiusPolicy.calculateRadius(intervalCount, avgSpeedKmh);

        if (distanceFromVKm <= radiusKm) {
            return new FilterDecision(true, context.withReturn(point));
        } else {
            return new FilterDecision(false, context.withRejectedReturn(point));
        }
    }

    private record FilterDecision(boolean accepted, GpsFilterContext context) {
    }
}
