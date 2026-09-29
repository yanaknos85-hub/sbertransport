package ru.sber.transport.dispatcher.messaging.listeners;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.dispatcher.database.dao.ShiftRepository;
import ru.sber.transport.dispatcher.database.model.Driver;
import ru.sber.transport.dispatcher.database.model.Shift;
import ru.sber.transport.dispatcher.database.model.Trip;
import ru.sber.transport.dispatcher.dto.enums.TripStatus;
import ru.sber.transport.dispatcher.mappers.DriverMapper;
import ru.sber.transport.dispatcher.mappers.ShiftMapperImpl;
import ru.sber.transport.dispatcher.mappers.TripsMapperImpl;
import ru.sber.transport.dispatcher.messages.DriverMessage;
import ru.sber.transport.dispatcher.messages.EwbMessage;
import ru.sber.transport.dispatcher.messages.ShiftFromMaisMessage;
import ru.sber.transport.dispatcher.messages.ShiftMessage;
import ru.sber.transport.dispatcher.messages.Source;
import ru.sber.transport.dispatcher.messages.TransportMessage;
import ru.sber.transport.dispatcher.service.*;
import ru.sber.transport.trip.message.TripMessage;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_dispatcher")
@DisplayName("Проверка получения данных")
class ListenerConfigTest {

    private final ListenerConfig config = new ListenerConfig();

