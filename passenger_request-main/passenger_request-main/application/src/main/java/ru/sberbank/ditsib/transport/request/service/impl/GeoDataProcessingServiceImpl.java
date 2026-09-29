package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.request.database.dao.AddressRepository;
import ru.sberbank.ditsib.transport.request.database.dao.WaypointRepository;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.database.model.Waypoint;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.exceptions.AddressDuplicateOneByOneException;
import ru.sberbank.ditsib.transport.request.messaging.senders.AddressSender;
import ru.sberbank.ditsib.transport.request.service.AddressService;
import ru.sberbank.ditsib.transport.request.service.GeoDataProcessingService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of service for working with addresses and waypoints.
 */
@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
class GeoDataProcessingServiceImpl implements GeoDataProcessingService {
    
    private final AddressSender addressSender;
    
    private final AddressService addressService;
    
    private final WaypointRepository waypointRepository;
    
    private final AddressRepository addressRepository;
    
    
    @Override
    public void saveAddresses(List<Waypoint> newWaypoints, Employee employee) {
        checkAddressOnDuplicateOneByOne(newWaypoints);
        var wayPoints = new ArrayList<>(newWaypoints).iterator();
        newWaypoints.clear();
        while (wayPoints.hasNext()) {
            var wayPoint = wayPoints.next();
            var firstAddress = newWaypoints.isEmpty();
            var address = addressService.getByData(wayPoint.getAddress())
                                                  .orElseGet(() -> addressService.save(wayPoint.getAddress()));
            wayPoint.setAddress(address);
            newWaypoints.add(wayPoint);
            Optional.ofNullable(employee).map(Employee::getId).ifPresent(id -> addressSender.send(wayPoint.getAddress(),
                                                                                                  id,
                                                                                                  firstAddress));
        }
    }
    
    @Override
    public void saveAddresses(Request request, List<Waypoint> newWaypoints) {
        checkAddressOnDuplicateOneByOne(newWaypoints);
        request.getWaypoints().clear();
        waypointRepository.flush();
        //flush необходим, так как без него при одновременной очистке и добавлении возникает ошибка с нарушением unique
        //constraint
        for (Waypoint newWaypoint : newWaypoints) {
            log.info("Waypoint to save was: {}", newWaypoint);
            var firstAddress = request.getWaypoints().isEmpty();
            newWaypoint.setRequest(request);
            newWaypoint.setAddress(addressService.getByData(newWaypoint.getAddress())
                                                 .orElseGet(() -> addressService.save(newWaypoint.getAddress())));
            request.getWaypoints().add(newWaypoint);
            addressSender.send(newWaypoint.getAddress(), request.getPassenger().getId(), firstAddress);
        }
        waypointRepository.flush();
    }
    
    /**
     * Throw exception if equals addresses follow one by one
     *
     * @param waypoints list of waypoints
     */
    protected void checkAddressOnDuplicateOneByOne(List<Waypoint> waypoints) {
        if (waypoints.size() > 1) {
            int i;
            for (i = 1; i < waypoints.size(); i++) {
                if (waypoints.get(i - 1).getAddress().equals(waypoints.get(i).getAddress())) {
                    throw new AddressDuplicateOneByOneException(i);
                }
            }
        }
    }
}