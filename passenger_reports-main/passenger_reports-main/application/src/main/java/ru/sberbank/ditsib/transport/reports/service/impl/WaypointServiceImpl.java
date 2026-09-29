package ru.sberbank.ditsib.transport.reports.service.impl;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.reports.dao.AddressRepository;
import ru.sberbank.ditsib.transport.reports.dao.WaypointRepository;
import ru.sberbank.ditsib.transport.reports.model.Address;
import ru.sberbank.ditsib.transport.reports.model.Waypoint;
import ru.sberbank.ditsib.transport.reports.service.WaypointService;

@Service
@AllArgsConstructor
public class WaypointServiceImpl implements WaypointService {
    private final WaypointRepository waypointRepository;
    private final AddressRepository addressRepository;

    @Override
    public List<Waypoint> findWaypointsByAddress(Address address) {
        List<Address> addresses = addressRepository.findAll(Example.of(address));
        return waypointRepository.findAllByAddressIn(addresses);
    }

    @Override
    public Waypoint save(Waypoint waypoint) {
        return waypointRepository.save(waypoint);
    }
}
