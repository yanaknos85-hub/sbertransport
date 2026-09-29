package ru.sber.transport.driver_track.service.gpsspoofing;

import lombok.RequiredArgsConstructor;
import ru.sber.transport.driver_track.config.GpsSpoofingFilterProperties;
import ru.sber.transport.driver_track.database.driver_track.tables.records.CoordinateRecord;
import ru.sber.transport.driver_track.util.GeoDistanceUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Класс для накопления последних валидных точек и расчёта средней скорости.
 */
@RequiredArgsConstructor
public final class SpeedWindow {

    private final GpsSpoofingFilterProperties properties;

    private final List<CoordinateRecord> points = new ArrayList<>();

    public void addPoint(CoordinateRecord point) {
        points.add(point);
        if (points.size() > properties.maxIntervals() + 1) {
            points.remove(0);
        }
    }

    public int getIntervalCount() {
        return Math.max(0, points.size() - 1);
    }

    public List<CoordinateRecord> getPoints() {
        return Collections.unmodifiableList(points);
    }

    public double calculateAverageSpeedKmh() {
        if (points.size() < 2) {
            return 0;
        }

        var totalDistanceKm = 0.0;
        for (int i = 0; i < points.size() - 1; i++) {
            var p1 = points.get(i);
            var p2 = points.get(i + 1);
            totalDistanceKm += GeoDistanceUtils.calculateDistance(p1, p2);
        }

        var totalSeconds = (points.size() - 1) * properties.intervalSeconds();
        return totalDistanceKm / (totalSeconds / 3600.0);
    }

}
