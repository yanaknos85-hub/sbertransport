package ru.sber.transport.trips.cargo.messaging.senders.dispatcher.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketSession;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.dispatcher.messages.DriverMessage;
import ru.sber.transport.dispatcher.messages.Source;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.trips.cargo.business.dto.DriverOnMapDto;
import ru.sber.transport.trips.cargo.business.model.*;
import ru.sber.transport.trips.cargo.business.providers.TripProvider;
import ru.sber.transport.trips.cargo.messaging.ChannelType;
import ru.sber.transport.trips.cargo.messaging.providers.DispatcherProvider;
import ru.sber.transport.trips.cargo.messaging.providers.ShiftProvider;
import ru.sber.transport.trips.cargo.messaging.providers.VehicleProvider;
import ru.sber.transport.trips.cargo.messaging.senders.dispatcher.DriverSender;
import ru.sber.transport.trips.cargo.providers.driver.mapper.DriverMapper;
import ru.sber.transport.trips.cargo.providers.driver.mapper.DriverMapperImpl;
import ru.sber.transport.trips.cargo.providers.shift.mapper.ShiftMapper;
import ru.sber.transport.trips.cargo.providers.shift.mapper.ShiftMapperImpl;
import ru.sber.transport.trips.cargo.providers.vehicle.mapper.VehicleMapper;
import ru.sber.transport.trips.cargo.providers.vehicle.mapper.VehicleMapperImpl;
import ru.sber.transport.web_socket.handlers.WebSocketHandler;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_trips_cargo")
@DisplayName("Проверка отправителя водителей")
@SpringBootTest
@ActiveProfiles({"test", "kafka", "kafka-ssl"})
public class DriverSenderImplTest extends KafkaTest {

    private final OutputBridge driverOutput = mock(OutputBridge.class);
    private final DispatcherProvider dispatcherProvider = mock(DispatcherProvider.class);
    private final ShiftProvider shiftProvider = mock(ShiftProvider.class);
    private final VehicleProvider vehicleProvider = mock(VehicleProvider.class);
    private final DriverMapper driverMapper = new DriverMapperImpl();
    private final ShiftMapper shiftMapper = new ShiftMapperImpl();
    private final VehicleMapper vehicleMapper = new VehicleMapperImpl();
    private final TripProvider tripProvider = mock(TripProvider.class);

    private final ObjectProvider<OutputBridge> objectProvider = new ObjectProvider<>() {
        @Override
        public OutputBridge getObject(Object... args) throws BeansException {
            return driverOutput;
        }

        @Override
        public OutputBridge getIfAvailable() throws BeansException {
            return driverOutput;
        }

        @Override
        public OutputBridge getIfUnique() throws BeansException {
            return driverOutput;
        }

        @Override
        public OutputBridge getObject() throws BeansException {
            return driverOutput;
        }
    };

    private final DriverSender driverSender = new DriverSenderImpl(objectProvider, objectProvider, driverMapper, shiftMapper,
            dispatcherProvider, shiftProvider);

    private final DriverSender driverSenderSpy = spy(driverSender);

    @Test
    @DisplayName("Проверка отправки по кафке")
    void test_kafka() {
        var driver = Instancio.create(Driver.class);

        driverSender.send(driver);

        assertKafka(driver);
    }

    @Test
    @DisplayName("Проверка отправки по сокетам")
    void test_socket() throws NoSuchFieldException, IllegalAccessException {
        var dispatcher = Instancio.create(Dispatcher.class);
        var shift = Instancio.create(Shift.class);
        var vehicle = Instancio.create(Vehicle.class);
        var driver = Instancio.of(Driver.class)
                .set(Select.field(Driver::isOnline), true)
                .create();
        var trip = Instancio.create(Trip.class);

        var sessions = new HashMap<UUID, WebSocketSession>();
        sessions.put(dispatcher.getId(), null);

        var field = WebSocketHandler.class.getDeclaredField("authorizedSessions");
        field.setAccessible(true);
        field.set(driverSenderSpy, sessions);

        Mockito.when(dispatcherProvider.findAllByContractorIdAndIdIn(any(), any())).thenReturn(List.of(dispatcher));
        Mockito.when(shiftProvider.get(any())).thenReturn(Optional.of(shift));
        Mockito.when(vehicleProvider.get(any())).thenReturn(Optional.of(vehicle));
        when(tripProvider.get(any())).thenReturn(Optional.of(trip));
        doCallRealMethod().when(driverSenderSpy).send(any(), any());
        doNothing().when((WebSocketHandler<DriverOnMapDto>) driverSenderSpy).send(any(), any());

        driverSenderSpy.send(driver, ChannelType.WEB_SOCKET);

        assertSocket(driver, shift, vehicle, dispatcher, trip);
    }

