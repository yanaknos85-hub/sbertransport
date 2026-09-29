package ru.sber.transport.driver_track.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.driver_track.database.driver_track.tables.records.ExpectedRouteRecord;
import ru.sber.transport.driver_track.database.driver_track.tables.records.ExpectedWaypointsTripRecord;
import ru.sber.transport.driver_track.repository.ExpectedWaypointsTripRepository;
import ru.sber.transport.driver_track.service.CoordinateService;
import ru.sber.transport.driver_track.service.ExpectedWaypointsTripService;
import ru.sber.transport.trip.message.TripMessage;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static ru.sber.transport.driver_track.database.driver_track.Tables.EXPECTED_WAYPOINTS_TRIP;
import static ru.sber.transport.driver_track.dto.RouteSource.FORMULA;
import static ru.sber.transport.driver_track.dto.RouteSource.TWO_GIS;

@UnitTest
@IsolatedTest
@Feature("app_platform_driver_track")
@DisplayName("Проверка сервиса по работе с координатами планового маршрута")
class ExpectedWaypointsTripServiceImplTest {

    private final ExpectedWaypointsTripRepository expectedWaypointsTripRepository = mock(ExpectedWaypointsTripRepository.class);
    private final DSLContext dslContext = mock(DSLContext.class);
    private final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private final CoordinateService coordinateService = mock(CoordinateService.class);
    private final ExpectedWaypointsTripService expectedWaypointsTripService = new ExpectedWaypointsTripServiceImpl(dslContext, objectMapper, expectedWaypointsTripRepository);

    @Test
    @DisplayName("Проверка не сохранения координат, если координаты не существуют")
    void saveNullTest(){

        expectedWaypointsTripService.save(null);

        verifyNoInteractions(expectedWaypointsTripRepository, dslContext, coordinateService);
    }

    @Test
    @DisplayName("Проверка не сохранения координат, если wayponts уже есть в БД")
    void saveShouldNotSaveWhenWaypointExistsTest() {
        TripMessage message = mock(TripMessage.class);
        UUID tripId = UUID.randomUUID();

        when(message.getId()).thenReturn(tripId);
        when(expectedWaypointsTripRepository.findByTripId(tripId)).thenReturn(Optional.of(mock(ExpectedWaypointsTripRecord.class)));

        expectedWaypointsTripService.save(message);

        verify(expectedWaypointsTripRepository).findByTripId(tripId);
        verifyNoMoreInteractions(expectedWaypointsTripRepository);
        verifyNoInteractions(dslContext, coordinateService);
    }

    @Test
    @DisplayName("Проверка сохранения координат, если wayponts не существует в БД")
    void saveShouldCreateAndSaveRecordWhenWaypointNotExists() {
        TripMessage tripMessage = mock(TripMessage.class);
        UUID tripId = UUID.randomUUID();

        when(tripMessage.getId()).thenReturn(tripId);
        when(tripMessage.getWaypoints()).thenReturn(List.of(Map.of("latitude", 53.555, "longitude", 54.555)));
        when(expectedWaypointsTripRepository.findByTripId(tripId)).thenReturn(Optional.empty());

        ExpectedWaypointsTripRecord mockRecord = mock(ExpectedWaypointsTripRecord.class);

        when(expectedWaypointsTripRepository.table()).thenReturn(EXPECTED_WAYPOINTS_TRIP);
        when(dslContext.newRecord(EXPECTED_WAYPOINTS_TRIP)).thenReturn(mockRecord);

        expectedWaypointsTripService.save(tripMessage);

        verify(expectedWaypointsTripRepository).findByTripId(tripId);
        verify(expectedWaypointsTripRepository).table();
        verify(dslContext).newRecord(EXPECTED_WAYPOINTS_TRIP);
        verify(expectedWaypointsTripRepository).save(mockRecord);

        verify(mockRecord).setId(any(UUID.class));
        verify(mockRecord).setTripId(tripId);
        verify(mockRecord).setIsTrackCreated(false);
    }

    @Test
    @DisplayName("Проверка получения всех необработанных записей")
    void getAllNotHandledRecords_returnsRecordsFromRepository() {
        var record1 = mock(ExpectedWaypointsTripRecord.class);
        var record2 = mock(ExpectedWaypointsTripRecord.class);
        List<ExpectedWaypointsTripRecord> mockList = new ArrayList<>(List.of(record1, record2));

        when(expectedWaypointsTripRepository.getAllNotHandledRecords()).thenReturn(mockList);

        var result = expectedWaypointsTripService.getAllNotHandledRecords();

        verify(expectedWaypointsTripRepository).getAllNotHandledRecords();
        assertEquals(mockList, result);
    }

    @Test
    @DisplayName("Проверка максимального количества попыток генерации планового маршрута")
    void setMaxAttemptsCountToWrongRouteGenerationTest() {
        expectedWaypointsTripService.setMaxAttemptsCountToWrongRouteGeneration(List.of(UUID.randomUUID(), UUID.randomUUID()));
        verify(expectedWaypointsTripRepository).setMaxAttemptsCountToWrongRouteGeneration(anyList());
    }

    @Test
    @DisplayName("Проверка корректного обновления количества попыток генерации планового маршрута")
    void handleCompliteCreateExpectedRouteTest() {
        var expectedRouteRecord1 = new ExpectedRouteRecord(UUID.randomUUID(), UUID.randomUUID(), null, FORMULA.name());
        var expectedRouteRecord2 = new ExpectedRouteRecord(UUID.fromString("e1c5283d-a668-4150-9031-7485589991ff"), expectedRouteRecord1.getTripId(), null, TWO_GIS.name());
        var expectedRouteRecord3 = new ExpectedRouteRecord(UUID.fromString("f175cd41-08b0-4de1-9ee1-7f76746bf8ab"), UUID.randomUUID(), null, TWO_GIS.name());
        var expectedRouteRecord4 = new ExpectedRouteRecord(UUID.randomUUID(), expectedRouteRecord3.getTripId(), null, FORMULA.name());
        var expectedRouteRecord5 = new ExpectedRouteRecord(UUID.fromString("17f3e005-d917-469e-8fd6-d96acc0d6c5a"), UUID.randomUUID(), null, FORMULA.name());

        expectedWaypointsTripService.handleCompliteCreateExpectedRoute(List.of(expectedRouteRecord1, expectedRouteRecord2, expectedRouteRecord3, expectedRouteRecord4, expectedRouteRecord5));

        Map<UUID, ExpectedRouteRecord> grouped = new HashMap<>();
        grouped.put(expectedRouteRecord2.getTripId(), expectedRouteRecord2);
        grouped.put(expectedRouteRecord3.getTripId(), expectedRouteRecord3);
        grouped.put(expectedRouteRecord5.getTripId(), expectedRouteRecord5);

        verify(expectedWaypointsTripRepository)
                .handleCompliteCreateExpectedRoute(
                        argThat(actual ->
                                new HashSet<>(actual).equals(new HashSet<>(grouped.values()))
                        )
                );


    }


}