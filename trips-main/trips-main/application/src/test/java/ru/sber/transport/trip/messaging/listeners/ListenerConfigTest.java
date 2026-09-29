package ru.sber.transport.trip.messaging.listeners;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.instancio.TargetSelector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.dispatcher.messages.*;
import ru.sber.transport.driver_track.messaging.TripFactDistanceMessage;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sber.transport.trip.business.TripUseCases;
import ru.sber.transport.trip.business.model.*;
import ru.sber.transport.trip.business.providers.TripProvider;
import ru.sber.transport.trip.messaging.mapper.RequestMapperImpl;
import ru.sber.transport.trip.messaging.mapper.WaypointMapperImpl;
import ru.sber.transport.trip.messaging.processor.ShiftProcessor;
import ru.sber.transport.trip.messaging.providers.*;
import ru.sber.transport.trip.messaging.senders.dispatcher.DriverSender;
import ru.sber.transport.trip.messaging.senders.dispatcher.ShiftSender;
import ru.sber.transport.trip.messaging.senders.dispatcher.TripSender;
import ru.sber.transport.trip.message.EwbMessage;
import ru.sber.transport.trip.providers.dispatcher.mapper.DispatcherMapper;
import ru.sber.transport.trip.providers.driver.mapper.DriverMapper;
import ru.sber.transport.trip.providers.history.trip.TripHistoryProvider;
import ru.sber.transport.trip.providers.integration_client.mapper.IntegrationClientMapper;
import ru.sber.transport.trip.providers.integration_client.mapper.IntegrationClientMapperImpl;
import ru.sber.transport.trip.providers.shift.mapper.ShiftMapper;
import ru.sber.transport.trip.providers.vehicle.mapper.VehicleMapper;
import ru.sber.transport.trip.web.service.RequestService;
import ru.sber.transport.trip.web.service.ShiftService;
import ru.sber.transport.trip.web.service.TripService;
import ru.sber.transport.trip.web.service.UpdateTripService;
import ru.sber.transport.trip.web.service.VerificationService;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.messaging.AddressMessage;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@DisplayName("Проверка слушателей")
@UnitTest
@IsolatedTest
@Feature("app_platform_trips")
class ListenerConfigTest {

    private final ListenerConfig config = new ListenerConfig();

    @DisplayName("Проверка заявок")
    @Test
    void test_request() {
        var tripUseCases = mock(TripUseCases.class);

        var requestMapper = new RequestMapperImpl(new WaypointMapperImpl());

        var contractorProvider = mock(ContractorProvider.class);

        var waypoints = List.of(
                generateWaypoint(),
                generateWaypoint()
        );

        var message = Instancio.of(RequestMessage.class)
                .set(Select.field(RequestMessage::getStatus), TripRequestStatus.TAXI_TRIP_IN_PROGRESS.name())
                .set(Select.field(RequestMessage::getTripClass), TaxiClass.ECONOMY.name())
                .set(Select.field(RequestMessage::getWaypoints), waypoints)
                .set(Select.field(RequestMessage::getTransportType), TransportTypeEnum.TAXI.name())
                .create();
        var map = new HashMap<String, Object>();
        map.put(KafkaHeaders.RECEIVED_KEY, message.getId());
        map.put("transportType", "TAXI");
        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(map));

        var input = config.requestInput(tripUseCases, requestMapper, contractorProvider);

        Mockito.when(contractorProvider.isDispatcher(any())).thenReturn(true);

        input.accept(rawMessage);

        var requestCaptor = ArgumentCaptor.forClass(Request.class);

        verify(tripUseCases).process(requestCaptor.capture(), any(UUID.class), any(UUID.class));

