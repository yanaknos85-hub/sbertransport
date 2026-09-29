package ru.sberbank.ditsib.transport.reports.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.messaging.messages.trip.CargoRequestMessage;
import ru.sberbank.ditsib.transport.reports.dao.RequestForCargoRepository;
import ru.sberbank.ditsib.transport.reports.mappers.CargoDetailMapper;
import ru.sberbank.ditsib.transport.reports.messaging.listeners.CargoRequestListener;
import ru.sberbank.ditsib.transport.reports.model.cargo.RequestForCargo;
import ru.sberbank.ditsib.transport.reports.service.EmployeeService;

import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component("cargoRequestInput")
@Slf4j
@Transactional
public class CargoRequestListenerImpl implements CargoRequestListener   {
    
    private final RequestForCargoRepository repository;
    
    private final EmployeeService employeeService;
    
    private final CargoDetailMapper cargoDetailMapper;
    
    @Override
    public void handleCargo(CargoRequestMessage message) {
        var requestId = message.getId();
        if (!message.isDeleted() && requestId!=null) {
            var detail = cargoDetailMapper.toEntity(message.getCargoDetails());
            var request =
                    repository.findById(requestId)
                                  .orElse(repository.save(RequestForCargo.builder().id(message.getId()).build()));
           if (message.getTransportType() != null) {
                request.setTransportType(message.getTransportType());
            }
            detail.forEach(cd -> cd.setRequest(request));
           request.setHumanReadableId(message.getHumanReadableId());
           request.setDesiredDate(message.getDesiredDate());
           request.setOccupiedPlacesCount(message.getOccupiedPlacesCount());
            if (message.getRecipientId() != null) {
                request.setRecipient(employeeService.findOrCreateEmployeeById(message.getRecipientId()));
            }
    
            if (message.getSenderId() != null) {
                request.setSender(employeeService.findOrCreateEmployeeById(message.getSenderId()));
            }
            request.setRecipientOrganization(message.getRecipientOrganization());
            request.setSenderOrganization(message.getSenderOrganization());
            request.setHeight(message.getHeight());
            request.setLength(message.getLength());
            request.setVolume(message.getVolume());
            request.setWeight(message.getWeight());
            request.setWidth(message.getWidth());
            request.setComment(message.getCommentForDriver());
            request.setActive(true);
            request.getCargoDetails().addAll(detail);
            repository.saveAndFlush(request);
        }

    }
}
