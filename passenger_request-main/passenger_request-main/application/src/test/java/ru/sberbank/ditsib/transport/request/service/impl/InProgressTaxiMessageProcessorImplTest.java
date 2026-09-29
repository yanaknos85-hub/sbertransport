package ru.sberbank.ditsib.transport.request.service.impl;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.assertj.core.groups.Tuple;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.integrations.messaging.InContractorTaxiTripInProgressMessage;
import ru.sberbank.ditsib.transport.LoggingExtension;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.dao.DriverRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForTaxiRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RouteHistoryElementRepository;
import ru.sberbank.ditsib.transport.request.database.dao.TaxiTripRepository;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.driversData.Driver;
import ru.sberbank.ditsib.transport.request.dto.mapper.DriverDTOMapper;
import ru.sberbank.ditsib.transport.request.mappers.CarMapper;
import ru.sberbank.ditsib.transport.request.messaging.senders.TaxiTripSender;
import ru.sberbank.ditsib.transport.request.service.RequestService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static ru.sberbank.ditsib.transport.constants.TripRequestStatus.TAXI_AWAITING_SEARCH;
import static ru.sberbank.ditsib.transport.constants.TripRequestStatus.TAXI_TRIP_IN_PROGRESS;
import static ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus.DRIVER_ARRIVED;
import static ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus.TRIP_IN_PROGRESS;

@DisplayName("Проверка обновления данных поездки такси по сообщению из Kafka")
@ExtendWith(MockitoExtension.class)
class InProgressTaxiMessageProcessorImplTest {
    @InjectMocks
    private InProgressTaxiMessageProcessorImpl inProgressTaxiMessageProcessor;
    @Mock
    private TaxiTripRepository taxiTripRepository;
    @Mock
    private RequestForTaxiRepository requestForTaxiRepository;
    @Mock
    private TaxiTripSender taxiTripSender;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private DriverDTOMapper driverDTOMapper;
    @Mock
    private CarMapper carMapper;
    @Mock
    private DriverRepository driverRepository;
    @Mock
    private RequestService requestService;
    @Mock
    private RouteHistoryElementRepository routeHistoryElementRepository;
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(InProgressTaxiMessageProcessorImpl.class);

    @Test
    void transportType() {
        assertThat(inProgressTaxiMessageProcessor.transportType()).isEqualTo(TransportTypeEnum.TAXI);
    }

