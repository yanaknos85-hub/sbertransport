package ru.sber.transport.trips.cargo.messaging.processor;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.dispatcher.messages.ShiftMessage;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.trips.cargo.business.dto.ShiftOperationType;
import ru.sber.transport.trips.cargo.business.model.Driver;
import ru.sber.transport.trips.cargo.business.model.Shift;
import ru.sber.transport.trips.cargo.business.model.Trip;
import ru.sber.transport.trips.cargo.business.model.TripStatus;
import ru.sber.transport.trips.cargo.business.providers.TripProvider;
import ru.sber.transport.trips.cargo.message.EwbMessage;
import ru.sber.transport.trips.cargo.messaging.ChannelType;
import ru.sber.transport.trips.cargo.messaging.processor.impl.ShiftProcessorImpl;
import ru.sber.transport.trips.cargo.messaging.providers.DriverProvider;
import ru.sber.transport.trips.cargo.messaging.providers.ShiftProvider;
import ru.sber.transport.trips.cargo.messaging.senders.dispatcher.DriverSender;
import ru.sber.transport.trips.cargo.messaging.senders.dispatcher.ShiftSender;
import ru.sber.transport.trips.cargo.messaging.senders.dispatcher.TripSender;
import ru.sber.transport.trips.cargo.providers.history.trip.TripHistoryProvider;
import ru.sber.transport.trips.cargo.providers.shift.mapper.ShiftMapper;
import ru.sber.transport.trips.cargo.web.service.ShiftService;
import ru.sber.transport.trips.cargo.web.service.TripService;
import ru.sber.transport.trips.cargo.web.service.UpdateTripService;
import ru.sber.transport.trips.cargo.web.service.VerificationService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Проверка процессора смен")
@UnitTest
@IsolatedTest
@Feature("app_platform_trips_cargo")
@ActiveProfiles({"test", "kafka", "kafka-ssl"})
class ShiftProcessorTest extends KafkaTest {

    private ShiftProcessorImpl shiftProcessor;

    @Test
    void shiftInputShiftNotExistTest() throws ExecutionException, InterruptedException {
        var shiftProvider = mock(ShiftProvider.class);

        var shiftMapper = mock(ShiftMapper.class);

        var driverProvider = mock(DriverProvider.class);

        var tripProvider = mock(TripProvider.class);

        var updateTripService = mock(UpdateTripService.class);

        var verificationService = mock(VerificationService.class);

        var shiftSender = mock(ShiftSender.class);

        var driverSender = mock(DriverSender.class);

        var tripSender = mock(TripSender.class);

        var message = Instancio.of(ShiftMessage.class).create();

        var driver = Instancio.of(Driver.class).create();

        var tripHistoryProvider = mock(TripHistoryProvider.class);

        var shiftService = mock(ShiftService.class);

        var tripService = mock(TripService.class);

        shiftProcessor = new ShiftProcessorImpl(shiftProvider, shiftMapper, tripProvider, updateTripService, tripSender, shiftSender, driverSender, verificationService, driverProvider, tripHistoryProvider, shiftService, tripService);

        when(shiftProvider.get(any())).thenReturn(Optional.empty());

        var future = shiftProcessor.processShift(message, driver);

        Assertions.assertEquals(1, future.get().intValue());
    }

    @Test
    void shiftInputShiftExistsAndInactiveTest() throws ExecutionException, InterruptedException {
        var shiftProvider = mock(ShiftProvider.class);

        var shiftMapper = mock(ShiftMapper.class);

        var driverProvider = mock(DriverProvider.class);

        var tripProvider = mock(TripProvider.class);

        var updateTripService = mock(UpdateTripService.class);

        var verificationService = mock(VerificationService.class);

        var shiftSender = mock(ShiftSender.class);

        var driverSender = mock(DriverSender.class);

        var tripSender = mock(TripSender.class);

        var tripHistoryProvider = mock(TripHistoryProvider.class);

        var message = Instancio.of(ShiftMessage.class).create();

        var driver = Instancio.of(Driver.class).create();

        var shiftService = mock(ShiftService.class);

        var tripService = mock(TripService.class);

        shiftProcessor = new ShiftProcessorImpl(shiftProvider, shiftMapper, tripProvider, updateTripService, tripSender, shiftSender, driverSender, verificationService, driverProvider, tripHistoryProvider, shiftService, tripService);

        when(driverProvider.get(any())).thenReturn(Optional.of(driver));
        when(shiftProvider.get(any())).thenReturn(Optional.of(Instancio.of(Shift.class)
                .set(Select.field(Shift::getDriverId), driver.getId())
                .set(Select.field(Shift::isActive), false)
                .create()));
        when(shiftMapper.toModel(any(ShiftMessage.class))).thenReturn(Instancio.of(Shift.class).create());
        when(tripProvider.findAllByPlannedShiftId(any())).thenReturn(List.of(Instancio.of(Trip.class).create()));

        var future = shiftProcessor.processShift(message, driver);

        Assertions.assertEquals(1, future.get().intValue());
    }

