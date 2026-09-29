package ru.sber.transport.trip.web.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.trip.business.dto.*;
import ru.sber.transport.trip.business.model.Driver;
import ru.sber.transport.trip.business.model.Waypoint;
import ru.sber.transport.trip.messaging.providers.DriverProvider;
import ru.sber.transport.trip.providers.trips.mapping.TripMapper;
import ru.sber.transport.trip.web.controller.DriverController;
import ru.sber.transport.trip.web.service.DriverService;
import ru.sber.transport.trip.web.service.RequestService;
import ru.sber.transport.trip.web.service.ResultProcessor;
import ru.sber.transport.trip.web.service.VerificationService;

import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@Transactional
class DriverControllerImpl implements DriverController {

    private final DriverService driverService;
    private final DriverProvider driverProvider;
    private final TripMapper tripMapper;
    private final RequestService requestService;
    private final VerificationService verificationService;
    private final ResultProcessor resultProcessor;

    @Override
    public Page<DriverShiftDTO> getDriversByCoordinates(UUID contractorId, DriverLocationSearchDTO driverLocationSearchDTO, Authentication authentication) {
        return driverService.getDriversByCoordinates(contractorId, driverLocationSearchDTO, authentication);
    }

    @Override
    public TripV2Dto getCurrentTripDriver(UUID contractorId, UUID driverId, RequestSearchDto searchDto) {
        var driver = driverProvider.get(driverId).orElseThrow(() -> new EntityNotFoundException(Driver.class, driverId));
        if (driver.getActiveTripId() != null) {
            var trip = requestService.find(contractorId, driver.getActiveTripId(), false);
            trip.setWaypoints(trip.getWaypoints().stream().sorted(Comparator.comparing(Waypoint::orderingIndex)).collect(Collectors.toList())); //NOSONAR
            return resultProcessor.process(trip);
        } else {
            return null;
        }
    }

    @Override
    public void updateFinalInfo(UUID contractorId, UUID driverId, UUID tripId, FinalInfoTripDto finalInfoTripDto) {
        requestService.updateFinal(contractorId, driverId, tripId, finalInfoTripDto);
    }

    @Override
    public void setLastPointInfo(GeoWaypointDTO geoWaypointDTO, JwtAuthenticationToken authentication) {
        var driver = getDriver(authentication);
        verificationService.checkIsDriverOnline(driver);
        driverService.checkCurrentTrip(geoWaypointDTO.getCurrentTripId(), driver);
        driverService.lastPointInfo(geoWaypointDTO, driver);
    }

    @Override
    public void setOnline(StateDTO stateDTO, JwtAuthenticationToken authentication) {
        var driver = getDriver(authentication);
        driverService.setOnline(driver, stateDTO.isState());
    }

    @Override
    public CheckinResponseDTO getTripCheckins(JwtAuthenticationToken authentication, UUID tripId) {
        var driver = getDriver(authentication);
        return driverService.getTripCheckins(driver, tripId);
    }

    private Driver getDriver(JwtAuthenticationToken authentication) {
        var userId = UUID.fromString(authentication.getToken().getId());
        return driverProvider.get(userId)
                .orElseGet(() -> driverProvider.getByOauthId(userId)
                        .orElseThrow(() -> new EntityNotFoundException(Driver.class, userId)));
    }

}
