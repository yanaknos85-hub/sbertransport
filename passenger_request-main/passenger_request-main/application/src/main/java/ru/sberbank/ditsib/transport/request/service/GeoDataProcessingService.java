package ru.sberbank.ditsib.transport.request.service;

import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.database.model.Waypoint;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;

import java.util.List;

/**
 * Service for working with addresses and waypoints.
 */
public interface GeoDataProcessingService {
    
    /**
     * Dara to save addresses.
     *
     * @param newWaypoints data of address.
     * @param employee employee to link address.
     */
    void saveAddresses(List<Waypoint> newWaypoints, Employee employee);
    
    /**
     * Save address.
     *
     * @param request linked request
     * @param newWaypoints source addresses.
     */
    void saveAddresses(Request request, List<Waypoint> newWaypoints);
}
