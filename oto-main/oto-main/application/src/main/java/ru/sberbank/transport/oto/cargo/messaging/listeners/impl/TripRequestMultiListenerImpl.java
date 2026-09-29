package ru.sberbank.transport.oto.cargo.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.cargo.CargoTypeEnum;
import ru.sberbank.ditsib.transport.request.messaging.CargoRequestMultiMessage;
import ru.sberbank.transport.oto.cargo.database.dao.OrganizationRepository;
import ru.sberbank.transport.oto.cargo.database.dao.WaypointContactRepository;
import ru.sberbank.transport.oto.cargo.database.dao.WaypointRepository;
import ru.sberbank.transport.oto.cargo.database.model.Request;
import ru.sberbank.transport.oto.cargo.database.model.Waypoint;
import ru.sberbank.transport.oto.cargo.database.model.WaypointContact;
import ru.sberbank.transport.oto.cargo.database.model.tariff.BaseTariff;
import ru.sberbank.transport.oto.cargo.dto.mapper.EntityDTOMapper;
import ru.sberbank.transport.oto.cargo.mappers.RequestMapper;
import ru.sberbank.transport.oto.cargo.messaging.listeners.TripRequestMultiListener;
import ru.sberbank.transport.oto.cargo.service.*;

import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

/**
 * Реализация обработчика.
 */
@RequiredArgsConstructor
@Transactional
@Component
@Slf4j
class TripRequestMultiListenerImpl implements TripRequestMultiListener {
    private final RequestService requestService;
    private final AddressService addressService;
    private final EntityDTOMapper mapper;
    private final EmployeeService employeeService;
    private final WaypointRepository waypointRepository;
    private final WaypointContactRepository waypointContactRepository;
    private final ContractorService contractorService;
    private final OrganizationRepository organizationRepository;
    
    private final Map<TransportTypeEnum, TariffService<BaseTariff>> publicTariffServiceMap;
    
    private final RequestMapper requestMapper;
    
    @Override
    public void handleCargo(UUID requestId, CargoRequestMultiMessage message) {
        log.info("Handle message: {}", message);

        if (message.isDeleted()) {
            return;
        }
        
        var request = requestService.findById(requestId)
                                    .orElseGet(() -> requestService.save(Request.builder().id(message.getId()).build()));
        
        requestMapper.update(request, message);
        setWaypoints(request, message);
        if (message.getControlDate() != null) {
            request.setControlDate(message.getControlDate());
        }
        
        if (message.getAuthor() != null && message.getInitiatorId() != null) {
            request.setAuthor(employeeService.findOrCreateEmployeeById(message.getInitiatorId()));
        }
        
        if (message.getAuthor() != null) {
            request.setAuthorName(message.getAuthor().getFullName());
            request.setAuthorPhone(message.getAuthor().getMobilePhone());
        }
        
        if (message.getApprovalId() != null) {
            request.setApprovedBy(employeeService.findOrCreateEmployeeById(message.getApprovalId()));
        }
        if (message.getContractorId() != null) {
            request.setContractor(contractorService.findOrCreateContractorById(message.getContractorId()));
        }
        if (message.getTariffId() != null && request.getTransportType() != null) {
            TransportTypeEnum.getByName(request.getTransportType())
                    .map(publicTariffServiceMap::get)
                    .flatMap(service -> service.findById(message.getTariffId()))
                    .ifPresent(request::setTariff);
        }
        
        
        if (message.getWeight() != null) {
            request.setWeight(message.getWeight());
        }
        
        if (message.getVolume() != null) {
            request.setVolume(message.getVolume());
        }
        
        request.setTripId(message.getCargoTripId());
        request.setCargoTripHumanReadableId(message.getCargoTripHumanReadableId());
        
        if (message.getSource() != null) {
            request.setSource(message.getSource());
        }
        
        if (message.getCargoDetails() != null) {
            request.setCargoTypes(message.getCargoDetails().stream()
                                         .map(CargoRequestMultiMessage.CargoDetail::getCargoType)
                                         .map(CargoTypeEnum::valueOf)
                                         .map(CargoTypeEnum::getName)
                                         .distinct()
                                         .collect(Collectors.joining(", ")));
        }
        
        if (message.getOrganizationId() != null) {
            organizationRepository.findById(message.getOrganizationId())
                                  .ifPresent(request::setOrganization);
        }
        setSenderAndRecipientInfo(request);
        requestService.save(request);
    }
    
