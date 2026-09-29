package ru.sberbank.ditsib.transport.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.StringUtils;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.ws.messages.WebSocketMessage;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.dao.CarLocationTaskRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForTaxiRepository;
import ru.sberbank.ditsib.transport.request.database.model.CarLocationTask;
import ru.sberbank.ditsib.transport.request.database.model.RequestForTaxi;
import ru.sberbank.ditsib.transport.request.database.model.TaxiTrip;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.Contractor;
import ru.sberbank.ditsib.transport.request.dto.OrderLocationDto;
import ru.sberbank.ditsib.transport.request.exceptions.CarLocationDesiredDateNotInTimeRangeException;
import ru.sberbank.ditsib.transport.request.exceptions.CarLocationRequestWrongStatusException;
import ru.sberbank.ditsib.transport.request.exceptions.CarLocationTaskNotFoundException;
import ru.sberbank.ditsib.transport.request.messaging.message.CarLocationMessage;
import ru.sberbank.ditsib.transport.request.messaging.message.CarLocationNotificationMessage;
import ru.sberbank.ditsib.transport.request.messaging.message.CarLocationNotificationMessage.LocationMessage;
import ru.sberbank.ditsib.transport.request.messaging.senders.CarLocationSender;
import ru.sberbank.ditsib.transport.request.messaging.senders.NotificationSender;
import ru.sberbank.ditsib.transport.request.service.ContractorService;
import ru.sberbank.ditsib.transport.request.service.impl.CarLocationServiceImpl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarLocationServiceImplTest {

    @Mock
    private CarLocationTaskRepository carLocationTaskRepository;
    @Mock
    private RequestForTaxiRepository requestForTaxiRepository;
    @Mock
    private ContractorService contractorService;
    @Mock
    private CarLocationSender carLocationSender;
    @Mock
    private NotificationSender notificationSender;
    @Mock
    private Clock clock;

    @InjectMocks
    private CarLocationServiceImpl carLocationService;

    @Captor
    private ArgumentCaptor<CarLocationTask> carLocationTaskArgumentCaptor;
    @Captor
    private ArgumentCaptor<CarLocationMessage> carLocationMessageArgumentCaptor;
    @Captor
    private ArgumentCaptor<WebSocketMessage> webSocketMessageArgumentCaptor;

    private static final LocalDateTime NOW = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
    private static final Clock FIXED_CLOCK = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

    @Test
    void addRequestToTask() {
        doReturn(FIXED_CLOCK.instant()).when(clock).instant();
        doReturn(FIXED_CLOCK.getZone()).when(clock).getZone();

        var requestId = UUID.randomUUID();

        var requestForTaxi = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getTaxiTrip), Instancio.of(TaxiTrip.class)
                        .set(field(TaxiTrip::getTaxiId), "id")
                        .create()
                )
                .set(field(RequestForTaxi::getStatus), TripRequestStatus.TAXI_DRIVER_ON_THE_WAY)
                .set(field(RequestForTaxi::getDesiredDate), LocalDateTime.now(FIXED_CLOCK).plusMinutes(30))
                .create();
        doReturn(Optional.of(requestForTaxi)).when(requestForTaxiRepository).findById(requestId);

        carLocationService.addRequestToTask(requestId);
        verify(carLocationTaskRepository).save(carLocationTaskArgumentCaptor.capture());

        var savedCarLocationTask = carLocationTaskArgumentCaptor.getValue();
        assertThat(savedCarLocationTask.getRequestId()).isEqualTo(requestId);
        assertThat(savedCarLocationTask.getOrderPartnerId()).isEqualTo("id");
        assertThat(savedCarLocationTask.getCreatedAt()).isEqualTo(NOW);
        assertThat(savedCarLocationTask.isActive()).isTrue();
    }

    @MethodSource
    @ParameterizedTest(name = "{0}")
    void addRequestToTaskThrowsExceptions(
            String name,
            UUID requestId,
            RequestForTaxi request,
            Class<? extends Exception> exceptionClazz,
            String message
    ) {
        lenient().doReturn(FIXED_CLOCK.instant()).when(clock).instant();
        lenient().doReturn(FIXED_CLOCK.getZone()).when(clock).getZone();

        doReturn(Optional.ofNullable(request)).when(requestForTaxiRepository).findById(requestId);

        assertThatExceptionOfType(exceptionClazz)
                .isThrownBy(() -> carLocationService.addRequestToTask(requestId))
                .withMessage(message);

    }

    static Stream<Arguments> addRequestToTaskThrowsExceptions() {
        var requestId = UUID.randomUUID();
        return Stream.of(
                Arguments.of(
                        "Request for taxi not found",
                        requestId,
                        null,
                        EntityNotFoundException.class,
                        "Data not found: Entity: %s, ID: %s".formatted(RequestForTaxi.class.getSimpleName(), requestId)
                ),
                Arguments.of(
                        "Request for taxi has wrong status",
                        requestId,
                        Instancio.of(RequestForTaxi.class)
                                .set(field(RequestForTaxi::getStatus), TripRequestStatus.REPAIR_FINISHED)
                                .create(),
                        CarLocationRequestWrongStatusException.class,
                        "Невозможно получить местоположение автомобиля. Заявка requestId=%s в статусе %s".formatted(requestId, TripRequestStatus.REPAIR_FINISHED.getDescription())
                ),
                Arguments.of(
                        "Request for taxi has desired date more then 60 minutes",
                        requestId,
                        Instancio.of(RequestForTaxi.class)
                                .set(field(RequestForTaxi::getStatus), TripRequestStatus.TAXI_DRIVER_ON_THE_WAY)
                                .set(field(RequestForTaxi::getDesiredDate), LocalDateTime.now(FIXED_CLOCK).plusMinutes(61))
                                .create(),
                        CarLocationDesiredDateNotInTimeRangeException.class,
                        """
                                Дата и время подачи такси для заявки requestId=%s не входит в диапазон времени - за 60 минут до подачи, и 30 минут после.
                                Дата и время подачи такси: %s. Текущее время: %s.
                                """.formatted(requestId, NOW.plusMinutes(61), NOW)
                ),
                Arguments.of(
                        "Request for taxi has desired date less then 30 minutes",
                        requestId,
                        Instancio.of(RequestForTaxi.class)
                                .set(field(RequestForTaxi::getStatus), TripRequestStatus.TAXI_DRIVER_ON_THE_WAY)
                                .set(field(RequestForTaxi::getDesiredDate), LocalDateTime.now(FIXED_CLOCK).minusMinutes(31))
                                .create(),
                        CarLocationDesiredDateNotInTimeRangeException.class,
                        """
                                Дата и время подачи такси для заявки requestId=%s не входит в диапазон времени - за 60 минут до подачи, и 30 минут после.
                                Дата и время подачи такси: %s. Текущее время: %s.
                                """.formatted(requestId, NOW.minusMinutes(31), NOW)
                )
        );
    }

    @Test
    void sendRequestBatch() {
        var contractor = new HashMap<UUID, Contractor>();
        var contractorId = UUID.randomUUID();
        contractor.put(contractorId, Instancio.of(Contractor.class)
                .set(field(Contractor::getId), contractorId)
                .create());
        var carLocationTasks = List.of(
                Instancio.of(CarLocationTask.class)
                        .set(field(CarLocationTask::isActive), true)
                        .set(field(CarLocationTask::getContractorId), contractorId)
                        .create(),
                Instancio.of(CarLocationTask.class)
                        .set(field(CarLocationTask::isActive), true)
                        .set(field(CarLocationTask::getContractorId), contractorId)
                        .create()
                );

        doReturn(carLocationTasks).when(carLocationTaskRepository).findAllByActiveIsTrue();
        doReturn(contractor).when(contractorService).getAllByRequestIds(carLocationTasks.stream()
                .map(CarLocationTask::getRequestId)
                .toList());

        carLocationService.sendRequestBatch();

        verify(carLocationSender).send(carLocationMessageArgumentCaptor.capture());
        var contractorRequests = carLocationMessageArgumentCaptor.getValue().contractorRequests();
        assertThat(contractorRequests)
                .containsEntry((contractorId),
                        new CarLocationMessage.ContractorInfo(
                                contractor.get(contractorId).getUrl(),
                                contractor.get(contractorId).getLogin(),
                                contractor.get(contractorId).getPassword(),
                                carLocationTasks.stream()
                                        .map(CarLocationTask::getOrderPartnerId)
                                        .filter(StringUtils::hasText)
                                        .toList()
                        )
                );

        doReturn(emptyList()).when(carLocationTaskRepository).findAllByActiveIsTrue();

        carLocationService.sendRequestBatch();
        verify(carLocationSender).send(any(CarLocationMessage.class));
    }

    @Test
    void sendLocationToSubscriber() {
        var orderPartnerId = "id";
        var orderLocation = Instancio.of(OrderLocationDto.class)
                .set(field(OrderLocationDto::orderPartnerId), orderPartnerId)
                .create();
        var carLocationTask = Instancio.of(CarLocationTask.class)
                .set(field(CarLocationTask::getOrderPartnerId), orderPartnerId)
                .set(field(CarLocationTask::isActive), true)
                .create();

        doReturn(List.of()).when(carLocationTaskRepository).findByOrderPartnerIdAndActiveIsTrue(orderLocation.orderPartnerId());
        assertThatExceptionOfType(CarLocationTaskNotFoundException.class)
                .isThrownBy(() -> carLocationService.sendLocationToSubscriber(orderLocation))
                .withMessage("Car location task with orderPartnerId=%s not found".formatted(orderLocation.orderPartnerId()));

        doReturn(List.of(carLocationTask)).when(carLocationTaskRepository).findByOrderPartnerIdAndActiveIsTrue(orderLocation.orderPartnerId());

        doReturn(Optional.empty()).when(requestForTaxiRepository).findById(carLocationTask.getRequestId());
        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> carLocationService.sendLocationToSubscriber(orderLocation))
                .withMessage("Data not found: Entity: %s, ID: %s".formatted(RequestForTaxi.class.getSimpleName(), carLocationTask.getRequestId()));

        var requestForTaxi = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getStatus), TripRequestStatus.TAXI_TRIP_IN_PROGRESS)
                .create();
        doReturn(Optional.of(requestForTaxi)).when(requestForTaxiRepository).findById(carLocationTask.getRequestId());
        carLocationService.sendLocationToSubscriber(orderLocation);
        verify(carLocationTaskRepository).save(carLocationTaskArgumentCaptor.capture());
        verify(notificationSender).sendNotificationToSubscriber(webSocketMessageArgumentCaptor.capture());

        var capturedCarLocationTask = carLocationTaskArgumentCaptor.getValue();
        assertThat(capturedCarLocationTask)
                .extracting(
                        CarLocationTask::getId,
                        CarLocationTask::getRequestId,
                        CarLocationTask::getOrderPartnerId,
                        CarLocationTask::getContractorId,
                        CarLocationTask::getCreatedAt,
                        CarLocationTask::isActive
                )
                .containsExactly(
                        carLocationTask.getId(),
                        carLocationTask.getRequestId(),
                        carLocationTask.getOrderPartnerId(),
                        carLocationTask.getContractorId(),
                        carLocationTask.getCreatedAt(),
                        false
                );
        var capturedMessage = webSocketMessageArgumentCaptor.getValue();
        assertThat(capturedMessage)
                .isNotNull()
                .extracting(
                        WebSocketMessage::subscription,
                        WebSocketMessage::resourceId,
                        WebSocketMessage::payload
                )
                .containsExactly(
                        "CAR_LOCATION",
                        requestForTaxi.getId(),
                        new CarLocationNotificationMessage(
                                requestForTaxi.getStatus(),
                                new LocationMessage(
                                        orderLocation.coordinates().longitude(),
                                        orderLocation.coordinates().latitude()
                                ),
                                orderLocation.duration()
                        )
                );

        var orderLocation2 = Instancio.of(OrderLocationDto.class)
                .set(field(OrderLocationDto::orderPartnerId), orderPartnerId)
                .set(field(OrderLocationDto::coordinates), null)
                .set(field(OrderLocationDto::duration), -1)
                .create();
        carLocationService.sendLocationToSubscriber(orderLocation2);

        verify(notificationSender, times(2)).sendNotificationToSubscriber(webSocketMessageArgumentCaptor.capture());
        capturedMessage = webSocketMessageArgumentCaptor.getValue();
        assertThat(capturedMessage)
                .isNotNull()
                .extracting(
                        WebSocketMessage::subscription,
                        WebSocketMessage::resourceId,
                        WebSocketMessage::payload
                )
                .containsExactly(
                        "CAR_LOCATION",
                        requestForTaxi.getId(),
                        new CarLocationNotificationMessage(
                                requestForTaxi.getStatus(),
                                null,
                                orderLocation2.duration()
                        )
                );
    }

    @Test
    void deactivateTask() {
        var requestId = UUID.randomUUID();

        doReturn(List.of()).when(carLocationTaskRepository).findAllByRequestIdAndActiveIsTrue(requestId);
        assertThatExceptionOfType(CarLocationTaskNotFoundException.class)
                .isThrownBy(() -> carLocationService.deactivateTask(requestId))
                .withMessage("Car location task with requestId=%s not found".formatted(requestId));

        var carLocationTask = Instancio.of(CarLocationTask.class)
                .set(field(CarLocationTask::isActive), true)
                .create();
        doReturn(List.of(carLocationTask)).when(carLocationTaskRepository).findAllByRequestIdAndActiveIsTrue(requestId);

        carLocationService.deactivateTask(requestId);

        verify(carLocationTaskRepository).save(carLocationTaskArgumentCaptor.capture());
        var capturedCarLocationTask = carLocationTaskArgumentCaptor.getValue();
        assertThat(capturedCarLocationTask)
                .extracting(
                        CarLocationTask::getId,
                        CarLocationTask::getRequestId,
                        CarLocationTask::getOrderPartnerId,
                        CarLocationTask::getContractorId,
                        CarLocationTask::getCreatedAt,
                        CarLocationTask::isActive
                )
                .containsExactly(
                        carLocationTask.getId(),
                        carLocationTask.getRequestId(),
                        carLocationTask.getOrderPartnerId(),
                        carLocationTask.getContractorId(),
                        carLocationTask.getCreatedAt(),
                        false
                );
    }
}
