package ru.sberbank.ditsib.transport.request.service.impl;


import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.sber.transport.tariff.grpc.dto.TariffDescriptor;
import ru.sber.transport.tariff.grpc.service.TariffServiceGrpc;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.database.model.Waypoint;
import ru.sberbank.ditsib.transport.request.exceptions.UpdateRequestException;
import ru.sberbank.ditsib.transport.request.mappers.TariffMapper;
import ru.sberbank.ditsib.transport.request.service.TariffGrpcClient;

import java.time.Duration;
import java.time.ZoneOffset;

@Slf4j
@RequiredArgsConstructor
@Component
public class TariffGrpcClientImpl implements TariffGrpcClient {
    
    private final TariffMapper tariffMapper;
    
    @GrpcClient("grpc-tariff")
    private TariffServiceGrpc.TariffServiceBlockingStub stub;
    
    public void recalculate(Request request) {
        var grpcRequest = toRequest(request);
        log.debug("Sending message by grpc, transport type is - " + grpcRequest.getTransportType());
        var response = stub.calculate(grpcRequest);
        if (response.getCost() > request.getExpected().getCost()) {
            throw new UpdateRequestException("Невозможно изменить маршрут, стоимость поездки по новому маршрут превышает изначальную");
        }
        request.getExpected().setCost((double) response.getCost());
        request.getExpected().setDistance(response.getDistance());
        request.getExpected().setTime(Duration.ofMillis(response.getTime()));
        request.getSegmentsJSON().clear();
        request.getSegmentsJSON().addAll(response.getSegmentsList().stream()
                .map(tariffMapper::toRouteSegmant)
                .toList());
    }

    private TariffDescriptor.CalculateRequest toRequest(Request request) {
        var date = request.getDesiredDate();
        return TariffDescriptor.CalculateRequest.newBuilder()
                .addAllWaypoints(
                        request.getWaypoints().stream()
                                .filter(Waypoint::isActive).map(Waypoint::getAddress)
                                .map(tariffMapper::toWaypoint)
                                .toList())
                .setDesireDate(Timestamp.newBuilder().setSeconds(date.toEpochSecond(ZoneOffset.UTC)).build())
                .setEmployeeId(request.getPassenger().getId().toString())
                .setOrganizationId(request.getOrganizationId().toString())
                .setTimeZone(request.getTimeZone())
                .setTransportType(request.getTransportType().getName())
                .build();
        
    }
    
}
