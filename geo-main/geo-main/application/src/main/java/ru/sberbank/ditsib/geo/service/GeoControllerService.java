package ru.sberbank.ditsib.geo.service;

import ru.sberbank.ditsib.geo.dto.*;

import java.util.Collection;
import java.util.List;

/**
 * Service for working with geo-provider.
 */
public interface GeoControllerService {
    
    /**
     * Get address.
     *
     * @param requestDto data for getting address.
     *
     * @return collection of addresses.
     */
    Collection<AddressDto> getAddress(AddressRequestDto requestDto);
    
    /**
     * Get route.
     *
     * @param routeRequest request data for getting route.
     *
     * @return route request data.
     */
    List<RouteDto> getRoutes(RouteRequestDto routeRequest);
}
