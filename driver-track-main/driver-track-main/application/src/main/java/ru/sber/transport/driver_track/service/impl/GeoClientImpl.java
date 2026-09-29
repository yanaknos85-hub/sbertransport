package ru.sber.transport.driver_track.service.impl;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sber.transport.driver_track.config.GeoProperties;
import ru.sber.transport.driver_track.database.driver_track.tables.records.CoordinateRecord;
import ru.sber.transport.driver_track.dto.GeoWaypointDTO;
import ru.sber.transport.driver_track.dto.RouteDTO;
import ru.sber.transport.driver_track.dto.SegmentDTO;
import ru.sber.transport.driver_track.service.GeoClient;
import ru.sber.transport.geo.grpc.dto.GeoDescriptor;
import ru.sber.transport.geo.grpc.service.GeoServiceGrpc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.time.Instant;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeoClientImpl implements GeoClient {

    private final GeoProperties geoProperties;

    private final GeoServiceGrpc.GeoServiceBlockingStub stub;

    @Override
    public RouteDTO recreateRoute(List<CoordinateRecord> coords) {
        Objects.requireNonNull(coords, "Coordinates must not be null");
        if (coords.isEmpty()) {
            return new RouteDTO(0D, null, List.of());
        }

        var batches = Lists.partition(
                List.copyOf(coords),
                geoProperties.batchSize()
        );

        log.info(
                "Processing {} coordinates in {} batches, batchSize={}",
                coords.size(),
                batches.size(),
                geoProperties.batchSize()
        );

        var allPoints = new ArrayList<GeoWaypointDTO>();
        double totalDistance = 0D;
        GeoWaypointDTO lastPoint = null;
        long nextNewPointTimestamp = Instant.now().getEpochSecond();

        for (int batchIndex = 0; batchIndex < batches.size(); batchIndex++) {
            var batch = batches.get(batchIndex);
            var requestCoordinates = addPreviousLastPoint(
                    batch,
                    lastPoint
            );
            log.info(
                    "Processing batch {}/{} with {} coordinates",
                    batchIndex + 1,
                    batches.size(),
                    requestCoordinates.size()
            );
            var request = buildRequest(
                    requestCoordinates,
                    nextNewPointTimestamp
            );
            var result = stub.getRouteByCoords(request);

            nextNewPointTimestamp = getNextNewPointTimestamp(nextNewPointTimestamp, requestCoordinates);
            totalDistance += result.getDistance() / 1000.0;
            var newPoints = extractPoints(result);
            removeBoundaryDuplicate(
                    newPoints,
                    lastPoint
            );
            allPoints.addAll(newPoints);
            if (!newPoints.isEmpty()) {
                lastPoint = newPoints.getLast();
            }
        }
        var segment = new SegmentDTO(
                totalDistance,
                null,
                List.copyOf(allPoints)
        );

        return new RouteDTO(
                totalDistance,
                null,
                List.of(segment)
        );
    }

    private static long getNextNewPointTimestamp(long nextNewPointTimestamp, List<CoordinateRecord> requestCoordinates) {
        return nextNewPointTimestamp + requestCoordinates.size();
    }

    private List<CoordinateRecord> addPreviousLastPoint(
            List<CoordinateRecord> batch,
            GeoWaypointDTO previousLastPoint
    ) {
        if (previousLastPoint == null) {
            return batch;
        }
        var coordinates = new ArrayList<CoordinateRecord>(
                batch.size() + 1
        );
        coordinates.add(new CoordinateRecord(
                null,
                null,
                previousLastPoint.getLatitude(),
                previousLastPoint.getLongitude(),
                null
        ));
        coordinates.addAll(batch);
        return coordinates;
    }

    private GeoDescriptor.RouteRecreationRequest buildRequest(
            List<CoordinateRecord> coordinates,
            long startTimestamp
    ) {
        var geoCoordinates = IntStream.range(0, coordinates.size())
                .mapToObj(index -> {
                    var coordinate = coordinates.get(index);
                    return GeoDescriptor.RouteRecreationPoint
                            .newBuilder()
                            .setLatitude(coordinate.getLatitude())
                            .setLongitude(coordinate.getLongitude())
                            .setTime(startTimestamp + index)
                            .build();
                })
                .toList();
        return GeoDescriptor.RouteRecreationRequest
                .newBuilder()
                .addAllCoordinates(geoCoordinates)
                .build();
    }

    private ArrayList<GeoWaypointDTO> extractPoints(
            GeoDescriptor.RouteResponse result
    ) {
        return result.getSegmentsList()
                .stream()
                .flatMap(segment ->
                        segment.getCoordinatesList().stream()
                )
                .map(point -> new GeoWaypointDTO(
                        point.getLatitude(),
                        point.getLongitude()
                ))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void removeBoundaryDuplicate(
            List<GeoWaypointDTO> newPoints,
            GeoWaypointDTO previousLastPoint
    ) {
        if (previousLastPoint == null || newPoints.isEmpty()) {
            return;
        }
        if (isSamePoint(previousLastPoint, newPoints.getFirst())) {
            newPoints.removeFirst();
        }
    }

    private boolean isSamePoint(
            GeoWaypointDTO first,
            GeoWaypointDTO second
    ) {
        return Math.abs(
                first.getLatitude() - second.getLatitude()
        ) < geoProperties.coordinateEpsilon()
                && Math.abs(
                first.getLongitude() - second.getLongitude()
        ) < geoProperties.coordinateEpsilon();
    }
}