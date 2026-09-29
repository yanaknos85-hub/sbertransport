package ru.sber.transport.driver_track.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.jooq.JSON;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.driver_track.database.driver_track.tables.records.CoordinateRecord;
import ru.sber.transport.driver_track.database.driver_track.tables.records.DriverMessageRecord;
import ru.sber.transport.driver_track.database.driver_track.tables.records.FactWaypointsTripRecord;
import ru.sber.transport.driver_track.database.driver_track.tables.records.RouteRecord;
import ru.sber.transport.driver_track.dto.GeoWaypointDTO;
import ru.sber.transport.driver_track.dto.RouteDTO;
import ru.sber.transport.driver_track.dto.RouteSource;
import ru.sber.transport.driver_track.dto.SegmentDTO;
import ru.sber.transport.driver_track.mapper.CoordinateMapper;
import ru.sber.transport.driver_track.mapper.CoordinateMapperImpl;
import ru.sber.transport.driver_track.messaging.sender.TripFactDistanceSender;
import ru.sber.transport.driver_track.repository.CoordinateRepository;
import ru.sber.transport.driver_track.repository.DriverRepository;
import ru.sber.transport.driver_track.repository.RouteRepository;
import ru.sber.transport.driver_track.service.*;
import ru.sber.transport.driver_track.service.gpsspoofing.GpsRouteSpoofingFilter;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_driver_track")
@DisplayName("Проверка сервиса координат")
class RouteServiceImplTest {

