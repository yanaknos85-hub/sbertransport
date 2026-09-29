package ru.sber.transport.driver_track.service.impl;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.driver_track.database.driver_track.tables.records.CoordinateRecord;
import ru.sber.transport.driver_track.dto.GeoWaypointDTO;
import ru.sber.transport.driver_track.dto.RouteDTO;
import ru.sber.transport.driver_track.dto.RouteSource;
import ru.sber.transport.driver_track.dto.SegmentDTO;
import ru.sber.transport.driver_track.service.CalculateRouteService;
import ru.sber.transport.driver_track.service.GeoClient;
import ru.sber.transport.driver_track.service.gpsspoofing.GpsRouteSpoofingFilter;
import ru.sber.transport.driver_track.util.GeoDistanceUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;



@Slf4j
@Service
@RequiredArgsConstructor
public class CalculateRouteServiceImpl implements CalculateRouteService {

    private final GeoClient geoClient;
    private final GpsRouteSpoofingFilter gpsRouteSpoofingFilter;

    @Override
    public Map<RouteSource, RouteDTO> calculateRoute(List<CoordinateRecord> coords, UUID tripId) {
        debugSegmentLengthDistribution(coords);

        var result = new HashMap<RouteSource, RouteDTO>();

        var cleanedCoords = gpsRouteSpoofingFilter.filter(coords);
        log.info("GPS spoofing filter for trip {}: input={}, output={}", tripId, coords.size(), cleanedCoords.size());

        if (cleanedCoords.size() < 2) {
            return result;
        }


        try {
            result.put(RouteSource.TWO_GIS, geoClient.recreateRoute(cleanedCoords));
        } catch (Exception e) {
            log.error("Error while 2gis calculating route for trip {}", tripId, e);
        }

        result.put(RouteSource.FORMULA, calculateRouteByFormula(cleanedCoords));
        return result;
    }

    public List<CoordinateRecord> prepareCoords(@NotNull List<CoordinateRecord> coords) {
        if (coords.isEmpty()) {
            return coords;
        }

        var result = new LinkedList<CoordinateRecord>();
        result.add(new CoordinateRecord(UUID.randomUUID(), null, coords.getFirst().getLatitude(),
                coords.getFirst().getLongitude(), LocalDateTime.now(ZoneOffset.UTC)));

        for (var i = 1; i < coords.size(); i++) {
            var coord = coords.get(i - 1);
            var coord2 = coords.get(i);
            var distance = GeoDistanceUtils.calculateDistance(coord, coord2);

            if (distance >= 0.003) {
                result.add(new CoordinateRecord(UUID.randomUUID(), null, coord2.getLatitude(), coord2.getLongitude(),
                        LocalDateTime.now(ZoneOffset.UTC)));
            }
        }
        return result;
    }

    private RouteDTO calculateRouteByFormula(List<CoordinateRecord> coords) {
        var totalDistance = calculateDistance(coords);
        var points = coords.stream().map(coord -> new GeoWaypointDTO(coord.getLatitude(), coord.getLongitude())).toList();
        var segment = new SegmentDTO(totalDistance, null, points);
        return new RouteDTO(totalDistance, null, List.of(segment));
    }

    private void debugSegmentLengthDistribution(List<CoordinateRecord> coords) {
        if (coords == null || coords.size() < 2) {
            log.info("DEBUG segments distribution: not enough coords ({}), skip", coords == null ? "null" : coords.size());
            return;
        }

        var bucketSize = 100;
        var segmentCounts = new TreeMap<Integer, Long>();

        for (var i = 0; i < coords.size() - 1; i++) {
            var distance = GeoDistanceUtils.calculateDistance(coords.get(i), coords.get(i + 1));
            var bucket = ((int) (distance / bucketSize)) + 1;
            segmentCounts.merge(bucket, 1L, Long::sum);
        }

        var logParts = segmentCounts.entrySet().stream()
                .map(e -> e.getValue() + " шт @ " + (e.getKey() * bucketSize) + "-" + ((e.getKey() + 1) * bucketSize) + "m")
                .toList();

        log.info("DEBUG segments distribution [{}]: {}", coords.size() + " segs", logParts);
    }

    private Double calculateDistance(List<CoordinateRecord> coords) {
        var result = 0D;
        for (var i = 0; i < coords.size() - 1; i++) {
            var coord1 = coords.get(i);
            var coord2 = coords.get(i + 1);
            result += GeoDistanceUtils.calculateDistance(coord1, coord2);
        }

        return result;
    }
}
