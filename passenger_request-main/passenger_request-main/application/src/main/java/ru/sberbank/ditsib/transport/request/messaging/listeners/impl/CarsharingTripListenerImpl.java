package ru.sberbank.ditsib.transport.request.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.integrations.carsharing.messages.CarsharingDataMessage;
import ru.sberbank.ditsib.transport.request.database.dao.CarsharingTripRepository;
import ru.sberbank.ditsib.transport.request.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForCarsharingRepository;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingTrip;
import ru.sberbank.ditsib.transport.request.exceptions.CarsharingException;
import ru.sberbank.ditsib.transport.request.mappers.CarsharingTripMapper;
import ru.sberbank.ditsib.transport.request.service.RequestService;

import java.util.Set;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Slf4j
@Transactional
public class CarsharingTripListenerImpl implements Consumer<Message<CarsharingDataMessage>> {
    
    private final CarsharingTripRepository repository;
    
    private final RequestForCarsharingRepository requestForCarsharingRepository;
    
    private final EmployeeRepository employeeRepository;
    
    private final CarsharingTripMapper mapper;
    
    private final RequestService service;
    
    private static final Set<TripRequestStatus> statuses = Set.of(TripRequestStatus.CARSHARING_AWAITING_APPROVAL,
                                                                  TripRequestStatus.CARSHARING_APPROVED);

    public void accept(Message<CarsharingDataMessage> message) {
        handle(message.getPayload());
    }
    
    private void handle(CarsharingDataMessage message) {
        log.info("Получено новое сообщение CarsharingDataMessage id {}, rentId {}", message.getId(), message.getRentId());
        var data = repository.findFirstByRentId(message.getRentId()).orElseGet(() -> newCarsharingData(message));
        var request = data.getRequest();
        mapper.toModel(message, data);
        data = repository.saveAndFlush(data);
        
        if (TripRequestStatus.CARSHARING_AWAITING_APPROVAL.equals(request.getStatus())) {
            log.warn("Заявка находится в статусе на согласовании requestId: {} Фактические данные сохранены, но не обработаны carsharingTipId: {}",
                     request.getId(), data.getId());
            return;
        }
        
        if (message.getRentCreatedAt() != null && !request.getStatus().isTerminal()) {
            service.changeState(request, TripRequestStatus.CARSHARING_TRIP_IN_PROGRESS, request.getAuthor());
        } else {
            log.debug("Не удалось изменить статус, так как заявка {} находится в статусе {}",
                      request.getHumanReadableId(),
                      request.getStatus());
        }
        if (message.getRentFinishedAt() != null && !request.getStatus().isTerminal()) {
            service.complete(request, request.getAuthor());
        } else {
            log.debug("Не удалось изменить статус, так как заявка {} находится в статусе {}",
                      request.getHumanReadableId(),
                      request.getStatus());
        }
    }
    
    private CarsharingTrip newCarsharingData(CarsharingDataMessage message) {
        var data = CarsharingTrip.builder();
        if (message.getPhoneNumber() == null) {
            throw new CarsharingException("Нет номера телефона " + message.getId());
        }
        var employee = employeeRepository.findFirstByMobilePhone(message.getPhoneNumber())
                                         .orElseThrow(() -> new CarsharingException(
                                                 "Не найден пользователь с таким номером телефона " +
                                                 message.getPhoneNumber()));
        var request = requestForCarsharingRepository.findFirstByAuthorAndStatusInOrderByCreationTimeDesc(employee, statuses)
                                                    .orElseThrow(() -> new CarsharingException(
                                                            "Не найдена активная поездка для " + employee.getHumanReadableId()));
        data.request(request);
        return data.build();
    }
}