    private final CoordinateRepository coordinateRepository = mock(CoordinateRepository.class);
    private final TripFactDistanceSender tripFactDistanceSender = mock(TripFactDistanceSender.class);
    private final DriverRepository driverRepository = mock(DriverRepository.class);
    private final RouteRepository routeRepository = mock(RouteRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GeoClient twoGisClient = mock(GeoClient.class);
    private final GpsRouteSpoofingFilter gpsRouteSpoofingFilter = mock(GpsRouteSpoofingFilter.class);
    private final CalculateRouteService calculateRouteService = new CalculateRouteServiceImpl(twoGisClient, gpsRouteSpoofingFilter);
    private final FactWaypointsTripService factWaypointsTripService = mock(FactWaypointsTripService.class);
    private final RouteService routeService = new RouteServiceImpl(coordinateRepository,
            tripFactDistanceSender, calculateRouteService, objectMapper, routeRepository, factWaypointsTripService);
    private final CoordinateMapper coordinateMapper = new CoordinateMapperImpl();
    private final CoordinateService coordinateService = new CoordinateServiceImpl(driverRepository, coordinateRepository, coordinateMapper);

    @BeforeEach
    void beforeAll() {
        objectMapper.registerModule(new JavaTimeModule());
        when(gpsRouteSpoofingFilter.filter(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Сохранение позиции")
    void savePointInfoTest() {
        var geoWaypointDTO = Instancio.create(GeoWaypointDTO.class);
        var driver = new DriverMessageRecord(UUID.randomUUID(), Instancio.create(Boolean.class),
                true, UUID.randomUUID(), Instancio.create(Boolean.class), true);

        when(driverRepository.getByIdNotNull(any())).thenReturn(driver);

        coordinateService.savePointInfo(geoWaypointDTO, driver.getId());

        var captor = ArgumentCaptor.forClass(CoordinateRecord.class);

        verify(coordinateRepository).save(captor.capture());

        var savedCoordinate = captor.getValue();

        assertEquals(driver.getActiveTripId(), savedCoordinate.getTripId());
        assertEquals(geoWaypointDTO.getLatitude(), savedCoordinate.getLatitude());
        assertEquals(geoWaypointDTO.getLongitude(), savedCoordinate.getLongitude());
    }

    @Test
    @DisplayName("Сохранение позиции. Водитель не онлайн")
    void savePointInfo_driverOfflineTest() {
        var driver = new DriverMessageRecord(UUID.randomUUID(), Instancio.create(Boolean.class),
                false, UUID.randomUUID(), Instancio.create(Boolean.class), true);

        when(driverRepository.getByIdNotNull(any())).thenReturn(driver);

        verify(coordinateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Сохранение позиции. Водитель едет к месту посадки")
    void savePointInfo_wrongTripStatusTest() {
        var geoWaypointDTO = Instancio.create(GeoWaypointDTO.class);
        var driver = new DriverMessageRecord(UUID.randomUUID(), Instancio.create(Boolean.class),
                true, UUID.randomUUID(), Instancio.create(Boolean.class), false);

        when(driverRepository.getByIdNotNull(any())).thenReturn(driver);

        coordinateService.savePointInfo(geoWaypointDTO, driver.getId());

        verify(coordinateRepository).save(any());
    }

    @Test
    @DisplayName("Сохранение позиции. У водителя нет активной поездки")
    void savePointInfo_hasNoActiveTripTest() {
        var geoWaypointDTO = Instancio.create(GeoWaypointDTO.class);
        var driver = new DriverMessageRecord(UUID.randomUUID(), Instancio.create(Boolean.class),
                true, null, Instancio.create(Boolean.class), false);

        when(driverRepository.getByIdNotNull(any())).thenReturn(driver);

        coordinateService.savePointInfo(geoWaypointDTO, driver.getId());

        verify(coordinateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Вычисление дистанции трипа")
    void calculateRouteTest() throws JsonProcessingException {
        JSON json = JSON.valueOf("""
                [
                	{
                		"id": "53481338-c667-4f78-aef1-58e9d058841e",
                		"latitude": 56.826648,
                		"longitude": 60.612773
                	},
                	{
                		"id": "faa8c6e5-1913-4f58-8639-f3eafe550371",
                		"latitude": 56.826882,
                		"longitude": 60.604988
                	},
                	{
                		"id": "faa8c6e5-1913-4f58-8639-f3eafe550372",
                		"latitude": 56.822473,
                		"longitude": 60.608843
                	},
                	{
                		"id": "faa8c6e5-1913-4f58-8639-f3eafe550373",
                		"latitude": 56.824583,
                		"longitude": 60.613043
                	}
                ]
                """);
        var factWaypointsTrip = new FactWaypointsTripRecord(
                UUID.randomUUID(),
                UUID.randomUUID(),
                json,
        false,
                0, null);

        var expectedDistance = 1.364;
        var tripId = factWaypointsTrip.getTripId();
        var coords = List.of(
                new CoordinateRecord(UUID.randomUUID(), tripId, 56.826648, 60.612773, LocalDateTime.now().plusMinutes(1)),
                new CoordinateRecord(UUID.randomUUID(), tripId, 56.826882, 60.604988, LocalDateTime.now().plusMinutes(2)),
                new CoordinateRecord(UUID.randomUUID(), tripId, 56.822473, 60.608843, LocalDateTime.now().plusMinutes(3)),
                new CoordinateRecord(UUID.randomUUID(), tripId, 56.824583, 60.613043, LocalDateTime.now().plusMinutes(4)));

        var expectedRoute = Instancio.create(RouteDTO.class);

        when(coordinateRepository.findCoordinatesByTripId(any())).thenReturn(coords);
        when(twoGisClient.recreateRoute(any())).thenReturn(expectedRoute);

        List<RouteRecord> routeRecordList = routeService.calculateRoute(factWaypointsTrip);

        assertEquals(2, routeRecordList.size());

        for (RouteRecord routeRecord : routeRecordList) {
            if (routeRecord.getSource().equals(RouteSource.FORMULA.name())) {
                checkFormulaCalculation(factWaypointsTrip.getTripId(), routeRecord, coords, expectedDistance);
            } else {
                check2GisCalculation(factWaypointsTrip.getTripId(), routeRecord, expectedRoute);
            }
        }
    }

    @Test
    @DisplayName("Вычисление дистанции трипа через формулу")
    void calculateRouteByFormulaTest() {

        JSON json = JSON.valueOf("""
                [
                	{
                		"id": "53481338-c667-4f78-aef1-58e9d058841e",
                		"latitude": 56.826648,
                		"longitude": 60.612773
                	},
                	{
                		"id": "faa8c6e5-1913-4f58-8639-f3eafe550371",
                		"latitude": 56.826882,
                		"longitude": 60.604988
                	},
                	{
                		"id": "faa8c6e5-1913-4f58-8639-f3eafe550372",
                		"latitude": 56.822473,
                		"longitude": 60.608843
                	},
                	{
                		"id": "faa8c6e5-1913-4f58-8639-f3eafe550373",
                		"latitude": 56.824583,
                		"longitude": 60.613043
                	}
                ]
                """);
        var factWaypointsTrip = new FactWaypointsTripRecord(
                UUID.randomUUID(),
                UUID.randomUUID(),
                json,
                false,
                0, null);

        var tripId = factWaypointsTrip.getTripId();
        var coords = List.of(
                new CoordinateRecord(UUID.randomUUID(), tripId, 56.826648, 60.612773, LocalDateTime.now().plusMinutes(1)),
                new CoordinateRecord(UUID.randomUUID(), tripId, 56.826882, 60.604988, LocalDateTime.now().plusMinutes(2)),
                new CoordinateRecord(UUID.randomUUID(), tripId, 56.822473, 60.608843, LocalDateTime.now().plusMinutes(3)),
                new CoordinateRecord(UUID.randomUUID(), tripId, 56.824583, 60.613043, LocalDateTime.now().plusMinutes(4)));

        when(coordinateRepository.findCoordinatesByTripId(any())).thenReturn(coords);
        when(twoGisClient.recreateRoute(any())).thenThrow(new RuntimeException("Error"));

        routeService.calculateRoute(factWaypointsTrip);

        List<RouteRecord> routeRecordList = routeService.calculateRoute(factWaypointsTrip);

        assertEquals(1, routeRecordList.size());
    }

    @Test
    @DisplayName("Вычисление дистанции трипа без координат")
    void calculateDistance_withoutCoordsTest() {
        JSON json = null;
        var factWaypointsTrip = new FactWaypointsTripRecord(
                UUID.randomUUID(),
                UUID.randomUUID(),
                json,
                false,
                0, null);

        var coords = new LinkedList<CoordinateRecord>();
        var tripId = factWaypointsTrip.getTripId();

        when(coordinateRepository.findCoordinatesByTripId(any())).thenReturn(coords);

        routeService.calculateRoute(factWaypointsTrip);

        var mapCaptor = ArgumentCaptor.forClass(Map.class);
        var idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(tripFactDistanceSender).send(mapCaptor.capture(), idCaptor.capture());

        assertTrue(mapCaptor.getValue().isEmpty());
        assertEquals(tripId, idCaptor.getValue());
    }

    @Test
    @DisplayName("Получение маршрута")
    void getRouteTest() throws JsonProcessingException {
        var route = Instancio.create(RouteDTO.class);
        var routeJson = JSON.valueOf(objectMapper.writeValueAsString(route));
        var routeRecord = new RouteRecord(UUID.randomUUID(), UUID.randomUUID(), routeJson, RouteSource.TWO_GIS.name());

        when(routeRepository.findByTripIdAndSource(any(), any())).thenReturn(Optional.of(routeRecord));

        var actual = routeService.getRoute(UUID.randomUUID(), RouteSource.TWO_GIS);
        assertEquals(route.getDistance(), actual.getDistance());
        assertEquals(route.getTime(), actual.getTime());

        var segments = actual.getSegments();
        for (var i = 0; i < segments.size(); i++) {
            var segment = segments.get(i);
            var expected = route.getSegments().get(i);

            assertEquals(expected.getDistance(), segment.getDistance());
            assertEquals(expected.getTime(), segment.getTime());

            var points = segment.getPoints();
            for (var j = 0; j < points.size(); j++) {
                var point = points.get(j);
                var expectedPoint = expected.getPoints().get(j);

                assertEquals(expectedPoint.getLongitude(), point.getLongitude());
                assertEquals(expectedPoint.getLatitude(), point.getLatitude());
            }
        }
    }

    @Test
    @DisplayName("Получение маршрута. Не найден маршрут")
    void getRoute_notFoundTest() {
        when(routeRepository.findByTripIdAndSource(any(), any())).thenReturn(Optional.empty());

        var actual = routeService.getRoute(UUID.randomUUID(), RouteSource.TWO_GIS);
        assertNull(actual);
    }

    @Test
    @DisplayName("Создание фактического маршрута")
    void createFactRouteShouldProcessRoutesAndSetFlags() {
        JSON json = JSON.valueOf("""
                [
                	{
                		"id": "53481338-c667-4f78-aef1-58e9d058841e",
                		"latitude": 56.826648,
                		"longitude": 60.612773
                	},
                	{
                		"id": "faa8c6e5-1913-4f58-8639-f3eafe550371",
                		"latitude": 56.826882,
                		"longitude": 60.604988
                	},
                	{
                		"id": "faa8c6e5-1913-4f58-8639-f3eafe550372",
                		"latitude": 56.822473,
                		"longitude": 60.608843
                	},
                	{
                		"id": "faa8c6e5-1913-4f58-8639-f3eafe550373",
                		"latitude": 56.824583,
                		"longitude": 60.613043
                	}
                ]
                """);
        var factWaypointsTrip = new FactWaypointsTripRecord(
                UUID.randomUUID(),
                UUID.randomUUID(),
                json,
                false,
                0, null);
        List<FactWaypointsTripRecord> factTrips = List.of(factWaypointsTrip);
        RouteRecord route1 = mock(RouteRecord.class);

        RouteServiceImpl routeServiceSpy = spy(new RouteServiceImpl(coordinateRepository, tripFactDistanceSender, calculateRouteService,
                        objectMapper, routeRepository, factWaypointsTripService));

        when(factWaypointsTripService.getAllNotHandledRecords()).thenReturn(factTrips);
        doReturn(List.of(route1)).when(routeServiceSpy).calculateRoute(factWaypointsTrip);
        when(route1.getSource()).thenReturn(RouteSource.TWO_GIS.name());
        UUID tripId = UUID.randomUUID();
        when(route1.getTripId()).thenReturn(tripId);

        routeServiceSpy.createFactRoute();

        verify(factWaypointsTripService).getAllNotHandledRecords();
        verify(routeServiceSpy).calculateRoute(factWaypointsTrip);
        verify(routeRepository).insertIfNotExist(route1);
        verify(factWaypointsTripService).handleCompliteCreateExpectedRoute(any());
    }

    @Test
    @DisplayName("Проверка обработки отсутсвия записей для построения маршрута")
    void createFactRouteShouldDoNothingWhenNoUnhandledRecords() {
        when(factWaypointsTripService.getAllNotHandledRecords()).thenReturn(List.of());
        routeService.createFactRoute();

        verify(factWaypointsTripService).getAllNotHandledRecords();
        verifyNoMoreInteractions(factWaypointsTripService, routeRepository);
    }

    @Test
    @DisplayName("Проверка создания записи фактического маршрута с waypoints")
    void createFactWaypointForTripShouldSaveAndDeleteWhenCoordinatesExist() {
        UUID tripId = UUID.randomUUID();
        CoordinateRecord coord1 = new CoordinateRecord(
                UUID.randomUUID(),
                tripId,
                55.0,
                37.0,
                LocalDateTime.now()
        );
        CoordinateRecord coord2 = new CoordinateRecord(
                UUID.randomUUID(),
                tripId,
                56.0,
                37.0,
                LocalDateTime.now()
        );
        CoordinateRecord coord3 = new CoordinateRecord(
                UUID.randomUUID(),
                tripId,
                57.0,
                37.0,
                LocalDateTime.now()
        );
        List<CoordinateRecord> coords = List.of(coord1, coord2, coord3);
        when(coordinateRepository.findCoordinatesByTripId(tripId)).thenReturn(coords);

        String jsonString = """
                [{"latitude":55.0,"longitude":37.0},{"latitude":56.0,"longitude":37.0},{"latitude":57.0,"longitude":37.0}]""";

        routeService.createFactWaypointForTrip(tripId);

        ArgumentCaptor<FactWaypointsTripRecord> captor = ArgumentCaptor.forClass(FactWaypointsTripRecord.class);
        verify(factWaypointsTripService).saveFactWaypointsTrip(captor.capture());
        FactWaypointsTripRecord saved = captor.getValue();
        assertEquals(tripId, saved.getTripId());
        assertEquals(JSON.valueOf(jsonString), saved.getWaypoints());
        assertFalse(saved.getIsTrackCreated());

        verify(coordinateRepository).deleteAllByTripId(tripId);
    }

    @Test
    @DisplayName("Проверка на создания записи фактического маршрута с waypoints, если координаты отсутствуют")
    void createFactWaypointForTripShouldDoNothingWhenNoCoordinates() {
        UUID tripId = UUID.randomUUID();

        when(coordinateRepository.findCoordinatesByTripId(tripId)).thenReturn(List.of()); // пусто

        routeService.createFactWaypointForTrip(tripId);

        verify(coordinateRepository).findCoordinatesByTripId(tripId);
        verifyNoMoreInteractions(coordinateRepository);
    }

    private void checkFormulaCalculation(UUID tripId, RouteRecord actualRoute, List<CoordinateRecord> coords, double expectedDistance) throws JsonProcessingException {
        assertEquals(tripId, actualRoute.getTripId());

        var routeJson = actualRoute.getCoords();
        var route = objectMapper.readValue(routeJson.toString(), RouteDTO.class);
        assertTrue(Math.abs(route.getDistance() - expectedDistance) < 0.001);

        var segments = route.getSegments();
        assertEquals(1, segments.size());

        for (SegmentDTO segment : segments) {
            assertTrue(Math.abs(segment.getDistance() - expectedDistance) < 0.001);

            var points = segment.getPoints();
            assertEquals(coords.size(), points.size());
            for (var j = 0; j < points.size(); j++) {
                var expectedPoint = coords.get(j);
                var point = points.get(j);
                assertEquals(expectedPoint.getLongitude(), point.getLongitude());
                assertEquals(expectedPoint.getLatitude(), point.getLatitude());
            }
        }

        var distanceCaptor = ArgumentCaptor.forClass(Map.class);
        var idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(tripFactDistanceSender).send(distanceCaptor.capture(), idCaptor.capture());

        assertEquals(route.getDistance(), ((RouteDTO) distanceCaptor.getValue().get(RouteSource.FORMULA)).getDistance());
        assertEquals(tripId, idCaptor.getValue());
    }

    private void check2GisCalculation(UUID tripId, RouteRecord actualRoute, RouteDTO expectedRoute) throws JsonProcessingException {
        assertEquals(tripId, actualRoute.getTripId());

        var routeJson = actualRoute.getCoords();
        var route = objectMapper.readValue(routeJson.toString(), RouteDTO.class);
        assertEquals(expectedRoute.getDistance(), route.getDistance());

        var expectedSegments = expectedRoute.getSegments();
        var segments = route.getSegments();
        assertEquals(expectedSegments.size(), segments.size());

        for (var i = 0; i < segments.size(); i++) {
            var expectedSegment = expectedSegments.get(i);
            var segment = segments.get(i);
            assertEquals(expectedSegment.getDistance(), segment.getDistance());

            var expectedPoints = expectedSegment.getPoints();
            var points = segment.getPoints();
            assertEquals(expectedPoints.size(), points.size());
            for (var j = 0; j < points.size(); j++) {
                var expectedPoint = expectedPoints.get(j);
                var point = points.get(j);
                assertEquals(expectedPoint.getLongitude(), point.getLongitude());
                assertEquals(expectedPoint.getLatitude(), point.getLatitude());
            }
        }

        var distanceCaptor = ArgumentCaptor.forClass(Map.class);
        var idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(tripFactDistanceSender).send(distanceCaptor.capture(), idCaptor.capture());

        assertEquals(route.getDistance(), ((RouteDTO) distanceCaptor.getValue().get(RouteSource.TWO_GIS)).getDistance());
        assertEquals(tripId, idCaptor.getValue());
    }
}
