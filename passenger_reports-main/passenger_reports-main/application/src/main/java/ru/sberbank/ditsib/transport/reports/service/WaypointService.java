package ru.sberbank.ditsib.transport.reports.service;

import ru.sberbank.ditsib.transport.reports.model.Address;
import ru.sberbank.ditsib.transport.reports.model.Waypoint;

import java.util.List;

public interface WaypointService {

    List<Waypoint> findWaypointsByAddress(Address address);
    
    Waypoint save(Waypoint waypoint);
}