    @Test
    void shiftInputShiftExistsAndActiveAndShiftMessageIsNotDeletedTest() throws ExecutionException, InterruptedException {
        var shiftProvider = mock(ShiftProvider.class);

        var shiftMapper = mock(ShiftMapper.class);

        var driverProvider = mock(DriverProvider.class);

        var tripProvider = mock(TripProvider.class);

        var updateTripService = mock(UpdateTripService.class);

        var verificationService = mock(VerificationService.class);

        var shiftSender = mock(ShiftSender.class);

        var driverSender = mock(DriverSender.class);

        var tripSender = mock(TripSender.class);

        var tripHistoryProvider = mock(TripHistoryProvider.class);

        var message = Instancio.of(ShiftMessage.class).create();

        var driver = Instancio.of(Driver.class).create();

        var shiftService = mock(ShiftService.class);

        var tripService = mock(TripService.class);

        shiftProcessor = new ShiftProcessorImpl(shiftProvider, shiftMapper, tripProvider, updateTripService, tripSender, shiftSender, driverSender, verificationService, driverProvider, tripHistoryProvider, shiftService, tripService);

        when(driverProvider.get(any())).thenReturn(Optional.of(driver));
        when(shiftProvider.get(any())).thenReturn(Optional.of(Instancio.of(Shift.class)
                .set(Select.field(Shift::getDriverId), driver.getId())
                .set(Select.field(Shift::isActive), true)
                .create()));

        var future = shiftProcessor.processShift(message, driver);

        Assertions.assertEquals(1, future.get().intValue());
    }

    @Test
    void shiftInputShiftExistsAndActiveAndShiftMessageIsDeletedTest() throws ExecutionException, InterruptedException {
        var shiftProvider = mock(ShiftProvider.class);

        var shiftMapper = mock(ShiftMapper.class);

        var driverProvider = mock(DriverProvider.class);

        var tripProvider = mock(TripProvider.class);

        var updateTripService = mock(UpdateTripService.class);

        var verificationService = mock(VerificationService.class);

        var shiftSender = mock(ShiftSender.class);

        var driverSender = mock(DriverSender.class);

        var tripSender = mock(TripSender.class);

        var tripHistoryProvider = mock(TripHistoryProvider.class);

        var message = Instancio.of(ShiftMessage.class).create();

        var driver = Instancio.of(Driver.class).create();

        var shiftService = mock(ShiftService.class);

        var tripService = mock(TripService.class);

        shiftProcessor = new ShiftProcessorImpl(shiftProvider, shiftMapper, tripProvider, updateTripService, tripSender, shiftSender, driverSender, verificationService, driverProvider, tripHistoryProvider, shiftService, tripService);

        var trips = Instancio.ofList(Trip.class)
                .set(Select.field(Trip::getId), driver.getId())
                .set(Select.field(Trip::getStatus), TripStatus.DRIVER_ASSIGNED)
                .create();

        when(driverProvider.get(any())).thenReturn(Optional.of(driver));
        when(shiftProvider.get(any())).thenReturn(Optional.of(Instancio.of(Shift.class)
                .set(Select.field(Shift::getDriverId), driver.getId())
                .set(Select.field(Shift::isActive), true)
                .create()));
        when(tripProvider.findAllByDriverId(any())).thenReturn(trips);

        var future = shiftProcessor.processShift(message, driver);

        Assertions.assertEquals(1, future.get().intValue());
    }

