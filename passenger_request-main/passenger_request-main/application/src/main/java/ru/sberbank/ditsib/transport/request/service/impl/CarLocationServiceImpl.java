package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.ws.messages.WebSocketMessage;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.dao.CarLocationTaskRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForTaxiRepository;
import ru.sberbank.ditsib.transport.request.database.model.CarLocationTask;
import ru.sberbank.ditsib.transport.request.database.model.RequestForTaxi;
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
import ru.sberbank.ditsib.transport.request.service.CarLocationService;
import ru.sberbank.ditsib.transport.request.service.ContractorService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.sberbank.ditsib.transport.request.exceptions.CarLocationTaskNotFoundException.ORDER_PARTNER_ID_NOT_FOUND;
import static ru.sberbank.ditsib.transport.request.exceptions.CarLocationTaskNotFoundException.REQUEST_ID_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarLocationServiceImpl implements CarLocationService {

    private final CarLocationTaskRepository carLocationTaskRepository;
    private final RequestForTaxiRepository requestForTaxiRepository;
    private final ContractorService contractorService;
    private final CarLocationSender carLocationSender;
    private final NotificationSender notificationSender;
    private final Clock clock;

    private static final Set<TripRequestStatus> CAR_LOCATION_ACCESS_STATUSES = Set.of(
            TripRequestStatus.TAXI_DRIVER_FOUND,
            TripRequestStatus.TAXI_DRIVER_ON_THE_WAY,
            TripRequestStatus.TAXI_DRIVER_ARRIVED
    );
    private static final String SUBSCRIPTION_NAME = "CAR_LOCATION";

    @Override
    @Transactional
    public void addRequestToTask(UUID requestId) {
        var request = requestForTaxiRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException(RequestForTaxi.class, requestId));

        if (!CAR_LOCATION_ACCESS_STATUSES.contains(request.getStatus())) {
            throw new CarLocationRequestWrongStatusException(requestId, request.getStatus());
        }
        var now = LocalDateTime.now(clock);
        if (!request.getDesiredDate().minusMinutes(60).isBefore(now) ||
                !request.getDesiredDate().plusMinutes(30).isAfter(now)) {
            throw new CarLocationDesiredDateNotInTimeRangeException(requestId, request.getDesiredDate(), now);
        }

        var carLocationTask = new CarLocationTask();
        carLocationTask.setRequestId(requestId);
        carLocationTask.setOrderPartnerId(request.getTaxiTrip().getTaxiId());
        carLocationTask.setContractorId(request.getContractorId());
        carLocationTask.setCreatedAt(now);
        carLocationTask.setActive(true);

        carLocationTaskRepository.save(carLocationTask);
    }

    @Override
    @Transactional
    public void sendRequestBatch() {
        var carLocationTasks = carLocationTaskRepository.findAllByActiveIsTrue();
        if (!carLocationTasks.isEmpty()) {
            var requestIds = carLocationTasks.stream()
                    .map(CarLocationTask::getRequestId)
                    .toList();
            var contractors = contractorService.getAllByRequestIds(requestIds);
            var contractorRequestMap = groupOrderPartnerIdsByContractor(carLocationTasks, contractors);

            log.info("Sending batch of {} requests to contractors", contractorRequestMap.size());
            carLocationSender.send(new CarLocationMessage(UUID.randomUUID(), contractorRequestMap));
        }
    }

    @Override
    public void sendLocationToSubscriber(OrderLocationDto orderLocation) {
        var carLocationTasks = carLocationTaskRepository.findByOrderPartnerIdAndActiveIsTrue(orderLocation.orderPartnerId())
                .stream()
                .max(Comparator.comparing(CarLocationTask::getCreatedAt))
                .orElseThrow(
                        () -> new CarLocationTaskNotFoundException(
                                ORDER_PARTNER_ID_NOT_FOUND.formatted(orderLocation.orderPartnerId())
                        )
                );
        var request = requestForTaxiRepository.findById(carLocationTasks.getRequestId())
                .orElseThrow(() -> new EntityNotFoundException(RequestForTaxi.class, carLocationTasks.getRequestId()));

        if (request.getStatus().equals(TripRequestStatus.TAXI_TRIP_IN_PROGRESS)) {
            carLocationTasks.setActive(false);
            carLocationTaskRepository.save(carLocationTasks);
        }
        notificationSender.sendNotificationToSubscriber(
                new WebSocketMessage(
                        SUBSCRIPTION_NAME,
                        request.getId(),
                        new CarLocationNotificationMessage(
                                request.getStatus(),
                                orderLocation.coordinates() != null ?
                                new LocationMessage(
                                        orderLocation.coordinates().longitude(),
                                        orderLocation.coordinates().latitude()

                                ) : null,
                                orderLocation.duration()
                        )
                )
        );
    }

    @Override
    @Transactional
    public void deactivateTask(UUID requestId) {
        var carLocationTask = carLocationTaskRepository.findAllByRequestIdAndActiveIsTrue(requestId)
                .stream()
                .min(Comparator.comparing(CarLocationTask::getCreatedAt))
                .orElseThrow(() -> new CarLocationTaskNotFoundException(REQUEST_ID_NOT_FOUND.formatted(requestId)));
        carLocationTask.setActive(false);
        carLocationTaskRepository.save(carLocationTask);
    }

    @Override
    @Transactional
    public void deleteExpiredTasks() {
        var now = LocalDateTime.now(clock);
        carLocationTaskRepository.deleteAllByCreatedAtBefore(now.minusDays(5));
    }

    private Map<UUID, CarLocationMessage.ContractorInfo> groupOrderPartnerIdsByContractor(
            List<CarLocationTask> carLocationTasks,
            Map<UUID, Contractor> contractors
    ) {
        var orderIdsByContractorId = carLocationTasks.stream()
                .filter(task -> task.getContractorId() != null && task.getOrderPartnerId() != null)
                .collect(Collectors.groupingBy(
                        CarLocationTask::getContractorId,
                        Collectors.mapping(
                                CarLocationTask::getOrderPartnerId,
                                Collectors.toList()
                        )
                ));

        return orderIdsByContractorId.entrySet().stream()
                .filter(entry -> contractors.containsKey(entry.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            var contractor = contractors.get(entry.getKey());
                            return new CarLocationMessage.ContractorInfo(
                                    contractor.getUrl(),
                                    contractor.getLogin(),
                                    contractor.getPassword(),
                                    entry.getValue()
                            );
                        }
                ));
    }
}