        var actual = requestCaptor.getValue();

        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getAuthorId()).isEqualTo(message.getAuthorId());
        assertThat(actual.getPassengerId()).isEqualTo(message.getPassengerId());
        assertThat(actual.getTaxiClass()).isEqualTo(message.getTripClass());
        assertThat(actual.getPassengerCount()).isEqualTo(message.getPassengerCount());
        assertThat(actual.getExpected().time()).isEqualTo(message.getExpected().time());
        assertThat(actual.getExpected().cost()).isEqualTo(Double.valueOf(message.getExpected().cost()).longValue());
        assertThat(actual.getExpected().distance()).isEqualTo(message.getExpected().distance());
        assertThat(actual.getCreationTime()).isEqualTo(OffsetDateTime.of(message.getCreationTime(), ZoneOffset.UTC));
        assertThat(actual.getDesiredDate()).isEqualTo(OffsetDateTime.of(message.getDesiredDate(), ZoneOffset.UTC));
        assertThat(actual.getRideId()).isEqualTo(message.getRideId());
        assertThat(actual.isCoopTrip()).isEqualTo(message.isCoopTrip());
        assertThat(actual.isSuburb()).isEqualTo(message.isSuburbTrip());
        assertThat(actual.getTimeZone()).isEqualTo(message.getTimeZone());
        assertThat(actual.getTariffId()).isEqualTo(message.getTariffId());
        assertThat(actual.getContractorId()).isEqualTo(message.getContractorId());
        assertThat(actual.getStatus()).isEqualTo(message.getStatus());
        assertThat(actual.getComment()).isEqualTo(message.getCommentForDriver());
        assertThat(actual.getWaypoints()).hasSameSizeAs(message.getWaypoints());
        assertThat(actual.getWaypoints().get(0).latitude()).isEqualTo((double) message.getWaypoints().get(0).address().getLatitude());
        assertThat(actual.getWaypoints().get(0).longitude()).isEqualTo((double) message.getWaypoints().get(0).address().getLongitude());
        assertThat(actual.getWaypoints().get(0).id()).isEqualTo(message.getWaypoints().get(0).id());
    }

    @DisplayName("Проверка заявок")
    @Test
    void test_request_contractor_is_not_dispatcher() {
        var tripUseCases = mock(TripUseCases.class);

        var requestMapper = new RequestMapperImpl(new WaypointMapperImpl());

        var contractorProvider = mock(ContractorProvider.class);

        var waypoints = List.of(
                generateWaypoint(),
                generateWaypoint()
        );

        var message = Instancio.of(RequestMessage.class)
                .set(Select.field(RequestMessage::getStatus), TripRequestStatus.TAXI_TRIP_IN_PROGRESS.name())
                .set(Select.field(RequestMessage::getTripClass), TaxiClass.ECONOMY.name())
                .set(Select.field(RequestMessage::getWaypoints), waypoints)
                .set(Select.field(RequestMessage::getTransportType), TransportTypeEnum.TAXI.name())
                .create();
        var map = new HashMap<String, Object>();
        map.put(KafkaHeaders.RECEIVED_KEY, message.getId());
        map.put("transportType", "TAXI");
        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(map));

        var input = config.requestInput(tripUseCases, requestMapper, contractorProvider);

        Mockito.when(contractorProvider.isDispatcher(any())).thenReturn(false);

        input.accept(rawMessage);

        var requestCaptor = ArgumentCaptor.forClass(Request.class);

        verify(tripUseCases, times(0)).process(requestCaptor.capture(), any(UUID.class), any(UUID.class));
    }

    @DisplayName("Проверка заявок на трансфер")
    @Test
    void test_request_transfer() {
        var tripUseCases = mock(TripUseCases.class);

        var requestMapper = new RequestMapperImpl(new WaypointMapperImpl());

        var contractorProvider = mock(ContractorProvider.class);

        var waypoints = List.of(
                generateWaypoint(),
                generateWaypoint()
        );

        var message = Instancio.of(RequestMessage.class)
                .set(Select.field(RequestMessage::getStatus), TripRequestStatus.GROUP_TRANSFER_TRIP_IN_PROGRESS.name())
                .set(Select.field(RequestMessage::getTripClass), null)
                .set(Select.field(RequestMessage::getWaypoints), waypoints)
                .set(Select.field(RequestMessage::getTransportType), TransportTypeEnum.GROUP_TRANSFER.name())
                .create();
        var map = new HashMap<String, Object>();
        map.put(KafkaHeaders.RECEIVED_KEY, message.getId());
        map.put("transportType", "GROUP_TRANSFER");
        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(map));

        var input = config.requestInput(tripUseCases, requestMapper, contractorProvider);

        Mockito.when(contractorProvider.isDispatcher(any())).thenReturn(true);

        input.accept(rawMessage);

        var requestCaptor = ArgumentCaptor.forClass(Request.class);

        verify(tripUseCases).process(requestCaptor.capture(), any(UUID.class), any(UUID.class));

        var actual = requestCaptor.getValue();

        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getAuthorId()).isEqualTo(message.getAuthorId());
        assertThat(actual.getPassengerId()).isEqualTo(message.getPassengerId());
        assertThat(actual.getTaxiClass()).isNull();
        assertThat(actual.getPassengerCount()).isEqualTo(message.getPassengerCount());
        assertThat(actual.getExpected().time()).isEqualTo(message.getExpected().time());
        assertThat(actual.getExpected().cost()).isEqualTo(Double.valueOf(message.getExpected().cost()).longValue());
        assertThat(actual.getExpected().distance()).isEqualTo(message.getExpected().distance());
        assertThat(actual.getCreationTime()).isEqualTo(OffsetDateTime.of(message.getCreationTime(), ZoneOffset.UTC));
        assertThat(actual.getDesiredDate()).isEqualTo(OffsetDateTime.of(message.getDesiredDate(), ZoneOffset.UTC));
        assertThat(actual.getRideId()).isEqualTo(message.getRideId());
        assertThat(actual.isCoopTrip()).isEqualTo(message.isCoopTrip());
        assertThat(actual.isSuburb()).isEqualTo(message.isSuburbTrip());
        assertThat(actual.getTimeZone()).isEqualTo(message.getTimeZone());
        assertThat(actual.getTariffId()).isEqualTo(message.getTariffId());
        assertThat(actual.getContractorId()).isEqualTo(message.getContractorId());
        assertThat(actual.getStatus()).isEqualTo(message.getStatus());
        assertThat(actual.getComment()).isEqualTo(message.getCommentForDriver());
        assertThat(actual.getWaypoints()).hasSameSizeAs(message.getWaypoints());
        assertThat(actual.getWaypoints().get(0).latitude()).isEqualTo((double) message.getWaypoints().get(0).address().getLatitude());
        assertThat(actual.getWaypoints().get(0).longitude()).isEqualTo((double) message.getWaypoints().get(0).address().getLongitude());
        assertThat(actual.getWaypoints().get(0).id()).isEqualTo(message.getWaypoints().get(0).id());
    }

    private RequestMessage.Waypoint generateWaypoint() {
        return new RequestMessage.Waypoint(
                UUID.randomUUID(),
                AddressMessage.builder()
                        .country(Instancio.create(String.class))
                        .region(Instancio.create(String.class))
                        .city(Instancio.create(String.class))
                        .street(Instancio.create(String.class))
                        .house(Instancio.create(String.class))
                        .latitude(Instancio.create(Double.class))
                        .longitude(Instancio.create(Double.class))
                        .build(),
                Instancio.create(Duration.class),
                Instancio.create(Boolean.class),
                Instancio.create(Boolean.class),
                Instancio.create(String.class),
                Instancio.create(Integer.class)
                );
    }

    @DisplayName("Проверка контрагентов")
    @Test
    void test_contractorInput() {
        var contractorProvider = mock(ContractorProvider.class);

        var dispatcherMapper = Mockito.mock(DispatcherMapper.class);

        var dispatcherProvider = Mockito.mock(DispatcherProvider.class);

        var message = Instancio.create(ContractorMessage.class);

        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId())));

        var input = config.contractorInput(contractorProvider ,dispatcherProvider, dispatcherMapper);

        input.accept(rawMessage);

        var idCaptor = ArgumentCaptor.forClass(UUID.class);
        var messageCaptor = ArgumentCaptor.forClass(ContractorMessage.class);

        verify(contractorProvider).save(idCaptor.capture(), messageCaptor.capture());

        var actualId = idCaptor.getValue();
        var actualMessage = messageCaptor.getValue();

        assertThat(actualId).isEqualTo(message.getId());
        assertThat(actualMessage.getId()).isEqualTo(message.getId());
        assertThat(actualMessage.password()).isEqualTo(message.password());
        assertThat(actualMessage.url()).isEqualTo(message.url());
        assertThat(actualMessage.contactPersonInfo()).isEqualTo(message.contactPersonInfo());
        assertThat(actualMessage.contactPersonPhone()).isEqualTo(message.contactPersonPhone());
        assertThat(actualMessage.contractorRusName()).isEqualTo(message.contractorRusName());
        assertThat(actualMessage.contractorName()).isEqualTo(message.contractorName());
        assertThat(actualMessage.name()).isEqualTo(message.name());
        assertThat(actualMessage.digitId()).isEqualTo(message.digitId());
        assertThat(actualMessage.msrn()).isEqualTo(message.msrn());
        assertThat(actualMessage.tin()).isEqualTo(message.tin());
        assertThat(actualMessage.rating()).isEqualTo(message.rating());
        assertThat(actualMessage.deleted()).isEqualTo(message.deleted());
    }

    @Test
    void driverInput(){
        var driverProvider = mock(DriverProvider.class);

        var driverMapper = mock(DriverMapper.class);

        var message = Instancio.of(DriverMessage.class).set(Select.field(DriverMessage::driverSpeciality), "PASSENGER").create();

        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId())));

        var input = config.driverInput(driverProvider, driverMapper);

        input.accept(rawMessage);

        var messageCaptor = ArgumentCaptor.forClass(DriverMessage.class);

        verify(driverMapper).toModel(messageCaptor.capture());

        var actualMessage = messageCaptor.getValue();

        assertThat(actualMessage.getId()).isEqualTo(message.getId());
        assertThat(actualMessage.lastName()).isEqualTo(message.lastName());
        assertThat(actualMessage.firstName()).isEqualTo(message.firstName());
        assertThat(actualMessage.patronymic()).isEqualTo(message.patronymic());
        assertThat(actualMessage.passport()).isEqualTo(message.passport());
        assertThat(actualMessage.contractorId()).isEqualTo(message.contractorId());
        assertThat(actualMessage.active()).isEqualTo(message.active());
        assertThat(actualMessage.rating()).isEqualTo(message.rating());
        assertThat(actualMessage.driverLicenseNumber()).isEqualTo(message.driverLicenseNumber());
        assertThat(actualMessage.cargoLicenceNumber()).isEqualTo(message.cargoLicenceNumber());
        assertThat(actualMessage.serviceLicenseNumber()).isEqualTo(message.serviceLicenseNumber());
        assertThat(actualMessage.latitude()).isEqualTo(message.latitude());
        assertThat(actualMessage.longitude()).isEqualTo(message.longitude());
        assertThat(actualMessage.pointTime()).isEqualTo(message.pointTime());
        assertThat(actualMessage.timeZone()).isEqualTo(message.timeZone());
        assertThat(actualMessage.serving()).isEqualTo(message.serving());
        assertThat(actualMessage.online()).isEqualTo(message.online());
        assertThat(actualMessage.activeTripId()).isEqualTo(message.activeTripId());
        assertThat(actualMessage.activeShiftId()).isEqualTo(message.activeShiftId());
        assertThat(actualMessage.licenseClasses()).isEqualTo(message.licenseClasses());
        assertThat(actualMessage.experience()).isEqualTo(message.experience());
        assertThat(actualMessage.contactPhone()).isEqualTo(message.contactPhone());
        assertThat(actualMessage.email()).isEqualTo(message.email());
        assertThat(actualMessage.humanReadableId()).isEqualTo(message.humanReadableId());
        assertThat(actualMessage.driverSpeciality()).isEqualTo(message.driverSpeciality());
    }

    @Test
    void vehicleInput(){
        var vehicleProvider = mock(VehicleProvider.class);

        var vehicleMapper = mock(VehicleMapper.class);

        var message = Instancio.of(VehicleMessage.class).create();

        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId())));

        var input = config.vehicleInput(vehicleProvider, vehicleMapper);

        input.accept(rawMessage);

        var messageCaptor = ArgumentCaptor.forClass(VehicleMessage.class);

        verify(vehicleMapper).toModel(messageCaptor.capture());

        var actualMessage = messageCaptor.getValue();

        assertThat(actualMessage.getId()).isEqualTo(message.getId());
        assertThat(actualMessage.brand()).isEqualTo(message.brand());
        assertThat(actualMessage.model()).isEqualTo(message.model());
        assertThat(actualMessage.stateNumber()).isEqualTo(message.stateNumber());
        assertThat(actualMessage.color()).isEqualTo(message.color());
        assertThat(actualMessage.contractorId()).isEqualTo(message.contractorId());
        assertThat(actualMessage.deleted()).isEqualTo(message.deleted());
    }

    @Test
    void shiftInputTest(){

        var driverProvider = mock(DriverProvider.class);

        var shiftProcessor = mock(ShiftProcessor.class);

        var message = Instancio.of(ShiftMessage.class).create();

        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId())));

        var input = config.shiftInput(driverProvider, shiftProcessor);

        when(driverProvider.get(any())).thenReturn(Optional.of(Instancio.of(Driver.class).create()));

        input.accept(rawMessage);

        var messageCaptor = ArgumentCaptor.forClass(ShiftMessage.class);

        verify(shiftProcessor).processShift(messageCaptor.capture(), any());

        var actualMessage = messageCaptor.getValue();

        assertThat(actualMessage.getId()).isEqualTo(message.getId());
        assertThat(actualMessage.driverId()).isEqualTo(message.driverId());
        assertThat(actualMessage.vehicleId()).isEqualTo(message.vehicleId());
        assertThat(actualMessage.startDate()).isEqualTo(message.startDate());
        assertThat(actualMessage.endDate()).isEqualTo(message.endDate());
        assertThat(actualMessage.contractorId()).isEqualTo(message.contractorId());
        assertThat(actualMessage.deleted()).isEqualTo(message.deleted());
        assertThat(actualMessage.active()).isEqualTo(message.active());
    }

    @Test
    @DisplayName("Получение фактической дистанции поездки")
    void factDistanceInput() {
        var requestService = mock(RequestService.class);
        var payload = Instancio.create(TripFactDistanceMessage.class);
        var message = MessageBuilder.createMessage(payload, new MessageHeaders(null));

        var func = config.factDistanceInput(requestService);
        func.accept(message);

        var idCaptor = ArgumentCaptor.forClass(UUID.class);
        var mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(requestService).updateFinal(idCaptor.capture(), mapCaptor.capture());

        assertEquals(idCaptor.getValue(), payload.getId());
        assertArrayEquals(payload.getDistances().entrySet().toArray(), mapCaptor.getValue().entrySet().toArray());
    }

    @Test
    @DisplayName("Получение нового клиента для интеграции")
    void integrationClientInput() {
        var provider = mock(IntegrationClientProvider.class);
        var payload = Instancio.create(IntegrationClientMessage.class);
        var message = MessageBuilder.createMessage(payload, new MessageHeaders(null));

        var func = config.integrationClientInput(provider, new IntegrationClientMapperImpl());
        func.accept(message);

        var captor = ArgumentCaptor.forClass(IntegrationClient.class);
        verify(provider).save(captor.capture());

        var captureValue = captor.getValue();
        assertEquals(captureValue.getId(), payload.getId());
        assertEquals(captureValue.getContractorId(), payload.contractorId());
        assertEquals(captureValue.isActive(), payload.active());
    }

    @Test
    @DisplayName("Получение филиала автопарка")
    void autoparkInput() {
        var provider = mock(AutoparkProvider.class);
        var payload = Instancio.create(AutoparkMessage.class);
        var message = MessageBuilder.createMessage(payload, new MessageHeaders(null));

        var func = config.autoparkInput(provider);
        func.accept(message);

        var captor = ArgumentCaptor.forClass(AutoparkMessage.class);
        verify(provider).save(captor.capture());

        var captureValue = captor.getValue();
        assertEquals(captureValue.getId(), payload.getId());
        assertEquals(captureValue.contractorId(), payload.contractorId());
        assertEquals(captureValue.routingId(), payload.routingId());
        assertEquals(captureValue.deleted(), payload.deleted());
    }

    @Test
    @DisplayName("Получение данных EWB")
    void ewbInputOnTheLineTest() {
        var shiftProcessor = mock(ShiftProcessor.class);

        var ewbMessage = Instancio.of(EwbMessage.class)
                .set(Select.field(EwbMessage::status), "ON_THE_LINE")
                .create();

        var rawMessage = MessageBuilder.createMessage(ewbMessage, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, ewbMessage.getId())));

        var func = config.ewbInput(shiftProcessor);
        func.accept(rawMessage);

        var captor = ArgumentCaptor.forClass(EwbMessage.class);
        verify(shiftProcessor).processEwbUpdate(captor.capture());

        var capturedMessage = captor.getValue();
        assertEquals(capturedMessage.getId(), ewbMessage.getId());
        assertEquals(capturedMessage.status(), "ON_THE_LINE");
    }

    @Test
    @DisplayName("Получение данных EWB - закрытие")
    void ewbInputEwbClosedTest() {
        var shiftProcessor = mock(ShiftProcessor.class);

        var ewbMessage = Instancio.of(EwbMessage.class)
                .set(Select.field(EwbMessage::status), "EWB_CLOSED")
                .create();

        var rawMessage = MessageBuilder.createMessage(ewbMessage, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, ewbMessage.getId())));

        var func = config.ewbInput(shiftProcessor);
        func.accept(rawMessage);

        var captor = ArgumentCaptor.forClass(EwbMessage.class);
        verify(shiftProcessor).processEwbUpdate(captor.capture());

        var capturedMessage = captor.getValue();
        assertEquals(capturedMessage.getId(), ewbMessage.getId());
        assertEquals(capturedMessage.status(), "EWB_CLOSED");
    }

    @Test
    @DisplayName("Получение данных EWB - отмена")
    void ewbInputEwbCancelledTest() {
        var shiftProcessor = mock(ShiftProcessor.class);

        var ewbMessage = Instancio.of(EwbMessage.class)
                .set(Select.field(EwbMessage::status), "EWB_CANCELLED")
                .create();

        var rawMessage = MessageBuilder.createMessage(ewbMessage, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, ewbMessage.getId())));

        var func = config.ewbInput(shiftProcessor);
        func.accept(rawMessage);

        var captor = ArgumentCaptor.forClass(EwbMessage.class);
        verify(shiftProcessor).processEwbUpdate(captor.capture());

        var capturedMessage = captor.getValue();
        assertEquals(capturedMessage.getId(), ewbMessage.getId());
        assertEquals(capturedMessage.status(), "EWB_CANCELLED");
    }

    @Test
    @DisplayName("Получение данных EWB - пустой статус")
    void ewbInputEmptyStatusTest() {
        var shiftProcessor = mock(ShiftProcessor.class);

        var ewbMessage = Instancio.of(EwbMessage.class)
                .set(Select.field(EwbMessage::status), "")
                .create();

        var rawMessage = MessageBuilder.createMessage(ewbMessage, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, ewbMessage.getId())));

        var func = config.ewbInput(shiftProcessor);
        func.accept(rawMessage);

        var captor = ArgumentCaptor.forClass(EwbMessage.class);
        verify(shiftProcessor).processEwbUpdate(captor.capture());

        var capturedMessage = captor.getValue();
        assertEquals("", capturedMessage.status());
    }
}