    @Test
    void shiftInputShiftExistsAndActiveAndShiftMessageIsDeletedAndDriverIsBusyTest() throws ExecutionException, InterruptedException {
        var shiftProvider = mock(ShiftProvider.class);

        var shiftMapper = mock(ShiftMapper.class);

        var driverProvider = mock(DriverProvider.class);

        var tripProvider = mock(TripProvider.class);

        var updateTripService = mock(UpdateTripService.class);

        var verificationService = mock(VerificationService.class);

        var shiftSender = mock(ShiftSender.class);

        var driverSender = mock(DriverSender.class);

        var tripSender = mock(TripSender.class);

        var tripHistoryProvider = mock(TripHistoryProvider.class);

        var message = Instancio.of(ShiftMessage.class).create();

        var driver = Instancio.of(Driver.class).create();

        var shiftService = mock(ShiftService.class);

        var tripService = mock(TripService.class);

        shiftProcessor = new ShiftProcessorImpl(shiftProvider, shiftMapper, tripProvider, updateTripService, tripSender, shiftSender, driverSender, verificationService, driverProvider, tripHistoryProvider, shiftService, tripService);

        var trips = Instancio.ofList(Trip.class)
                .set(Select.field(Trip::getId), driver.getId())
                .set(Select.field(Trip::getStatus), TripStatus.DRIVER_ASSIGNED)
                .create();

        trips.add(Instancio.of(Trip.class)
                .set(Select.field(Trip::getId), driver.getId())
                .set(Select.field(Trip::getStatus), TripStatus.TRIP_IN_PROGRESS)
                .create());

        when(driverProvider.get(any())).thenReturn(Optional.of(driver));
        when(shiftProvider.get(any())).thenReturn(Optional.of(Instancio.of(Shift.class)
                .set(Select.field(Shift::getDriverId), driver.getId())
                .set(Select.field(Shift::isActive), true)
                .create()));
        when(tripProvider.findAllByDriverId(any())).thenReturn(trips);

        var future = shiftProcessor.processShift(message, driver);

        Assertions.assertEquals(1, future.get().intValue());
    }

    @Test
    @DisplayName("Обновление EWB - вход на линию")
    void processEwbUpdate_onTheLineTest() {
        var shiftProvider = mock(ShiftProvider.class);
        var shiftService = mock(ShiftService.class);
        var tripService = mock(TripService.class);
        var verificationService = mock(VerificationService.class);

        var ewbMessage = Instancio.of(EwbMessage.class)
                .set(Select.field(EwbMessage::status), "ON_THE_LINE")
                .create();

        var shift = Instancio.of(Shift.class).create();
        when(shiftProvider.getByEwbId(any())).thenReturn(Optional.of(shift));

        var processor = new ShiftProcessorImpl(
                shiftProvider,
                mock(ShiftMapper.class),
                mock(TripProvider.class),
                mock(UpdateTripService.class),
                mock(TripSender.class),
                mock(ShiftSender.class),
                mock(DriverSender.class),
                verificationService,
                mock(DriverProvider.class),
                mock(TripHistoryProvider.class),
                shiftService,
                tripService
        );

        processor.processEwbUpdate(ewbMessage);

        verify(verificationService).checkShiftIsDeleted(shift, ShiftOperationType.ENTER);
        verify(shiftService).activate(shift);
        verify(tripService).processPlannedTrips(shift);
    }

    @Test
    @DisplayName("Обновление EWB - выход с линии")
    void processEwbUpdate_ewbClosedTest() {
        var shiftProvider = mock(ShiftProvider.class);
        var driverProvider = mock(DriverProvider.class);
        var tripProvider = mock(TripProvider.class);
        var updateTripService = mock(UpdateTripService.class);
        var tripSender = mock(TripSender.class);
        var tripHistoryProvider = mock(TripHistoryProvider.class);
        var verificationService = mock(VerificationService.class);
        var shiftService = mock(ShiftService.class);
        var driverSender = mock(DriverSender.class);

        var ewbMessage = Instancio.of(EwbMessage.class)
                .set(Select.field(EwbMessage::status), "EWB_CLOSED")
                .create();

        var driver = Instancio.of(Driver.class).create();
        var shift = Instancio.of(Shift.class)
                .set(Select.field(Shift::getDriverId), driver.getId())
                .create();

        var trip = Instancio.of(Trip.class)
                .set(Select.field(Trip::getStatus), TripStatus.DRIVER_ASSIGNED)
                .create();

        when(shiftProvider.getByEwbId(any())).thenReturn(Optional.of(shift));
        when(driverProvider.get(any())).thenReturn(Optional.of(driver));
        when(tripProvider.findAllByDriverAndStatusIn(any(), any())).thenReturn(List.of(trip));
        when(updateTripService.updateTrip(any(), any(), any())).thenReturn(trip);

        var processor = new ShiftProcessorImpl(
                shiftProvider,
                mock(ShiftMapper.class),
                tripProvider,
                updateTripService,
                tripSender,
                mock(ShiftSender.class),
                driverSender,
                verificationService,
                driverProvider,
                tripHistoryProvider,
                shiftService,
                mock(TripService.class)
        );

        processor.processEwbUpdate(ewbMessage);

        verify(verificationService).checkShiftIsDeleted(shift, ShiftOperationType.EXIT);
        verify(verificationService).checkDriverBusynessForExitFromShift(driver);
        verify(tripProvider, times(1)).findAllByDriverAndStatusIn(any(), any());
        verify(updateTripService).updateTrip(any(), any(), eq(TripStatus.WAITING_FOR_ASSIGNMENT));
        verify(tripSender).send(any(), eq(false), eq(ChannelType.BOTH));
        verify(tripHistoryProvider).save(any());
        verify(shiftService).deactivate(shift);
    }