    @DisplayName("Получение водителей")
    @Test
    void test_driver() {
        var driverService = mock(DriverService.class);
        var driverMapper = mock(DriverMapper.class);
        var shiftRepository = mock(ShiftRepository.class);

        var message = Instancio.of(DriverMessage.class)
                .create();
        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of("source", Source.TRIPS)));

        var input = config.driverInput(driverService, driverMapper, shiftRepository);

        var driver = new Driver();
        driver.setId(message.getId());
        when(driverService.get(any())).thenReturn(Optional.of(driver));

        input.accept(rawMessage);

        var driverCaptor = ArgumentCaptor.forClass(Driver.class);

        verify(driverService).save(driverCaptor.capture());

        var saved = driverCaptor.getValue();

        assertThat(saved.getId()).isEqualTo(message.getId());
    }
    @DisplayName("Получение смен")
    @Test
    void test_shift() {
        var shiftService = mock(ShiftService.class);
        var shiftMapper = new ShiftMapperImpl();

        var message = Instancio.of(ShiftMessage.class)
                .create();
        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of("source", Source.TRIPS)));

        var input = config.shiftInput(shiftService, shiftMapper);

        var shift = new Shift();
        shiftMapper.update(shift, message);
        when(shiftService.get(any())).thenReturn(Optional.of(shift));

        input.accept(rawMessage);

        var shiftArgumentCaptor = ArgumentCaptor.forClass(Shift.class);

        verify(shiftService).save(shiftArgumentCaptor.capture(), any());

        var saved = shiftArgumentCaptor.getValue();

        assertThat(saved.getId()).isEqualTo(message.getId());;
    }

    @DisplayName("Получение поездок: сохранение")
    @Test
    void test_trips_save() {
        var tripsService = mock(TripsService.class);
        var tripMapper = new TripsMapperImpl();

        var message = Instancio.of(TripMessage.class)
                .create();
        var shift = Instancio.of(Shift.class).create();
        message.setPlannedShiftId(shift.getId());

        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of("source", Source.TRIPS)));

        var input = config.tripInput(tripMapper, tripsService);

        input.accept(rawMessage);

        var tripArgumentCaptor = ArgumentCaptor.forClass(Trip.class);

        verify(tripsService).save(tripArgumentCaptor.capture());

        var saved = tripArgumentCaptor.getValue();

        assertThat(saved.getId()).isEqualTo(message.getId());;
    }

    @DisplayName("Получение поездок: удаление")
    @Test
    void test_trips_delete() {
        var tripsService = mock(TripsService.class);
        var tripMapper = new TripsMapperImpl();

        var message = Instancio.of(TripMessage.class)
                .create();
        message.setStatus(TripStatus.ORDER_CANCELLED_BY_CLIENT.name());
        var shift = Instancio.of(Shift.class).create();
        message.setPlannedShiftId(shift.getId());

        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of("source", Source.TRIPS)));

        var input = config.tripInput(tripMapper, tripsService);

        input.accept(rawMessage);

        verify(tripsService).delete(any(UUID.class));
    }

    @DisplayName("Получение автомобилей")
    @Test
    void test_vehicles() {
        var vehicleService = mock(VehicleService.class);

        var message = Instancio.of(TransportMessage.class)
                .create();

        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of("source", Source.TRIPS)));

        var input = config.transportInput(vehicleService);

        input.accept(rawMessage);

        verify(vehicleService).upsert(message);
    }

    @DisplayName("Получение смен из МАИС: обработка")
    @Test
    void test_integration_mais_shift() {
        var shiftService = mock(ShiftService.class);

        var message = new ShiftFromMaisMessage(
                UUID.randomUUID(),
                "route-123",
                "driver-456",
                "A123BC777",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                ShiftFromMaisMessage.ActionType.CREATE
        );
        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of("source", Source.TRIPS)));

        var input = config.integrationMaisShiftInput(shiftService);

        input.accept(rawMessage);

        verify(shiftService).handleShiftFromMais(message);
    }

    @DisplayName("Получение EWB сообщения")
    @Test
    void test_ewbInput() {
        var shiftControllerService = mock(ShiftControllerService.class);

        var message = new EwbMessage(UUID.randomUUID(), null);
        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of("source", Source.CONTRACTOR)));

        var input = config.ewbInput(shiftControllerService);

        input.accept(rawMessage);

        verify(shiftControllerService).sendSocketForEwb(message);
    }

    @DisplayName("Получение EWB сообщения с ошибкой")
    @Test
    void test_ewbInput_withError() {
        var shiftControllerService = mock(ShiftControllerService.class);

        var message = new EwbMessage(UUID.randomUUID(), "Error occurred");
        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of("source", Source.CONTRACTOR)));

        var input = config.ewbInput(shiftControllerService);

        input.accept(rawMessage);

        verify(shiftControllerService).sendSocketForEwb(message);
    }

    @DisplayName("Получение смен из МАИС: пропуск невалидного сообщения")
    @ParameterizedTest
    @MethodSource("invalidShiftMessages")
    void test_integration_mais_shift_skip_invalid(ShiftFromMaisMessage message) {
        var shiftService = mock(ShiftService.class);

        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of("source", Source.TRIPS)));

        var input = config.integrationMaisShiftInput(shiftService);

        input.accept(rawMessage);

        verify(shiftService, never()).handleShiftFromMais(any());
    }

    private static Stream<Arguments> invalidShiftMessages() {
        var validStartDate = LocalDateTime.now().plusHours(1);
        var validEndDate = LocalDateTime.now().plusHours(2);

        return Stream.of(
                Arguments.of(new ShiftFromMaisMessage(
                        UUID.randomUUID(),
                        null,
                        "driver-456",
                        "A123BC777",
                        validStartDate,
                        validEndDate,
                        ShiftFromMaisMessage.ActionType.CREATE
                )),
                Arguments.of(new ShiftFromMaisMessage(
                        UUID.randomUUID(),
                        "",
                        "driver-456",
                        "A123BC777",
                        validStartDate,
                        validEndDate,
                        ShiftFromMaisMessage.ActionType.CREATE
                )),
                Arguments.of(new ShiftFromMaisMessage(
                        UUID.randomUUID(),
                        "route-123",
                        null,
                        "A123BC777",
                        validStartDate,
                        validEndDate,
                        ShiftFromMaisMessage.ActionType.CREATE
                )),
                Arguments.of(new ShiftFromMaisMessage(
                        UUID.randomUUID(),
                        "route-123",
                        "",
                        "A123BC777",
                        validStartDate,
                        validEndDate,
                        ShiftFromMaisMessage.ActionType.CREATE
                )),
                Arguments.of(new ShiftFromMaisMessage(
                        UUID.randomUUID(),
                        "route-123",
                        "driver-456",
                        null,
                        validStartDate,
                        validEndDate,
                        ShiftFromMaisMessage.ActionType.CREATE
                )),
                Arguments.of(new ShiftFromMaisMessage(
                        UUID.randomUUID(),
                        "route-123",
                        "driver-456",
                        "",
                        validStartDate,
                        validEndDate,
                        ShiftFromMaisMessage.ActionType.CREATE
                )),
                Arguments.of(new ShiftFromMaisMessage(
                        UUID.randomUUID(),
                        "route-123",
                        "driver-456",
                        "A123BC777",
                        null,
                        validEndDate,
                        ShiftFromMaisMessage.ActionType.CREATE
                )),
                Arguments.of(new ShiftFromMaisMessage(
                        UUID.randomUUID(),
                        "route-123",
                        "driver-456",
                        "A123BC777",
                        validStartDate,
                        null,
                        ShiftFromMaisMessage.ActionType.CREATE
                )),
                Arguments.of(new ShiftFromMaisMessage(
                        UUID.randomUUID(),
                        "route-123",
                        "driver-456",
                        "A123BC777",
                        validEndDate,
                        validStartDate,
                        ShiftFromMaisMessage.ActionType.CREATE
                )),
                Arguments.of(new ShiftFromMaisMessage(
                        UUID.randomUUID(),
                        "route-123",
                        "driver-456",
                        "A123BC777",
                        validStartDate,
                        validEndDate,
                        null
                ))
        );
    }

}
