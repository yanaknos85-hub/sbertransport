package ru.sber.transport.driver_track.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.driver_track.database.driver_track.tables.records.FactWaypointsTripRecord;
import ru.sber.transport.driver_track.database.driver_track.tables.records.RouteRecord;
import ru.sber.transport.driver_track.repository.FactWaypointsTripRepository;
import ru.sber.transport.driver_track.service.FactWaypointsTripService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static ru.sber.transport.driver_track.dto.RouteSource.FORMULA;
import static ru.sber.transport.driver_track.dto.RouteSource.TWO_GIS;

@UnitTest
@IsolatedTest
@Feature("app_platform_driver_track")
@DisplayName("Проверка сервиса по работе с координатами планового маршрута")
class FactWaypointsTripServiceImplTest {

    private final FactWaypointsTripRepository factWaypointsTripRepository = mock(FactWaypointsTripRepository.class);
    private final FactWaypointsTripService factWaypointsTripService = new FactWaypointsTripServiceImpl(factWaypointsTripRepository);

    @Test
    @DisplayName("Проверка сохранения координат, если wayponts не существует в БД")
    void saveFactWaypointsTripShouldSaveWhenListNotEmpty() {
        FactWaypointsTripRecord factWaypointsTripRecord = mock(FactWaypointsTripRecord.class);

        factWaypointsTripService.saveFactWaypointsTrip(factWaypointsTripRecord);

        verify(factWaypointsTripRepository).save(factWaypointsTripRecord);
    }

    @Test
    @DisplayName("Проверка получения всех необработанных записей")
    void getAllNotHandledRecordsReturnsRecordsFromRepository() {
        List<FactWaypointsTripRecord> mockList = new ArrayList<>();
        var record1 = mock(FactWaypointsTripRecord.class);
        var record2 = mock(FactWaypointsTripRecord.class);
        mockList.add(record1);
        mockList.add(record2);

        when(factWaypointsTripRepository.getAllNotHandledRecords()).thenReturn(mockList);

        var result = factWaypointsTripService.getAllNotHandledRecords();

        verify(factWaypointsTripRepository).getAllNotHandledRecords();
        assertEquals(mockList, result);
    }

    @Test
    @DisplayName("Проверка максимального количества попыток генерации фактического маршрута")
    void setMaxAttemptsCountToWrongRouteGenerationTest() {
        factWaypointsTripService.setMaxAttemptsCountToWrongRouteGeneration(List.of(UUID.randomUUID(), UUID.randomUUID()));
        verify(factWaypointsTripRepository).setMaxAttemptsCountToWrongRouteGeneration(anyList());
    }

    @Test
    @DisplayName("Проверка корректного обновления количества попыток генерации фактического маршрута")
    void handleCompliteCreateFactRouteTest() {
        var routeRecord1 = new RouteRecord(UUID.randomUUID(), UUID.randomUUID(), null, FORMULA.name());
        var routeRecord2 = new RouteRecord(UUID.fromString("e1c5283d-a668-4150-9031-7485589991ff"), routeRecord1.getTripId(), null, TWO_GIS.name());
        var routeRecord3 = new RouteRecord(UUID.fromString("f175cd41-08b0-4de1-9ee1-7f76746bf8ab"), UUID.randomUUID(), null, TWO_GIS.name());
        var routeRecord4 = new RouteRecord(UUID.randomUUID(), routeRecord3.getTripId(), null, FORMULA.name());
        var routeRecord5 = new RouteRecord(UUID.fromString("17f3e005-d917-469e-8fd6-d96acc0d6c5a"), UUID.randomUUID(), null, FORMULA.name());

        factWaypointsTripService.handleCompliteCreateExpectedRoute(List.of(routeRecord1, routeRecord2, routeRecord3, routeRecord4, routeRecord5));

        Map<UUID, RouteRecord> grouped = new HashMap<>();
        grouped.put(routeRecord2.getTripId(), routeRecord2);
        grouped.put(routeRecord3.getTripId(), routeRecord3);
        grouped.put(routeRecord5.getTripId(), routeRecord5);

        verify(factWaypointsTripRepository)
                .handleCompliteCreateExpectedRoute(
                        argThat(actual ->
                                new HashSet<>(actual).equals(new HashSet<>(grouped.values()))
                        )
                );
    }

}