    @Test
    @DisplayName("Проверка отправки по обоим каналам")
    void test_both() throws NoSuchFieldException, IllegalAccessException {
        var driver = Instancio.of(Driver.class)
                .set(Select.field(Driver::isOnline), true)
                .create();
        var dispatcher = Instancio.create(Dispatcher.class);
        var shift = Instancio.create(Shift.class);
        var vehicle = Instancio.create(Vehicle.class);
        var trip = Instancio.create(Trip.class);

        var sessions = new HashMap<UUID, WebSocketSession>();
        sessions.put(dispatcher.getId(), null);

        var field = WebSocketHandler.class.getDeclaredField("authorizedSessions");
        field.setAccessible(true);
        field.set(driverSenderSpy, sessions);

        Mockito.when(dispatcherProvider.findAllByContractorIdAndIdIn(any(), any())).thenReturn(List.of(dispatcher));
        Mockito.when(shiftProvider.get(any())).thenReturn(Optional.of(shift));
        Mockito.when(vehicleProvider.get(any())).thenReturn(Optional.of(vehicle));
        when(tripProvider.get(any())).thenReturn(Optional.of(trip));
        doCallRealMethod().when(driverSenderSpy).send(any(), any());
        doNothing().when((WebSocketHandler<DriverOnMapDto>) driverSenderSpy).send(any(), any());

        driverSenderSpy.send(driver, ChannelType.BOTH);

        assertKafka(driver);
        assertSocket(driver, shift, vehicle, dispatcher, trip);
    }

    private void assertKafka(Driver driver) {
        var driverMessageCaptor = ArgumentCaptor.forClass(DriverMessage.class);
        var additionalHeadersCaptor = ArgumentCaptor.forClass(Map.class);

        verify(driverOutput, times(2)).send(driverMessageCaptor.capture(), additionalHeadersCaptor.capture());

        var driverMessageActual = driverMessageCaptor.getValue();
        assertEquals(driver.getId(), driverMessageActual.getId());
        assertEquals(driver.getLastName(), driverMessageActual.lastName());
        assertEquals(driver.getFirstName(), driverMessageActual.firstName());
        assertEquals(driver.getPatronymic(), driverMessageActual.patronymic());
        assertEquals(driver.getPassport(), driverMessageActual.passport());
        assertEquals(driver.getContractorId(), driverMessageActual.contractorId());
        assertEquals(driver.isActive(), driverMessageActual.active());
        assertEquals(driver.getRating(), driverMessageActual.rating());
        assertEquals(driver.getDriverLicenseNumber(), driverMessageActual.driverLicenseNumber());
        assertEquals(driver.getCargoLicenceNumber(), driverMessageActual.cargoLicenceNumber());
        assertEquals(driver.getServiceLicenseNumber(), driverMessageActual.serviceLicenseNumber());
        assertEquals(driver.getLatitude(), driverMessageActual.latitude());
        assertEquals(driver.getLongitude(), driverMessageActual.longitude());
        assertEquals(driver.getPointTime(), driverMessageActual.pointTime());
        assertEquals(driver.getTimeZone(), driverMessageActual.timeZone());
        assertEquals(driver.isServing(), driverMessageActual.serving());
        assertEquals(driver.isOnline(), driverMessageActual.online());
        assertEquals(driver.getActiveTripId(), driverMessageActual.activeTripId());
        assertEquals(driver.getShiftId(), driverMessageActual.activeShiftId());
        assertEquals(driver.getExperience(), driverMessageActual.experience());
        assertEquals(driver.getContactPhone(), driverMessageActual.contactPhone());
        assertEquals(driver.getEmail(), driverMessageActual.email());
        assertEquals(driver.getHumanReadableId(), driverMessageActual.humanReadableId());

        var additionalHeadersActual = (Map<String, Object>) additionalHeadersCaptor.getValue();
        assertEquals(Source.TRIPS_CARGO.name(), additionalHeadersActual.get("source"));
    }

    private void assertSocket(Driver driver, Shift shift, Vehicle vehicle, Dispatcher dispatcher, Trip trip) {
        var driverMessageCaptor = ArgumentCaptor.forClass(DriverOnMapDto.class);
        var receiverIdCaptor = ArgumentCaptor.forClass(UUID.class);

        verify((WebSocketHandler<DriverOnMapDto>) driverSenderSpy, times(1)).send(receiverIdCaptor.capture(), driverMessageCaptor.capture());

        var driverMessageActual = driverMessageCaptor.getValue();
        assertEquals(driver.getId(), driverMessageActual.getId());
        assertEquals(driver.getHumanReadableId(), driverMessageActual.getHumanReadableId());
        assertEquals(driver.getLatitude(), driverMessageActual.getLatitude());
        assertEquals(driver.getLongitude(), driverMessageActual.getLongitude());
        assertEquals(driver.getAzimuth(), driverMessageActual.getAzimuth());
        assertEquals(driver.isOnline(), driverMessageActual.getOnline());

        var currentShiftActual = driverMessageActual.getCurrentShift();
        assertEquals(shift.getEndDate(), currentShiftActual.getEndDate());

        var receiverIdActual = receiverIdCaptor.getValue();
        assertEquals(dispatcher.getId(), receiverIdActual);
    }
}