    @SneakyThrows
    @Test
    void process() {
        var message1 = Instancio.create(InContractorTaxiTripInProgressMessage.class);
        var message2 = Instancio.of(InContractorTaxiTripInProgressMessage.class)
                .set(field(InContractorTaxiTripInProgressMessage::status), DRIVER_ARRIVED)
                .create();
        var message3 = Instancio.of(InContractorTaxiTripInProgressMessage.class)
                .set(field(InContractorTaxiTripInProgressMessage::status), TRIP_IN_PROGRESS)
                .set(field(InContractorTaxiTripInProgressMessage::performerArrivalTime), LocalDateTime.now().plusDays(1))
                .create();
        var singleTaxiTrip1 = Instancio.of(SingleTaxiTrip.class)
                .set(field(SingleTaxiTrip::getStatus), TRIP_IN_PROGRESS)
                .create();
        var singleTaxiTrip2 = Instancio.of(SingleTaxiTrip.class)
                .set(field(SingleTaxiTrip::getStatus), DRIVER_ARRIVED)
                .set(field(SingleTaxiTrip::getDriver), null)
                .set(field(SingleTaxiTrip::getRequests), Instancio.ofList(RequestForTaxi.class)
                        .size(3)
                        .set(field(RequestForTaxi::getDriverArrivedDatetime), LocalDateTime.now().plusDays(2))
                        .set(field(RequestForTaxi::isSentToContractor), false)
                        .set(field(RequestForTaxi::getStatus), TAXI_AWAITING_SEARCH)
                        .create())
                .create();
        var message4 = Instancio.of(InContractorTaxiTripInProgressMessage.class)
                .set(field(InContractorTaxiTripInProgressMessage::status), TRIP_IN_PROGRESS)
                .set(field(InContractorTaxiTripInProgressMessage::performerArrivalTime), LocalDateTime.now().plusDays(2))
                .set(field(InContractorTaxiTripInProgressMessage::taxiId), null)
                .set(field(InContractorTaxiTripInProgressMessage::driver), null)
                .set(field(InContractorTaxiTripInProgressMessage::resolution), null)
                .set(field(InContractorTaxiTripInProgressMessage::geoLocation), null)
                .set(field(InContractorTaxiTripInProgressMessage::distance), null)
                .set(field(InContractorTaxiTripInProgressMessage::price), null)
                .set(field(InContractorTaxiTripInProgressMessage::waitTime), null)
                .set(field(InContractorTaxiTripInProgressMessage::waitTimeOW), null)
                .set(field(InContractorTaxiTripInProgressMessage::finishTime), null)
                .create();
        var message5 = Instancio.of(InContractorTaxiTripInProgressMessage.class)
                .set(field(InContractorTaxiTripInProgressMessage::status), TRIP_IN_PROGRESS)
                .set(field(InContractorTaxiTripInProgressMessage::performerArrivalTime), LocalDateTime.now().plusDays(2))
                .set(field(InContractorTaxiTripInProgressMessage::taxiId), null)
                .set(field(InContractorTaxiTripInProgressMessage::driver), null)
                .set(field(InContractorTaxiTripInProgressMessage::resolution), null)
                .set(field(InContractorTaxiTripInProgressMessage::geoLocation), null)
                .set(field(InContractorTaxiTripInProgressMessage::distance), null)
                .set(field(InContractorTaxiTripInProgressMessage::price), null)
                .set(field(InContractorTaxiTripInProgressMessage::waitTime), null)
                .set(field(InContractorTaxiTripInProgressMessage::waitTimeOW), null)
                .set(field(InContractorTaxiTripInProgressMessage::finishTime), null)
                .create();
        var coopTaxiTrip = Instancio.of(CoopTaxiTrip.class)
                .set(field(CoopTaxiTrip::getStatus), TRIP_IN_PROGRESS)
                .set(field(CoopTaxiTrip::getRequests), Instancio.ofList(RequestForTaxi.class)
                        .size(3)
                        .set(field(RequestForTaxi::getDriverArrivedDatetime), LocalDateTime.now())
                        .set(field(RequestForTaxi::isSentToContractor), true)
                        .set(field(RequestForTaxi::getStatus), TAXI_TRIP_IN_PROGRESS)
                        .create())
                .create();
        var carInfo = Instancio.create(CarInfo.class);
        var routeHistoryElement = Instancio.create(RouteHistoryElement.class);
        var driverObject = Instancio.create(Object.class);
        var jsonDriver = Instancio.create(String.class);
        var requestForTaxi = Instancio.create(RequestForTaxi.class);
        var deadlineState = Instancio.create(DeadlineState.class);
        var driver1 = Instancio.of(Driver.class)
                .set(field(Driver::getId), null)
                .create();
        var driver2 = Instancio.create(Driver.class);
        var existDriver = Instancio.create(Driver.class);
        var savedDriver = Instancio.create(Driver.class);
        var savedSingleTaxiTrip = Instancio.create(SingleTaxiTrip.class);

        doReturn(Collections.emptyList()).when(taxiTripRepository).findByHumanReadableId(message1.humanId());
        doReturn(Collections.singletonList(singleTaxiTrip1)).when(taxiTripRepository).findByHumanReadableId(message2.humanId());
        doReturn(Collections.singletonList(singleTaxiTrip2)).when(taxiTripRepository).findByHumanReadableId(message3.humanId());
        doReturn(Collections.singletonList(coopTaxiTrip)).when(taxiTripRepository).findByHumanReadableId(message4.humanId());
        doReturn(Collections.singletonList(coopTaxiTrip)).when(taxiTripRepository).findByHumanReadableId(message5.humanId());
        doReturn(carInfo).when(carMapper).toModel(message3.vehicle());
        doReturn(routeHistoryElement).when(routeHistoryElementRepository).save(any(RouteHistoryElement.class));
        doReturn(driverObject).when(objectMapper).convertValue(any(InContractorTaxiTripInProgressMessage.Driver.class), ArgumentMatchers.<TypeReference<Object>>any());
        doReturn(jsonDriver).when(objectMapper).writeValueAsString(driverObject);
        doReturn(requestForTaxi).when(requestForTaxiRepository).save(any(RequestForTaxi.class));
        doReturn(requestForTaxi).when(requestService).changeState(any(RequestForTaxi.class), any(TripRequestStatus.class));
        doReturn(deadlineState).when(requestService).calcDriverArrivedDeadline(any(TransportTypeEnum.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class));
        doReturn(driver1).when(driverDTOMapper).map(message3.driver());
        doReturn(driver2).when(driverDTOMapper).map(message5.driver());
        doReturn(Collections.singletonList(existDriver), Collections.singletonList(existDriver), Collections.emptyList()).when(driverRepository).findByLastNameAndFirstNameAndPatronymicAndContactPhoneAndActive(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyBoolean());
        doReturn(Optional.of(driver2)).when(driverRepository).findById(driver2.getId());
        doReturn(savedDriver).when(driverRepository).save(any(Driver.class));
        doReturn(savedSingleTaxiTrip).when(taxiTripRepository).save(any(TaxiTrip.class));
        doNothing().when(taxiTripSender).send(savedSingleTaxiTrip);

        inProgressTaxiMessageProcessor.process(message1);
        inProgressTaxiMessageProcessor.process(message2);
        inProgressTaxiMessageProcessor.process(message3);
        inProgressTaxiMessageProcessor.process(message4);
        inProgressTaxiMessageProcessor.process(message5);
        verify(taxiTripRepository, times(5)).findByHumanReadableId(anyString());
        verify(carMapper, times(1)).toModel(any(InContractorTaxiTripInProgressMessage.Vehicle.class));
        verify(routeHistoryElementRepository, times(1)).save(any(RouteHistoryElement.class));
        verify(objectMapper, times(1)).convertValue(any(InContractorTaxiTripInProgressMessage.Driver.class), ArgumentMatchers.<TypeReference<Object>>any());
        verify(objectMapper, times(1)).writeValueAsString(any());

        verify(requestForTaxiRepository, times(6)).save(any(RequestForTaxi.class));
        verify(requestService, never()).finish(any(RequestForTaxi.class), any(Employee.class));
        verify(requestService, times(6)).changeState(any(RequestForTaxi.class), any(TripRequestStatus.class));
        verify(requestService, times(3)).calcDriverArrivedDeadline(any(TransportTypeEnum.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class));
        verify(driverDTOMapper, times(3)).map(any(InContractorTaxiTripInProgressMessage.Driver.class));
        verify(driverRepository, times(3)).findByLastNameAndFirstNameAndPatronymicAndContactPhoneAndActive(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyBoolean());
        verify(driverRepository, times(4)).findById(any(UUID.class));
        verify(driverRepository).save(any(Driver.class));
        verify(taxiTripRepository).save(any(TaxiTrip.class));
        verify(taxiTripSender).send(any(SingleTaxiTrip.class));
        LOGGING_EXTENSION.assertLogEvents(11, new Tuple[] {
                tuple(
                        Level.INFO,
                        "Taxi trip for humanReadableId %s not found"
                                .formatted(message1.humanId()),
                        false
                ),
                tuple(
                        Level.INFO,
                        "Поездка на такси %s имеет статус %s, изменение на статус %s невозможно, данные из InContractorTaxiTripInProgressMessage с id %s были проигнорированы"
                                .formatted(singleTaxiTrip1.getHumanReadableId(),
                                        singleTaxiTrip1.getStatus(),
                                        message2.status(),
                                        message2.getId()),
                        false
                ),
                tuple(
                        Level.DEBUG,
                        "Processing taxi trip driver data: jsonDriver = %s"
                                .formatted(jsonDriver),
                        false
                ),
                tuple(
                        Level.DEBUG,
                        "Start save request",
                        false
                ),
                tuple(
                        Level.DEBUG,
                        "Start save request",
                        false
                ),
                tuple(
                        Level.DEBUG,
                        "Start save request",
                        false
                ),
                tuple(
                        Level.INFO,
                        "Updated taxiTrip with id %s"
                                .formatted(singleTaxiTrip2.getId()),
                        false
                ),
                tuple(
                        Level.DEBUG,
                        "Start save request",
                        false
                ),
                tuple(
                        Level.DEBUG,
                        "Start save request",
                        false
                ),
                tuple(
                        Level.DEBUG,
                        "Start save request",
                        false
                ),
                tuple(
                        Level.INFO,
                        "Предполагаемый статус заявки, рассчитаный на основании статуса поездки, пуст или равен статусу заявки, tripRequestStatus:TAXI_TRIP_IN_PROGRESS",
                        false
                )
        });
    }
}
