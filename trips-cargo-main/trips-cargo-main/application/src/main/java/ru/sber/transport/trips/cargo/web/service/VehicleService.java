package ru.sber.transport.trips.cargo.web.service;

import org.springframework.security.core.Authentication;
import ru.sber.transport.trips.cargo.business.dto.VehicleBusynessDTO;
import ru.sber.transport.trips.cargo.business.dto.VehicleBusynessRequest;

import java.util.List;

public interface VehicleService {

    List<VehicleBusynessDTO> getVehicleBusyness(Authentication authentication, VehicleBusynessRequest vehicleBusynessRequest);

}
