package ru.sberbank.transport.oto.cargo.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.cargo.CargoTypeEnum;
import ru.sberbank.ditsib.transport.request.messaging.CargoRequestMessage;
import ru.sberbank.transport.oto.cargo.database.dao.OrganizationRepository;
import ru.sberbank.transport.oto.cargo.database.dao.WaypointRepository;
import ru.sberbank.transport.oto.cargo.database.model.Request;
import ru.sberbank.transport.oto.cargo.database.model.Waypoint;
import ru.sberbank.transport.oto.cargo.database.model.tariff.BaseTariff;
import ru.sberbank.transport.oto.cargo.dto.mapper.EntityDTOMapper;
import ru.sberbank.transport.oto.cargo.mappers.DriverMapper;
import ru.sberbank.transport.oto.cargo.mappers.RequestMapper;
import ru.sberbank.transport.oto.cargo.mappers.VehicleMapper;
import ru.sberbank.transport.oto.cargo.messaging.listeners.TripRequestListener;
import ru.sberbank.transport.oto.cargo.messaging.messages.RequestMessage;
import ru.sberbank.transport.oto.cargo.service.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Реализация обработчика.
 */
@RequiredArgsConstructor
@Component
@Slf4j
class TripRequestListenerImpl implements TripRequestListener {
    private final RequestService requestService;
    private final AddressService addressService;
    private final EntityDTOMapper mapper;
    private final EmployeeService employeeService;
    private final WaypointRepository waypointRepository;
    private final ContractorService contractorService;
    private final OrganizationRepository organizationRepository;

    private final Map<TransportTypeEnum, TariffService<BaseTariff>> publicTariffServiceMap;

    private final RequestMapper requestMapper;

    private final DriverMapper driverMapper;

    private final VehicleMapper vehicleMapper;

    @Override
    @Transactional
    public void handle(UUID requestId, RequestMessage message) {
        if (message.isDeleted()) return;

        var request = requestService.findById(requestId)
                .orElseGet(() -> requestService.save(Request.builder().id(message.getId()).build()));
        setWaypoints(request, message);
        requestMapper.update(request, message);

        if (message.getPassengerId() != null) {
            request.setPassenger(employeeService.findOrCreateEmployeeById(message.getPassengerId()));
        }
        if (message.getAuthorId() != null) {
            request.setAuthor(employeeService.findOrCreateEmployeeById(message.getAuthorId()));
        }
        if (message.getApprovalId() != null) {
            request.setApprovedBy(employeeService.findOrCreateEmployeeById(message.getApprovalId()));
        }
        if (message.getContractorId() != null) {
            request.setContractor(contractorService.findOrCreateContractorById(message.getContractorId()));
        }

        if (message.getTariffId() != null && request.getTransportType() != null) {
            TransportTypeEnum.getByName(request.getTransportType()).map(publicTariffServiceMap::get)
                    .flatMap(service -> service.findById(message.getTariffId()))
                    .ifPresent(request::setTariff);
        }
        if (message.getDeadlineState() == null) {
            if (!message.isSlaExpired()) {
                request.setDeadlineState(DeadlineState.NONE);
            } else {
                request.setDeadlineState(DeadlineState.RED);
            }
        }
        request.setDriver(driverMapper.driver(message.getDriverData()));
        request.setVehicle(vehicleMapper.vehicle(message.getVehicleData()));

        var information = Optional.ofNullable(message.getInformation());
        if (information.isPresent()) {
            String addContactPhone = String.valueOf(information.get().get("addContactPhone"));
            String addContactFIO = String.valueOf(information.get().get("addContactFIO"));
            if (isNotEmpty(addContactFIO) && isNotEmpty(addContactPhone)) {
                request.setAddContactPhone(addContactPhone);
                request.setAddContactFIO(addContactFIO);
            }
        }
        requestService.save(request);
    }

    @Override
    public void handleCargo(UUID requestId, CargoRequestMessage message) {
        if (message.isDeleted()) return;

        var request = requestService.findById(requestId)
                .orElseGet(() -> requestService.save(Request.builder().id(message.getId()).build()));

        requestMapper.update(request, message);
        setWaypoints(request, message);
        setStartEndWaypoints(request);

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
            TransportTypeEnum.getByName(request.getTransportType()).map(publicTariffServiceMap::get)
                    .flatMap(service -> service.findById(message.getTariffId()))
                    .ifPresent(request::setTariff);
        }

