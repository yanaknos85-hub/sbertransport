package ru.sberbank.ditsib.transport.request.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import ru.sber.transport.contractor.messages.ContractorUpdateTripMessage;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;
import ru.sberbank.ditsib.transport.request.database.dao.DriverRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForGroupTransferRepository;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForTaxiRepository;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.database.model.driversData.Driver;
import ru.sberbank.ditsib.transport.request.dto.mapper.DriverDTOMapper;
import ru.sberbank.ditsib.transport.request.service.RequestService;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class ContractorTripUpdateListenerImpl implements Consumer<Message<ContractorUpdateTripMessage>> {
    
    private final RequestForTaxiRepository requestForTaxiRepository;
    private final RequestForGroupTransferRepository requestForGroupTransferRepository;
    private final DriverDTOMapper driverMapper;
    private final DriverRepository driverRepository;
    private final RequestService requestService;
    
    public void accept(Message<ContractorUpdateTripMessage> message) {
        handle(message.getPayload());
    }
    
    private void handle(ContractorUpdateTripMessage message) {
        log.debug("handle() - start. message.id = {}", message.getId());
        var received = message.requests();
        var requests = requestForTaxiRepository.findAllById(received.stream()
                .map(ContractorUpdateTripMessage.Request::id)
                .toList());
        log.debug("handle() - step 1. Количество заявок на такси - requests.size() = {}", requests.size());
        
        //Если не удалось найти среди заявок по такси, ищем среди заявок на групповой трансфер
        var found = requests.stream().map(Request::getId).collect(Collectors.toSet());
        var notFound = received.stream()
                .map(ContractorUpdateTripMessage.Request::id)
                .filter(id -> !found.contains(id))
                .toList();
        var requestsForGroupTransfer = requestForGroupTransferRepository.findAllById(notFound);
        
        log.debug("handle() - step 1. Количество id заявок внутри сообщения - requests.size() = {}", requests.size());
        log.debug("handle() - step 1.1 Количество заявок на групповой трансфер - requestsForGroupTransfer.size() = {}",
                  requestsForGroupTransfer.size());
        var receivedDriver = message.driver();
        
        Driver savedDriver = null;
        if (receivedDriver != null) {
            var driver = driverRepository.findById(receivedDriver.id()).orElseGet(Driver::new);
            driverMapper.update(driver, receivedDriver);
            savedDriver = driverRepository.save(driver);
            log.debug("handle() - step 3. Сохранили водителя - savedDriver.getId() = {}", savedDriver.getId());
        }
        
        for (var request : requests) {
            log.debug("handle() - step 4. Обновление данных заявки - request.getId() = {}", request.getId());
            
            if (request.getStatus().isTerminal()) {
                log.debug("Заявка [{}] уже находится в финальном статусе", request.getId());
                continue;
            }
            
            var newStatus = TripRequestStatus.fromContractorStatus(InboundTaxiTripStatus.valueOf(message.status()));
            request.setDriver(savedDriver);
            Optional.ofNullable(message.driverWaitingTime()).ifPresent(request::setFactWaitingTime);
            Optional.ofNullable(message.factDistance()).ifPresent(request::setFactDistance);
            requestService.changeState(request, newStatus);
        }
        
        for (var request : requestsForGroupTransfer) {
            log.debug("handle() - step 4. Обновление данных заявки groupTransfer - request.getId() = {}", request.getId());
            
            if (request.getStatus().isTerminal()) {
                log.debug("Заявка groupTransfer [{}] уже находится в финальном статусе", request.getId());
                continue;
            }
            
            var newStatus = fromContractorStatusToGroupTransferStatus(InboundTaxiTripStatus.valueOf(message.status()));
            request.setDriver(savedDriver);
            Optional.ofNullable(message.driverWaitingTime()).ifPresent(request::setFactWaitingTime);
            Optional.ofNullable(message.factDistance()).ifPresent(request::setFactDistance);
            requestService.changeState(request, newStatus);
        }
        log.debug("handle() - end. message.id = {}", message.getId());
    }
    
    private static TripRequestStatus fromContractorStatusToGroupTransferStatus(InboundTaxiTripStatus inboundTaxiTripStatus) {
        return switch (inboundTaxiTripStatus) {
            case SENT_TO_CONTRACTOR, WAITING_FOR_ASSIGNMENT -> TripRequestStatus.GROUP_TRANSFER_AWAITING_SEARCH;
            case DRIVER_ASSIGNED, DRIVER_APPROVED -> TripRequestStatus.GROUP_TRANSFER_DRIVER_FOUND;
            case DRIVER_ON_THE_WAY -> TripRequestStatus.GROUP_TRANSFER_DRIVER_ON_THE_WAY;
            case DRIVER_ARRIVED -> TripRequestStatus.GROUP_TRANSFER_DRIVER_ARRIVED;
            case TRIP_IN_PROGRESS -> TripRequestStatus.GROUP_TRANSFER_TRIP_IN_PROGRESS;
            case ORDER_FINISHED -> TripRequestStatus.GROUP_TRANSFER_TRIP_FINISHED;
            default -> TripRequestStatus.GROUP_TRANSFER_CANCELLED;
        };
    }
}
