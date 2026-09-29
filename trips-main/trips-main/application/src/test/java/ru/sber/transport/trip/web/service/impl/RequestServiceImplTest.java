package ru.sber.transport.trip.web.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.trip.business.model.Driver;
import ru.sber.transport.trip.business.model.Trip;
import ru.sber.transport.trip.business.providers.TripProvider;
import ru.sber.transport.trip.messaging.providers.DriverProvider;
import ru.sber.transport.trip.providers.history.trip.TripHistoryProvider;
import ru.sber.transport.trip.web.service.RequestService;
import ru.sber.transport.trip.web.service.UpdateTripService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Проверка сервиса заявок")
@UnitTest
@IsolatedTest
@Feature("app_platform_trips")
public class RequestServiceImplTest {

    TripProvider tripProvider = mock(TripProvider.class);
    DriverProvider driverProvider = mock(DriverProvider.class);
    UpdateTripService updateTripService = mock(UpdateTripService.class);
    TripHistoryProvider tripHistoryProvider = mock(TripHistoryProvider.class);
    RequestService service = new RequestServiceImpl(tripProvider,driverProvider,updateTripService, List.of(),
            null,tripHistoryProvider);

    @DisplayName("Обновление фактического расстояния. Ожидаемое расстояние null")
    @Test
    void test_updateFinal_expectedDistanceNull(){
        var trip = new Trip();
        var driver = new Driver();

        when(tripProvider.get(any())).thenReturn(Optional.of(trip));
        when(driverProvider.get(any())).thenReturn(Optional.of(driver));

        var distances = Map.of("QWE", 1d);

        service.updateFinal(trip.getId(), distances);

        verify(tripProvider, never()).updateFactData(any());
    }

    @DisplayName("Обновление фактического расстояния. Ожидаемое расстояние null, но есть расстояние по формуле")
    @Test
    void test_updateFinal_expectedDistanceNull_containsFormula(){
        var trip = new Trip();
        var driver = new Driver();

        when(tripProvider.get(any())).thenReturn(Optional.of(trip));
        when(driverProvider.get(any())).thenReturn(Optional.of(driver));

        var distances = Map.of("FORMULA", 1d);

        service.updateFinal(trip.getId(), distances);

        var tripCaptor = ArgumentCaptor.forClass(Trip.class);
        verify(tripProvider).updateFactData(tripCaptor.capture());
        assertEquals(1d, tripCaptor.getValue().getFactDistance());
    }

    @DisplayName("Обновление фактического расстояния. Текущий факт null")
    @Test
    void test_updateFinal(){
        var trip = new Trip();
        trip.setExpectedDistance(10d);
        var driver = new Driver();

        when(tripProvider.get(any())).thenReturn(Optional.of(trip));
        when(driverProvider.get(any())).thenReturn(Optional.of(driver));

        var distances = Map.of("FORMULA", 8d, "TWO_GIS", 9d);

        service.updateFinal(trip.getId(), distances);

        var tripCaptor = ArgumentCaptor.forClass(Trip.class);
        verify(tripProvider).updateFactData(tripCaptor.capture());
        assertEquals(9d, tripCaptor.getValue().getFactDistance());
    }

    @DisplayName("Обновление фактического расстояния. Значение дальше от планового, чем текущий факт")
    @Test
    void test_updateFinal_curFactIsCloserToExpected(){
        var trip = new Trip();
        trip.setExpectedDistance(10d);
        trip.setFactDistance(9d);
        var driver = new Driver();

        when(tripProvider.get(any())).thenReturn(Optional.of(trip));
        when(driverProvider.get(any())).thenReturn(Optional.of(driver));

        var distances = Map.of("FORMULA", 8d, "TWO_GIS", 7d);

        service.updateFinal(trip.getId(), distances);

        verify(tripProvider, never()).updateFactData(any());
    }
}
