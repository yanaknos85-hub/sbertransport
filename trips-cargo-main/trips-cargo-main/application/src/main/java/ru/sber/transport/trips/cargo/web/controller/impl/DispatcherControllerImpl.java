package ru.sber.transport.trips.cargo.web.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.trips.cargo.business.dto.DriverBusynessDTO;
import ru.sber.transport.trips.cargo.business.dto.DriverBusynessRequest;
import ru.sber.transport.trips.cargo.business.dto.VehicleBusynessDTO;
import ru.sber.transport.trips.cargo.business.dto.VehicleBusynessRequest;
import ru.sber.transport.trips.cargo.web.controller.DispatcherController;
import ru.sber.transport.trips.cargo.web.service.DriverOnlineSwitcherService;
import ru.sber.transport.trips.cargo.web.service.DriverService;
import ru.sber.transport.trips.cargo.web.service.VehicleService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DispatcherControllerImpl implements DispatcherController {

    private final DriverOnlineSwitcherService driverOnlineSwitcherService;

    private final DriverService driverService;

    private final VehicleService vehicleService;

    @Override
    public void switchOnline(UUID driverId, Authentication authentication) {
        driverOnlineSwitcherService.switchOnline(driverId, authentication);
    }

    @Override
    public DriverBusynessDTO getDriverBusyness(Authentication authentication, DriverBusynessRequest driverBusynessRequest) {
        return driverService.getDriverBusyness(authentication, driverBusynessRequest);
    }

    @Override
    public List<VehicleBusynessDTO> getVehicleBusyness(Authentication authentication, VehicleBusynessRequest vehicleBusynessRequest) {
        return vehicleService.getVehicleBusyness(authentication, vehicleBusynessRequest);
    }

}
