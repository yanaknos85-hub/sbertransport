package ru.sber.transport.trip.web.service;

import org.springframework.security.core.Authentication;
import ru.sber.transport.trip.business.dto.VehicleBusynessDTO;
import ru.sber.transport.trip.business.dto.VehicleBusynessRequest;

import java.util.List;

public interface VehicleService {

    List<VehicleBusynessDTO> getVehicleBusyness(Authentication authentication, VehicleBusynessRequest vehicleBusynessRequest);

}
