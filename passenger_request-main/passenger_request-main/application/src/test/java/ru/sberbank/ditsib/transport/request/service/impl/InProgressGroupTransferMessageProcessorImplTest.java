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
import ru.sberbank.ditsib.transport.request.database.dao.GroupTransferTripRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForGroupTransferRepository;
import ru.sberbank.ditsib.transport.request.database.model.GroupTransferTrip;
import ru.sberbank.ditsib.transport.request.database.model.RequestForGroupTransfer;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.driversData.Driver;
import ru.sberbank.ditsib.transport.request.dto.mapper.DriverDTOMapper;
import ru.sberbank.ditsib.transport.request.mappers.CarMapper;
import ru.sberbank.ditsib.transport.request.service.RequestService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static ru.sberbank.ditsib.transport.constants.TripRequestStatus.GROUP_TRANSFER_AWAITING_SEARCH;
import static ru.sberbank.ditsib.transport.constants.TripRequestStatus.GROUP_TRANSFER_TRIP_IN_PROGRESS;
import static ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus.DRIVER_ARRIVED;
import static ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus.TRIP_IN_PROGRESS;

@DisplayName("Проверка обновления данных групповой поездки по сообщению из Kafka")
@ExtendWith(MockitoExtension.class)
class InProgressGroupTransferMessageProcessorImplTest {
    @InjectMocks
    private InProgressGroupTransferMessageProcessorImpl inProgressGroupTransferMessageProcessor;
    @Mock
    private RequestForGroupTransferRepository requestForGroupTransferRepository;
    @Mock
    private DriverRepository driverRepository;
    @Mock
    private RequestService requestService;
    @Mock
    private CarMapper carMapper;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private DriverDTOMapper driverDTOMapper;
    @Mock
    private GroupTransferTripRepository groupTransferTripRepository;
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(InProgressGroupTransferMessageProcessorImpl.class);

