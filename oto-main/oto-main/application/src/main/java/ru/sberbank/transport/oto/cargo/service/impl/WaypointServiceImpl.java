package ru.sberbank.transport.oto.cargo.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import ru.sberbank.transport.oto.cargo.database.dao.AddressRepository;
import ru.sberbank.transport.oto.cargo.database.dao.WaypointRepository;
import ru.sberbank.transport.oto.cargo.database.model.Address;
import ru.sberbank.transport.oto.cargo.database.model.Waypoint;
import ru.sberbank.transport.oto.cargo.service.WaypointService;

import java.util.List;

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
