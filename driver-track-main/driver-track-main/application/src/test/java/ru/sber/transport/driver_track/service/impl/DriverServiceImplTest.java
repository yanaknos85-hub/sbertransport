package ru.sber.transport.driver_track.service.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.dispatcher.messages.DriverMessage;
import ru.sber.transport.driver_track.database.driver_track.tables.records.DriverMessageRecord;
import ru.sber.transport.driver_track.mapper.DriverMapper;
import ru.sber.transport.driver_track.mapper.DriverMapperImpl;
import ru.sber.transport.driver_track.repository.DriverRepository;
import ru.sber.transport.driver_track.service.DriverService;
import ru.sber.transport.driver_track.service.RouteService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_driver_track")
@DisplayName("Проверка сервиса водителей")
public class DriverServiceImplTest {

    private final DriverRepository driverRepository = mock(DriverRepository.class);
    private final RouteService routeService = mock(RouteService.class);
    private final DriverMapper driverMapper = new DriverMapperImpl();
    private final DriverService driverService = new DriverServiceImpl(driverRepository, driverMapper, routeService);

    @Test
    @DisplayName("Проверка сохранения")
    void saveTest() {
        var message = Instancio.create(DriverMessage.class);

        when(driverRepository.findById(any())).thenReturn(Optional.empty());

        driverService.save(message);

        var captor = ArgumentCaptor.forClass(DriverMessageRecord.class);
        verify(driverRepository).save(captor.capture());

        var actual = captor.getValue();
        assertEquals(message.getId(), actual.getId());
        assertEquals(message.active(), actual.getActive());
        assertEquals(message.online(), actual.getOnline());
        assertEquals(message.activeTripId(), actual.getActiveTripId());
        assertEquals(message.consent(), actual.getConsent());

        verify(routeService, never()).calculateRoute(any());
    }

    @Test
    @DisplayName("Проверка обновления")
    void updateTest() {
        var message = Instancio.of(DriverMessage.class)
                .set(Select.field(DriverMessage::serving), true)
                .create();
        var driver = new DriverMessageRecord(UUID.randomUUID(), Instancio.create(Boolean.class),
                Instancio.create(Boolean.class), UUID.randomUUID(), Instancio.create(Boolean.class), true);

        when(driverRepository.findById(any())).thenReturn(Optional.of(driver));

        driverService.save(message);

        var captor = ArgumentCaptor.forClass(DriverMessageRecord.class);
        verify(driverRepository).save(captor.capture());

        var actual = captor.getValue();
        assertEquals(driver.getId(), actual.getId());
        assertEquals(message.active(), actual.getActive());
        assertEquals(message.online(), actual.getOnline());
        assertEquals(message.activeTripId(), actual.getActiveTripId());
        assertEquals(message.consent(), actual.getConsent());

        verify(routeService, never()).calculateRoute(any());
    }

    @Test
    @DisplayName("Проверка обновления. Завершение поездки")
    void update_sentTest() {
        var message = Instancio.of(DriverMessage.class)
                .set(Select.field(DriverMessage::serving), false)
                .create();
        var driver = new DriverMessageRecord(UUID.randomUUID(), Instancio.create(Boolean.class),
                Instancio.create(Boolean.class), UUID.randomUUID(), Instancio.create(Boolean.class), true);

        when(driverRepository.findById(any())).thenReturn(Optional.of(driver));

        driverService.save(message);

        var captor = ArgumentCaptor.forClass(DriverMessageRecord.class);
        verify(driverRepository).save(captor.capture());

        var actual = captor.getValue();
        assertEquals(driver.getId(), actual.getId());
        assertEquals(message.active(), actual.getActive());
        assertEquals(message.online(), actual.getOnline());
        assertEquals(message.activeTripId(), actual.getActiveTripId());
        assertEquals(message.consent(), actual.getConsent());

        verify(routeService).createFactWaypointForTrip(any());
    }
}
