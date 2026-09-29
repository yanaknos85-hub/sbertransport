package ru.sberbank.ditsib.transport.request.util;

import lombok.experimental.UtilityClass;
import ru.sberbank.ditsib.transport.request.database.model.Address;
import ru.sberbank.ditsib.transport.request.database.model.Waypoint;
import ru.sberbank.ditsib.transport.request.dto.WaypointDTO;

@UtilityClass
public class RequestHelper {
    
    public static WaypointDTO waypointToWaypointDTO(Waypoint waypoint) {
        return addressToWaypointDTO(waypoint.getAddress());
    }
    
    public static WaypointDTO addressToWaypointDTO(Address address) {
        return WaypointDTO.builder()
                                             .country(address.getCountry())
                                             .region(address.getRegion())
                                             .city(address.getCity())
                                             .street(address.getStreet())
                                             .house(address.getHouse())
                                             .structure(address.getStructure())
                                             .building(address.getBuilding())
                                             .build();
    }
}
