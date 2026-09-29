package ru.sber.transport.trips.cargo.messaging.listeners;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.transport.dispatcher.messages.*;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.trips.cargo.business.TripUseCases;
import ru.sber.transport.trips.cargo.business.model.Driver;
import ru.sber.transport.trips.cargo.business.model.IntegrationClient;
import ru.sber.transport.trips.cargo.messaging.processor.ShiftProcessor;
import ru.sber.transport.trips.cargo.messaging.providers.*;
import ru.sber.transport.trips.cargo.providers.dispatcher.mapper.DispatcherMapper;
import ru.sber.transport.trips.cargo.providers.driver.mapper.DriverMapper;
import ru.sber.transport.trips.cargo.providers.integration_client.mapper.IntegrationClientMapperImpl;
import ru.sber.transport.trips.cargo.providers.vehicle.mapper.VehicleMapper;
import ru.sberbank.ditsib.transport.request.messaging.RouteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@DisplayName("Проверка слушателей")
@UnitTest
@IsolatedTest
@Feature("app_platform_trips_cargo")
@ActiveProfiles({"test", "kafka", "kafka-ssl"})
class ListenerConfigTest extends KafkaTest {

    private final ListenerConfig config = new ListenerConfig();

    @DisplayName("Проверка грузовых заявок")
    @Test
    void test_cargo_request() {
        var tripUseCases = (TripUseCases<RouteMessage>) mock(TripUseCases.class);

        var message = Instancio.create(RouteMessage.class);
        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId())));

        var input = config.requestCargoInput(tripUseCases);

        input.accept(rawMessage);

        var requestCaptor = ArgumentCaptor.forClass(RouteMessage.class);

        verify(tripUseCases).process(requestCaptor.capture(), eq(null), eq(null));

        var actual = requestCaptor.getValue();
        assertThat(actual).isNotNull().isEqualTo(message);
    }

    @DisplayName("Проверка контрагентов")
    @Test
    void test_contractorInput() {
        var contractorProvider = Mockito.mock(ContractorProvider.class);

        var dispatcherMapper = Mockito.mock(DispatcherMapper.class);

        var dispatcherProvider = Mockito.mock(DispatcherProvider.class);

        var message = Instancio.create(ContractorMessage.class);

        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId())));

        var input = config.contractorInput(contractorProvider, dispatcherMapper, dispatcherProvider);

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

        var message = Instancio.of(DriverMessage.class).set(Select.field(DriverMessage::driverSpeciality), "CARGO").create();

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
    void driverInputIgnore(){
        var driverProvider = mock(DriverProvider.class);

        var driverMapper = mock(DriverMapper.class);

        var message = Instancio.of(DriverMessage.class).set(Select.field(DriverMessage::driverSpeciality), "CARGO").create();

        var map = new HashMap<String, Object>();
        map.put(KafkaHeaders.RECEIVED_KEY,  message.getId());
        map.put("source", Source.TRIPS_CARGO.name());

        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(map));

        var input = config.driverInput(driverProvider, driverMapper);

        input.accept(rawMessage);

        var messageCaptor = ArgumentCaptor.forClass(DriverMessage.class);

        verify(driverMapper, times(0)).toModel(messageCaptor.capture());
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
    void shiftInputTestIgnore(){

        var driverProvider = mock(DriverProvider.class);

        var shiftProcessor = mock(ShiftProcessor.class);

        var message = Instancio.of(ShiftMessage.class).create();

        var map = new HashMap<String, Object>();
        map.put(KafkaHeaders.RECEIVED_KEY,  message.getId());
        map.put("source", Source.TRIPS_CARGO.name());

        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(map));

        var input = config.shiftInput(driverProvider, shiftProcessor);

        when(driverProvider.get(any())).thenReturn(Optional.of(Instancio.of(Driver.class).create()));

        input.accept(rawMessage);

        var messageCaptor = ArgumentCaptor.forClass(ShiftMessage.class);

        verify(shiftProcessor, times(0)).processShift(messageCaptor.capture(), any());
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

        var ewbMessage = Instancio.of(ru.sber.transport.trips.cargo.message.EwbMessage.class)
                .set(Select.field(ru.sber.transport.trips.cargo.message.EwbMessage::status), "ON_THE_LINE")
                .create();

        var rawMessage = MessageBuilder.createMessage(ewbMessage, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, ewbMessage.getId())));

        var func = config.ewbInput(shiftProcessor);
        func.accept(rawMessage);

        var captor = ArgumentCaptor.forClass(ru.sber.transport.trips.cargo.message.EwbMessage.class);
        verify(shiftProcessor).processEwbUpdate(captor.capture());

        var capturedMessage = captor.getValue();
        assertEquals(capturedMessage.getId(), ewbMessage.getId());
        assertEquals(capturedMessage.status(), "ON_THE_LINE");
    }

    @Test
    @DisplayName("Получение данных EWB - закрытие")
    void ewbInputEwbClosedTest() {
        var shiftProcessor = mock(ShiftProcessor.class);

        var ewbMessage = Instancio.of(ru.sber.transport.trips.cargo.message.EwbMessage.class)
                .set(Select.field(ru.sber.transport.trips.cargo.message.EwbMessage::status), "EWB_CLOSED")
                .create();

        var rawMessage = MessageBuilder.createMessage(ewbMessage, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, ewbMessage.getId())));

        var func = config.ewbInput(shiftProcessor);
        func.accept(rawMessage);

        var captor = ArgumentCaptor.forClass(ru.sber.transport.trips.cargo.message.EwbMessage.class);
        verify(shiftProcessor).processEwbUpdate(captor.capture());

        var capturedMessage = captor.getValue();
        assertEquals(capturedMessage.getId(), ewbMessage.getId());
        assertEquals(capturedMessage.status(), "EWB_CLOSED");
    }

    @Test
    @DisplayName("Получение данных EWB - отмена")
    void ewbInputEwbCancelledTest() {
        var shiftProcessor = mock(ShiftProcessor.class);

        var ewbMessage = Instancio.of(ru.sber.transport.trips.cargo.message.EwbMessage.class)
                .set(Select.field(ru.sber.transport.trips.cargo.message.EwbMessage::status), "EWB_CANCELLED")
                .create();

        var rawMessage = MessageBuilder.createMessage(ewbMessage, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, ewbMessage.getId())));

        var func = config.ewbInput(shiftProcessor);
        func.accept(rawMessage);

        var captor = ArgumentCaptor.forClass(ru.sber.transport.trips.cargo.message.EwbMessage.class);
        verify(shiftProcessor).processEwbUpdate(captor.capture());

        var capturedMessage = captor.getValue();
        assertEquals(capturedMessage.getId(), ewbMessage.getId());
        assertEquals(capturedMessage.status(), "EWB_CANCELLED");
    }

    @Test
    @DisplayName("Получение данных EWB - пустой статус")
    void ewbInputEmptyStatusTest() {
        var shiftProcessor = mock(ShiftProcessor.class);

        var ewbMessage = Instancio.of(ru.sber.transport.trips.cargo.message.EwbMessage.class)
                .set(Select.field(ru.sber.transport.trips.cargo.message.EwbMessage::status), "")
                .create();

        var rawMessage = MessageBuilder.createMessage(ewbMessage, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, ewbMessage.getId())));

        var func = config.ewbInput(shiftProcessor);
        func.accept(rawMessage);

        var captor = ArgumentCaptor.forClass(ru.sber.transport.trips.cargo.message.EwbMessage.class);
        verify(shiftProcessor).processEwbUpdate(captor.capture());

        var capturedMessage = captor.getValue();
        assertEquals("", capturedMessage.status());
    }
}