        if (message.getSenderId() != null) {
            request.setSender(employeeService.findOrCreateEmployeeById(message.getSenderId()));
        }
        if (message.getSender() != null && message.getSender().getFullName() != null) {
            request.setSenderName(message.getSender().getFullName());
        }
        if (message.getSenderPhone() != null) {
            request.setSenderPhone(message.getSenderPhone());
        }
        if (message.getSenderOrganization() != null) {
            request.setSenderOrganization(message.getSenderOrganization());
        }

        if (message.getRecipientId() != null) {
            request.setRecipient(employeeService.findOrCreateEmployeeById(message.getRecipientId()));
        }
        if (message.getRecipient() != null && message.getRecipient().getFullName() != null) {
            request.setRecipientName(message.getRecipient().getFullName());
        }
        if (message.getRecipientPhone() != null) {
            request.setRecipientPhone(message.getRecipientPhone());
        }
        if (message.getRecipientOrganization() != null) {
            request.setRecipientOrganization(message.getRecipientOrganization());
        }

        if (message.getWeight() != null) {
            request.setWeight(message.getWeight());
        }

        if (message.getVolume() != null) {
            request.setVolume(message.getVolume());
        }

        if (message.getCargoTripId() != null) {
            request.setTripId(message.getCargoTripId());
        }

        if (message.getCargoTripHumanReadableId() != null) {
            request.setCargoTripHumanReadableId(message.getCargoTripHumanReadableId());
        }

        if (message.getSource() != null) {
            request.setSource(message.getSource());
        }

        if (message.getCargoDetails() != null) {
            request.setCargoTypes(message.getCargoDetails().stream()
                    .map(CargoRequestMessage.CargoDetail::getCargoType)
                    .map(CargoTypeEnum::getByName)
                    .filter(Optional::isPresent)
                    .map(cargoType -> cargoType.get().getName())
                    .distinct()
                    .collect(Collectors.joining(", ")));
        }

        if (message.getOrganizationId() != null) {
            organizationRepository.findById(message.getOrganizationId())
                    .ifPresent(request::setOrganization);
        }

        requestService.save(request);

    }

    private void setWaypoints(Request request, RequestMessage message) {
        request.getWaypoints().clear();
        var waypoints = message.getWaypoints();
        ArrayList<Waypoint> waypointsList = new ArrayList<>();
        for (var messageWaypoint : waypoints) {
            var address = addressService.saveIfNotExists(
                    mapper.addressMessageToAddress(messageWaypoint.address()));
            var waypoint = waypointRepository.findById(messageWaypoint.id()).orElseGet(Waypoint::new);
            waypoint.setId(messageWaypoint.id());
            waypoint.setAddress(address);
            waypoint.setWaitTime(messageWaypoint.waitTime());
            waypoint.setCheckinAutomatic(messageWaypoint.checkinAutomatic());
            waypoint.setCheckinManual(messageWaypoint.checkinManual());
            waypoint.setRequest(request);
            waypoint.setOrderingIndex(messageWaypoint.orderingIndex());
            request.getWaypoints().add(waypoint);
            waypointsList.add(waypoint);
        }
        waypointRepository.saveAll(waypointsList);
    }

    private void setWaypoints(Request request, CargoRequestMessage message) {
        request.getWaypoints().clear();
        waypointRepository.flush();
        var waypoints = message.getWaypoints();
        ArrayList<Waypoint> waypointsList = new ArrayList<>();
        for (var messageWaypoint : waypoints) {
            var address = addressService.saveIfNotExists(
                    mapper.addressMessageToAddress(messageWaypoint.getAddress()));
            var waypoint = waypointRepository.findById(messageWaypoint.getId()).orElseGet(Waypoint::new);
            waypoint.setId(messageWaypoint.getId());
            waypoint.setAddress(address);
            waypoint.setWaitTime(messageWaypoint.getWaitTime());
            waypoint.setCheckinAutomatic(messageWaypoint.getCheckinAutomatic());
            waypoint.setCheckinManual(messageWaypoint.getCheckinManual());
            waypoint.setRequest(request);
            waypoint.setOrderingIndex(messageWaypoint.getOrderingIndex());
            request.getWaypoints().add(waypoint);
            waypointsList.add(waypoint);
        }
        waypointRepository.saveAll(waypointsList);
    }

    private void setStartEndWaypoints(Request request) {

        var waypoints = request.getWaypoints();

        waypoints.stream().min(Comparator.comparingInt(Waypoint::getOrderingIndex))
                .ifPresent(request::setStartWaypoint);

        waypoints.stream().max(Comparator.comparingInt(Waypoint::getOrderingIndex))
                .ifPresent(request::setEndWaypoint);
    }
}
