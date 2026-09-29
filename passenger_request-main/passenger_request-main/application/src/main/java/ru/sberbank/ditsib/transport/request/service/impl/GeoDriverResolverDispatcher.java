package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.sber.transport.contractor.grpc.dto.DriverDescriptor;
import ru.sber.transport.contractor.grpc.service.DriverServiceGrpc;
import ru.sberbank.ditsib.transport.constants.TaxiExternalIntegrationType;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.database.model.RequestForTaxi;
import ru.sberbank.ditsib.transport.request.dto.GeoDriverDTO;
import ru.sberbank.ditsib.transport.request.service.GeoDriverResolver;

import java.util.Objects;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class GeoDriverResolverDispatcher implements GeoDriverResolver {
    
    @GrpcClient("contractor")
    private DriverServiceGrpc.DriverServiceBlockingStub stub;
    
    @Override
    public TaxiExternalIntegrationType getIntegrationType() {
        return TaxiExternalIntegrationType.DISPATCHER;
    }
    
    @Override
    public GeoDriverDTO getGeoDriverByRequest(Request request) {
        
        log.debug("GeoDriverResolverDispatcher: getGeoDriverByRequest: request = {}", request);
        
        GeoDriverDTO result = GeoDriverDTO.builder()
                                          .latitude(0.0d)
                                          .latitude(0.0d)
                                          .build();
        try {
            UUID tripId = getTripId(request);
            var driverLocation = stub
                    .getLocation(DriverDescriptor.DriverLocationRequest.newBuilder()
                                                                       .setTripId(tripId.toString())
                                                                       .build());
            result = GeoDriverDTO.builder()
                                 .longitude(driverLocation.getLongitude())
                                 .latitude(driverLocation.getLatitude())
                                 .build();
            log.debug("GeoDriverResolverDispatcher: getGeoDriverByRequest: result = {}", result);
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error("Не удалось получить координаты водителя от диспетчерской для request.humanReadableId = {}",
                      request.getHumanReadableId());
        }
        return result;
    }
    
    private UUID getTripId(Request request) {
        Objects.requireNonNull(request, "request is null");
        Objects.requireNonNull(request.getId(), "requestId is null");
        
        UUID tripId = request.getId();
        
        if (request instanceof RequestForTaxi requestForTaxi && requestForTaxi.isCoopTrip() && requestForTaxi.getRideId() != null) {
            tripId = requestForTaxi.getRideId();
        }
        
        return tripId;
    }
}
