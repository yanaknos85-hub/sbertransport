package ru.sberbank.transport.oto.cargo.service;

import ru.sberbank.transport.oto.cargo.database.model.Address;
import ru.sberbank.transport.oto.cargo.database.model.Waypoint;

import java.util.List;

public interface WaypointService {

    List<Waypoint> findWaypointsByAddress(Address address);
    
    Waypoint save(Waypoint waypoint);
}
