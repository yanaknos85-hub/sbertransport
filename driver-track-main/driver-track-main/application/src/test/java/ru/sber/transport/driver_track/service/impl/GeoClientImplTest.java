package ru.sber.transport.driver_track.service.impl;

import io.grpc.stub.StreamObserver;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.driver_track.config.GeoProperties;
import ru.sber.transport.driver_track.database.driver_track.tables.records.CoordinateRecord;
import ru.sber.transport.geo.grpc.dto.GeoDescriptor;
import ru.sber.transport.geo.grpc.service.GeoServiceGrpc;
import ru.sber.transport.grpc.test.extension.GrpcCleanupExtension;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@IsolatedTest
@Feature("app_platform_driver_track")
@DisplayName("Проверка гео провайдера")
public class GeoClientImplTest {

    @RegisterExtension
    private static final GrpcCleanupExtension extension = new GrpcCleanupExtension();

    private GeoClientImpl geoClient;

    @BeforeEach
    void setUp() {
        var geoProperties = new GeoProperties(500, 0.00001);
        geoClient = new GeoClientImpl(geoProperties, null);
    }

    @Test
    @DisplayName("Воссоздание маршрута")
    void test_recreateRoute() throws IOException {
        var count = 10;
        var coords = IntStream.range(0, count)
                .mapToObj(i -> new CoordinateRecord(null, null, Instancio.create(Double.class), Instancio.create(Double.class), null))
                .toList();

        var coordinate = GeoDescriptor.AddressRequestCoordinates.newBuilder()
                .setLatitude(Instancio.create(Double.class))
                .setLongitude(Instancio.create(Double.class))
                .build();

        var segment = GeoDescriptor.Segment.newBuilder()
                .setDistance(Instancio.create(Double.class))
                .addAllCoordinates(List.of(coordinate))
                .build();

        var response = GeoDescriptor.RouteResponse.newBuilder()
                .setDistance(Instancio.create(Double.class))
                .addAllSegments(List.of(segment))
                .build();

        var channel = extension.addService(new GeoServiceGrpc.GeoServiceImplBase() {
            @Override
            public void getRouteByCoords(GeoDescriptor.RouteRecreationRequest request, StreamObserver<GeoDescriptor.RouteResponse> responseObserver) {
                var actualCoords = request.getCoordinatesList();
                assertEquals(count, actualCoords.size());
                for (var i = 0; i < count; i++) {
                    var actualCoord = actualCoords.get(i);
                    assertEquals(coords.get(i).getLatitude(), actualCoord.getLatitude());
                    assertEquals(coords.get(i).getLongitude(), actualCoord.getLongitude());
                }

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }

        });
        ReflectionTestUtils.setField(geoClient, "stub", GeoServiceGrpc.newBlockingStub(channel));

        var result = geoClient.recreateRoute(coords);

        assertEquals(1, result.getSegments().size());
        assertEquals(response.getDistance() / 1000, result.getDistance());

        var actualSegment = result.getSegments().getFirst();
        assertEquals(response.getDistance() / 1000, actualSegment.getDistance());
        assertEquals(response.getSegmentsList().getFirst().getCoordinatesCount(), actualSegment.getPoints().size());

        var expectedCoordinates = response.getSegmentsList().getFirst().getCoordinatesList();
        var actualCoordinates = actualSegment.getPoints();
        for (var j = 0; j < expectedCoordinates.size(); j++) {
            var expectedCoordinate = expectedCoordinates.get(j);
            var actualCoordinate = actualCoordinates.get(j);
            assertEquals(expectedCoordinate.getLatitude(), actualCoordinate.getLatitude());
            assertEquals(expectedCoordinate.getLongitude(), actualCoordinate.getLongitude());
        }
    }

    @Test
    @DisplayName("Воссоздание маршрута с батчингом")
    void test_recreateRoute_batching() throws IOException {
        var batchSize = 500;
        var totalCoords = 600;

        var coords = IntStream.range(0, totalCoords)
                .mapToObj(i -> new CoordinateRecord(
                        null, null,
                        55.0 + i * 0.001,
                        37.0 + i * 0.001,
                        null))
                .toList();

        var firstGeoPoint = GeoDescriptor.AddressRequestCoordinates.newBuilder()
                .setLatitude(55.0)
                .setLongitude(37.0)
                .build();

        var lastGeoPointBatch1 = GeoDescriptor.AddressRequestCoordinates.newBuilder()
                .setLatitude(55.0 + 499 * 0.001)
                .setLongitude(37.0 + 499 * 0.001)
                .build();

        var firstGeoPointBatch2 = GeoDescriptor.AddressRequestCoordinates.newBuilder()
                .setLatitude(55.0 + 499 * 0.001) // дубль = последняя точка батча 1
                .setLongitude(37.0 + 499 * 0.001)
                .build();

        var lastGeoPointBatch2 = GeoDescriptor.AddressRequestCoordinates.newBuilder()
                .setLatitude(55.0 + 599 * 0.001)
                .setLongitude(37.0 + 599 * 0.001)
                .build();

        var segment1 = GeoDescriptor.Segment.newBuilder()
                .setDistance(100000) // 100 км
                .addAllCoordinates(List.of(firstGeoPoint, lastGeoPointBatch1))
                .build();

        var segment2 = GeoDescriptor.Segment.newBuilder()
                .setDistance(50000) // 50 км
                .addAllCoordinates(List.of(firstGeoPointBatch2, lastGeoPointBatch2))
                .build();

        var response1 = GeoDescriptor.RouteResponse.newBuilder()
                .setDistance(100000)
                .addAllSegments(List.of(segment1))
                .build();

        var response2 = GeoDescriptor.RouteResponse.newBuilder()
                .setDistance(50000)
                .addAllSegments(List.of(segment2))
                .build();

        var callCount = new int[1];

        var channel = extension.addService(new GeoServiceGrpc.GeoServiceImplBase() {
            @Override
            public void getRouteByCoords(GeoDescriptor.RouteRecreationRequest request, StreamObserver<GeoDescriptor.RouteResponse> responseObserver) {
                var actualCoords = request.getCoordinatesList();

                if (callCount[0] == 0) {
                    // Первый батч: 500 точек + 0 (нет предыдущего) = 500
                    assertEquals(batchSize, actualCoords.size());
                    assertEquals(55.0, actualCoords.get(0).getLatitude());
                    responseObserver.onNext(response1);
                } else {
                    // Второй батч: 100 точек + 1 дубль = 101
                    assertEquals(totalCoords - batchSize + 1, actualCoords.size());
                    // Дубль — последняя точка батча 1
                    assertEquals(55.0 + 499 * 0.001, actualCoords.get(0).getLatitude());
                    responseObserver.onNext(response2);
                }
                callCount[0]++;
                responseObserver.onCompleted();
            }
        });

        ReflectionTestUtils.setField(geoClient, "stub", GeoServiceGrpc.newBlockingStub(channel));

        var result = geoClient.recreateRoute(coords);

        assertEquals(2, callCount[0]);
        assertEquals(1, result.getSegments().size());
        assertEquals(150.0, result.getDistance());
        var allPoints = result.getSegments().getFirst().getPoints();
        assertEquals(3, allPoints.size());
        assertEquals(55.0, allPoints.get(0).getLatitude());
        assertEquals(37.0, allPoints.get(0).getLongitude());
        var lastPoint = allPoints.get(allPoints.size() - 1);
        assertEquals(55.0 + 599 * 0.001, lastPoint.getLatitude());
        assertEquals(37.0 + 599 * 0.001, lastPoint.getLongitude());
    }
}