    private void setWaypoints(Request request, CargoRequestMultiMessage message) {
        
        List<UUID> wayPointsIds = request.getWaypoints().stream()
                                         .map(Waypoint::getId)
                                         .toList();
        if (isNotEmpty(wayPointsIds)) {
            waypointContactRepository.deleteByWaypointIds(request.getWaypoints().stream()
                                                                 .map(Waypoint::getId)
                                                                 .toList());
        }
        
        request.getWaypoints().clear();
        var waypoints = message.getWaypoints();
        for (var messageWaypoint : waypoints) {
            var address = addressService.saveIfNotExists(
                    mapper.addressMessageToAddress(messageWaypoint.getAddress()));
            var waypoint = waypointRepository.findById(messageWaypoint.getId()).orElseGet(Waypoint::new);
            waypoint.setId(messageWaypoint.getId());
            waypoint.setAddress(address);
            waypoint.setWaitTime(messageWaypoint.getWaitTime());
            
            if (Objects.nonNull(messageWaypoint.getContacts())) {
                messageWaypoint.getContacts().stream()
                               .filter(Objects::nonNull)
                               .map(c -> this.getContacts(c, waypoint))
                               .forEach(waypoint.getContacts()::add);
            }
            
            if (messageWaypoint.getCheckinAutomatic() != null) {
                waypoint.setCheckinAutomatic(messageWaypoint.getCheckinAutomatic());
            }
            if (messageWaypoint.getCheckinManual() != null) {
                waypoint.setCheckinManual(messageWaypoint.getCheckinManual());
            }
            waypoint.setRequest(request);
            waypoint.setOrderingIndex(messageWaypoint.getOrderingIndex());
            waypoint.setOrganization(messageWaypoint.getOrganization());
            
            request.getWaypoints().add(waypoint);
        }
        
    }
    
    private WaypointContact getContacts(CargoRequestMultiMessage.Contact messageContact, Waypoint waypoint) {
        return WaypointContact.builder()
                              .waypoint(waypoint)
                              .mobilePhone(messageContact.getMobilePhone())
                              .fullname(messageContact.getFullname())
                              .employee(Optional.ofNullable(messageContact.getEmployeeId())
                                                .map(employeeService::findOrCreateEmployeeById)
                                                .orElse(null))
                              .build();
    }
    
    /**
     * Заполнение полей информации об отправителе и получателе из первой и последней путевой точки
     *
     * @param request заявка из грузовой таблички заявок
     */
    private void setSenderAndRecipientInfo(Request request) {
        
        var waypoints = request.getWaypoints();
      
        waypoints.stream().min(Comparator.comparingInt(Waypoint::getOrderingIndex))
                 .ifPresent(w -> {
                     Optional.ofNullable(w.getContacts()).orElse(Collections.emptyList()).stream().findFirst().ifPresent(contact -> {
                         request.setSender(contact.getEmployee());
                         request.setSenderName(contact.getFullname());
                         request.setSenderPhone(contact.getMobilePhone());
                         request.setSenderOrganization(w.getOrganization());
                     });
                     
                     request.setStartWaypoint(w);
                 });
    
        waypoints.stream().max(Comparator.comparingInt(Waypoint::getOrderingIndex))
                 .ifPresent(w -> {
                     Optional.ofNullable(w.getContacts()).orElse(Collections.emptyList()).stream().findFirst().ifPresent(contact -> {
                         request.setRecipient(contact.getEmployee());
                         request.setRecipientName(contact.getFullname());
                         request.setRecipientPhone(contact.getMobilePhone());
                         request.setRecipientOrganization(w.getOrganization());
                     });
        
                     request.setEndWaypoint(w);
                 });
    }
}