    @SneakyThrows
    @Test
    void transportType() {
        assertThat(inProgressGroupTransferMessageProcessor.transportType()).isEqualTo(TransportTypeEnum.GROUP_TRANSFER);
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
        var groupTransferTrip1 = Instancio.of(GroupTransferTrip.class)
                .set(field(GroupTransferTrip::getStatus), TRIP_IN_PROGRESS)
                .create();
        var groupTransferTrip2 = Instancio.of(GroupTransferTrip.class)
                .set(field(GroupTransferTrip::getStatus), DRIVER_ARRIVED)
                .set(field(GroupTransferTrip::getDriver), null)
                .set(field(GroupTransferTrip::getAssignedCar), null)
                .set(field(GroupTransferTrip::getRequest), Instancio.of(RequestForGroupTransfer.class)
                        .set(field(RequestForGroupTransfer::getDriverArrivedDatetime), LocalDateTime.now().plusDays(2))
                        .set(field(RequestForGroupTransfer::isSentToContractor), false)
                        .set(field(RequestForGroupTransfer::getStatus), GROUP_TRANSFER_AWAITING_SEARCH)
                        .create())
                .create();
        var message4 = Instancio.of(InContractorTaxiTripInProgressMessage.class)
                .set(field(InContractorTaxiTripInProgressMessage::status), TRIP_IN_PROGRESS)
                .set(field(InContractorTaxiTripInProgressMessage::performerArrivalTime), LocalDateTime.now().plusDays(2))
                .set(field(InContractorTaxiTripInProgressMessage::taxiId), UUID.randomUUID().toString())
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
                .set(field(InContractorTaxiTripInProgressMessage::performerArrivalTime), LocalDateTime.now().plusDays(1))
                .create();
        var message6 = Instancio.of(InContractorTaxiTripInProgressMessage.class)
                .set(field(InContractorTaxiTripInProgressMessage::status), TRIP_IN_PROGRESS)
                .set(field(InContractorTaxiTripInProgressMessage::performerArrivalTime), LocalDateTime.now().plusDays(1))
                .create();
        var groupTransferTrip3 = Instancio.of(GroupTransferTrip.class)
                .set(field(GroupTransferTrip::getStatus), TRIP_IN_PROGRESS)
                .set(field(GroupTransferTrip::getGroupTransferId), null)
                .set(field(GroupTransferTrip::getRequest), Instancio.of(RequestForGroupTransfer.class)
                        .set(field(RequestForGroupTransfer::getDriverArrivedDatetime), LocalDateTime.now())
                        .set(field(RequestForGroupTransfer::isSentToContractor), true)
                        .set(field(RequestForGroupTransfer::getStatus), GROUP_TRANSFER_TRIP_IN_PROGRESS)
                        .create())
                .create();
        var driverObject = Instancio.create(Object.class);
        var jsonDriver = Instancio.create(String.class);
        var requestForGroupTransfer = Instancio.create(RequestForGroupTransfer.class);
        var deadlineState = Instancio.create(DeadlineState.class);
        var driver1 = Instancio.create(ru.sberbank.ditsib.transport.request.database.model.driversData.Driver.class);
        var driver2 = Instancio.of(ru.sberbank.ditsib.transport.request.database.model.driversData.Driver.class)
                .set(field(ru.sberbank.ditsib.transport.request.database.model.driversData.Driver::getId), null)
                .create();
        var existDriver = Instancio.create(Driver.class);
        var groupTransferTrip = Instancio.create(GroupTransferTrip.class);

        doReturn(Optional.empty()).when(groupTransferTripRepository).findFirstByHumanReadableId(message1.humanId());
        doReturn(Optional.of(groupTransferTrip1)).when(groupTransferTripRepository).findFirstByHumanReadableId(message2.humanId());
        doReturn(Optional.of(groupTransferTrip2)).when(groupTransferTripRepository).findFirstByHumanReadableId(message3.humanId());
        doReturn(Optional.of(groupTransferTrip3)).when(groupTransferTripRepository).findFirstByHumanReadableId(message4.humanId());
        doReturn(Optional.of(groupTransferTrip2)).when(groupTransferTripRepository).findFirstByHumanReadableId(message5.humanId());
        doReturn(Optional.of(groupTransferTrip2)).when(groupTransferTripRepository).findFirstByHumanReadableId(message6.humanId());
        doReturn(driverObject).when(objectMapper).convertValue(any(InContractorTaxiTripInProgressMessage.Driver.class), ArgumentMatchers.<TypeReference<Object>>any());
        doReturn(jsonDriver).when(objectMapper).writeValueAsString(driverObject);
        doReturn(requestForGroupTransfer).when(requestForGroupTransferRepository).save(any(RequestForGroupTransfer.class));
        doReturn(requestForGroupTransfer).when(requestService).changeState(any(RequestForGroupTransfer.class), any(TripRequestStatus.class));
        doReturn(deadlineState).when(requestService).calcDriverArrivedDeadline(any(TransportTypeEnum.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class));
        doReturn(driver1).when(driverDTOMapper).map(message3.driver());
        doReturn(null).when(driverDTOMapper).map(message4.driver());
        doReturn(driver2).when(driverDTOMapper).map(message5.driver());
        doReturn(driver1).when(driverDTOMapper).map(message6.driver());
        doReturn(Collections.singletonList(existDriver), Collections.emptyList(), Collections.emptyList()).when(driverRepository).findByLastNameAndFirstNameAndPatronymicAndContactPhoneAndActive(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyBoolean());
        doReturn(groupTransferTrip).when(groupTransferTripRepository).save(any(GroupTransferTrip.class));

        inProgressGroupTransferMessageProcessor.process(message1);
        inProgressGroupTransferMessageProcessor.process(message2);
        inProgressGroupTransferMessageProcessor.process(message3);
        inProgressGroupTransferMessageProcessor.process(message4);
        inProgressGroupTransferMessageProcessor.process(message5);
        inProgressGroupTransferMessageProcessor.process(message6);
        verify(groupTransferTripRepository, times(6)).findFirstByHumanReadableId(anyString());
        verify(objectMapper, times(1)).convertValue(any(InContractorTaxiTripInProgressMessage.Driver.class), ArgumentMatchers.<TypeReference<Object>>any());
        verify(objectMapper, times(1)).writeValueAsString(any());

        verify(requestForGroupTransferRepository, times(4)).save(any(RequestForGroupTransfer.class));
        verify(requestService, never()).finish(any(RequestForGroupTransfer.class), any(Employee.class));
        verify(requestService, times(4)).changeState(any(RequestForGroupTransfer.class), any(TripRequestStatus.class));
        verify(requestService, times(1)).calcDriverArrivedDeadline(any(TransportTypeEnum.class),
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
        verify(driverRepository, times(2)).save(any(Driver.class));
        verify(groupTransferTripRepository, times(4)).save(any(GroupTransferTrip.class));
        LOGGING_EXTENSION.assertLogEvents(7, new Tuple[]{
                tuple(
                        Level.INFO,
                        "Transfer trip for humanreadableid %s not found"
                                .formatted(message1.humanId()),
                        false
                ),
                tuple(
                        Level.INFO,
                        "Поездка на трансфере %s имеет статус %s, изменение на статус %s невозможно, данные из InContractorTaxiTripDoneMessage с id %s были проигнорированы"
                                .formatted(groupTransferTrip1.getHumanReadableId(),
                                        groupTransferTrip1.getStatus(),
                                        message2.status(),
                                        message2.getId()),
                        false
                ),
                tuple(
                        Level.DEBUG,
                        "InContractorTaxiTripInProgressListenerImpl: processTransfer: jsonDriver = %s"
                                .formatted(jsonDriver),
                        false
                ),
                tuple(
                        Level.INFO,
                        "Updated transfer trip with id %s"
                                .formatted(groupTransferTrip2.getId()),
                        false
                ),
                tuple(
                        Level.INFO,
                        "Updated transfer trip with id %s"
                                .formatted(groupTransferTrip3.getId()),
                        false
                ),
                tuple(
                        Level.INFO,
                        "Updated transfer trip with id %s"
                                .formatted(groupTransferTrip2.getId()),
                        false
                ),
                tuple(
                        Level.INFO,
                        "Updated transfer trip with id %s"
                                .formatted(groupTransferTrip2.getId()),
                        false
                )
        });
    }
}
