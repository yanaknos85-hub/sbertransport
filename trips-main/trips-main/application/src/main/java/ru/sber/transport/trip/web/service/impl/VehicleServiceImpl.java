package ru.sber.transport.trip.web.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sber.transport.trip.business.dto.DriverBusynessDTO;
import ru.sber.transport.trip.business.dto.DriverBusynessRequest;
import ru.sber.transport.trip.business.dto.VehicleBusynessDTO;
import ru.sber.transport.trip.business.dto.VehicleBusynessRequest;
import ru.sber.transport.trip.business.model.TripStatus;
import ru.sber.transport.trip.business.providers.TripProvider;
import ru.sber.transport.trip.messaging.providers.ContractorProvider;
import ru.sber.transport.trip.providers.checkin.CheckinProvider;
import ru.sber.transport.trip.web.service.AuthCheckService;
import ru.sber.transport.trip.web.service.VehicleService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Component
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final AuthCheckService authCheckService;

    private final ContractorProvider contractorProvider;

    private final TripProvider tripProvider;

    private final CheckinProvider checkinProvider;

    @Override
    public List<VehicleBusynessDTO> getVehicleBusyness(Authentication authentication, VehicleBusynessRequest vehicleBusynessRequest) {
        var contractorId = authCheckService.userAuthCheck(vehicleBusynessRequest.contractorId(), (JwtAuthenticationToken) authentication);
        var contractorDigitId = contractorProvider.getContractorDigitId(contractorId);
        var busynessData = new ArrayList<VehicleBusynessDTO>();
        if (vehicleBusynessRequest.vehicleIds() != null && !vehicleBusynessRequest.vehicleIds().isEmpty()) {
            var rawData = tripProvider.findAllVehicleBusyness(vehicleBusynessRequest, contractorId, contractorDigitId);
            fillDriverProcessingTime(rawData);
            rawData.addAll(tripProvider.findAllPlanningVehicleBusyness(vehicleBusynessRequest, contractorId, contractorDigitId));
            rawData.addAll(tripProvider.findAllOrderedVehicles(vehicleBusynessRequest, contractorId, contractorDigitId));
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

    private void fillDriverProcessingTime(List<VehicleBusynessDTO> data){
        var busynessDataTripMap = data.parallelStream()
                .map(VehicleBusynessDTO::getTrips)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(VehicleBusynessDTO.TripData::getId, Function.identity()));
        var tripIds = new ArrayList<>(busynessDataTripMap.keySet());
        if (isNotEmpty(tripIds)) {
            checkinProvider.findAllByTripIdsAndStatus(tripIds, TripStatus.DRIVER_ON_THE_WAY)
                    .parallelStream()
                    .forEach(c -> busynessDataTripMap.computeIfPresent(c.getTripId(), (k, v) -> {
                        v.setDriverProcessingTime(c.getTime().toOffsetDateTime());
                        return v;
                    }));
        }
    }
}
