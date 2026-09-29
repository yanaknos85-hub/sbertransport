package ru.sber.transport.trips.cargo.web.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sber.transport.trips.cargo.business.dto.VehicleBusynessDTO;
import ru.sber.transport.trips.cargo.business.dto.VehicleBusynessRequest;
import ru.sber.transport.trips.cargo.business.providers.TripProvider;
import ru.sber.transport.trips.cargo.messaging.providers.ContractorProvider;
import ru.sber.transport.trips.cargo.web.service.AuthCheckService;
import ru.sber.transport.trips.cargo.web.service.VehicleService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final AuthCheckService authCheckService;

    private final ContractorProvider contractorProvider;

    private final TripProvider tripProvider;

    @Override
    public List<VehicleBusynessDTO> getVehicleBusyness(Authentication authentication, VehicleBusynessRequest vehicleBusynessRequest) {
        var contractorId = authCheckService.userAuthCheck(vehicleBusynessRequest.contractorId(), (JwtAuthenticationToken) authentication);
        var contractorDigitId = contractorProvider.getContractorDigitId(contractorId);
        var busynessData = new ArrayList<VehicleBusynessDTO>();
        if (vehicleBusynessRequest.vehicleIds() != null && !vehicleBusynessRequest.vehicleIds().isEmpty()) {
            var rawData = tripProvider.findAllVehicleBusyness(vehicleBusynessRequest, contractorId, contractorDigitId);
            rawData.parallelStream()
                    .collect(Collectors.toMap(
                            VehicleBusynessDTO::getVehicleId,
                            VehicleBusynessDTO::getTrips,
                            (o1, o2) -> {
                                o1.addAll(o2);
                                return o1;
                            }
                    )).forEach((vehicleId, tripData) -> busynessData.add(new VehicleBusynessDTO(vehicleId, tripData)));
        }
        return busynessData;
    }
}
