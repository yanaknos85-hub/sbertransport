package ru.sberbank.ditsib.transport.tariff.service.impl;

import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.sber.transport.geo.grpc.dto.GeoDescriptor;
import ru.sber.transport.geo.grpc.service.GeoServiceGrpc;
import ru.sber.transport.tariff.grpc.dto.TariffDescriptor;
import ru.sber.transport.tariff.model.WaypointDTO;
import ru.sberbank.ditsib.transport.tariff.mappers.GeoMapper;
import ru.sberbank.ditsib.transport.tariff.service.GeoService;

import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GeoServiceImpl implements GeoService {
    
    @GrpcClient("grpc-geo")
    private GeoServiceGrpc.GeoServiceBlockingStub stub;
    
    private final GeoMapper mapper;
    
    public TariffDescriptor.CalculateResponse getRoute(List<WaypointDTO> waypoints) {
        var segments = new ArrayList<TariffDescriptor.Segment>();
        var request = GeoDescriptor.RouteRequest.newBuilder()
                                                .addAllCoordinates(mapper.toAddressList(waypoints))
                                                .setDistanceUnit("KILOMETERS")
                                                .build();
        
        var response = stub.getRoute(request);
        var responseList = new ArrayList<GeoDescriptor.RouteResponse>();
        while (response.hasNext()){
            responseList.add(response.next());
        }
        var chosenResponseOpt = responseList.stream().min(Comparator.comparing(GeoDescriptor.RouteResponse::getDistance));
        if(chosenResponseOpt.isPresent()){
            var chosenResponse = chosenResponseOpt.get();
            chosenResponse.getSegmentsList().stream().map(mapper::toSegment).forEach(segments::add);
            return TariffDescriptor.CalculateResponse.newBuilder()
                                                     .setDistance(chosenResponse.getDistance())
                                                     .setTime(chosenResponse.getTime())
                                                     .addAllSegments(segments).build();
        } else throw new EntityNotFoundException("Route not found");
        
    }
    
}