    @Test
    @DisplayName("Обновление EWB - отмена")
    void processEwbUpdate_ewbCancelledTest() {
        var shiftProvider = mock(ShiftProvider.class);
        var shiftSender = mock(ShiftSender.class);

        var ewbMessage = Instancio.of(EwbMessage.class)
                .set(Select.field(EwbMessage::status), "EWB_CANCELLED")
                .create();

        var shift = Instancio.of(Shift.class)
                .set(Select.field(Shift::getEwbId), ewbMessage.getId())
                .create();

        when(shiftProvider.getByEwbId(any())).thenReturn(Optional.of(shift));

        var processor = new ShiftProcessorImpl(
                shiftProvider,
                mock(ShiftMapper.class),
                mock(TripProvider.class),
                mock(UpdateTripService.class),
                mock(TripSender.class),
                shiftSender,
                mock(DriverSender.class),
                mock(VerificationService.class),
                mock(DriverProvider.class),
                mock(TripHistoryProvider.class),
                mock(ShiftService.class),
                mock(TripService.class)
        );

        processor.processEwbUpdate(ewbMessage);

        verify(shiftProvider).save(argThat(s -> s.getEwbId() == null));
        verify(shiftSender).send(any());
    }

    @Test
    @DisplayName("Обновление EWB - пустой статус")
    void processEwbUpdate_emptyStatusTest() {
        var shiftProvider = mock(ShiftProvider.class);
        var shiftService = mock(ShiftService.class);
        var tripService = mock(TripService.class);
        var verificationService = mock(VerificationService.class);

        var ewbMessage = Instancio.of(EwbMessage.class)
                .set(Select.field(EwbMessage::status), "")
                .create();

        var processor = new ShiftProcessorImpl(
                shiftProvider,
                mock(ShiftMapper.class),
                mock(TripProvider.class),
                mock(UpdateTripService.class),
                mock(TripSender.class),
                mock(ShiftSender.class),
                mock(DriverSender.class),
                verificationService,
                mock(DriverProvider.class),
                mock(TripHistoryProvider.class),
                shiftService,
                tripService
        );

        processor.processEwbUpdate(ewbMessage);

        verifyNoInteractions(shiftProvider, shiftService, tripService, verificationService);
    }

    @Test
    @DisplayName("Обновление EWB - невалидный статус")
    void processEwbUpdate_invalidStatusTest() {
        var shiftProvider = mock(ShiftProvider.class);
        var shiftService = mock(ShiftService.class);
        var tripService = mock(TripService.class);
        var verificationService = mock(VerificationService.class);

        var ewbMessage = Instancio.of(EwbMessage.class)
                .set(Select.field(EwbMessage::status), "INVALID_STATUS")
                .create();

        var processor = new ShiftProcessorImpl(
                shiftProvider,
                mock(ShiftMapper.class),
                mock(TripProvider.class),
                mock(UpdateTripService.class),
                mock(TripSender.class),
                mock(ShiftSender.class),
                mock(DriverSender.class),
                verificationService,
                mock(DriverProvider.class),
                mock(TripHistoryProvider.class),
                shiftService,
                tripService
        );

        processor.processEwbUpdate(ewbMessage);

        verifyNoInteractions(shiftProvider, shiftService, tripService, verificationService);
    }

}
