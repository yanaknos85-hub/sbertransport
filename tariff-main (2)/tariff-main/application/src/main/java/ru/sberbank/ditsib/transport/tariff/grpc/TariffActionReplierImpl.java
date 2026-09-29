package ru.sberbank.ditsib.transport.tariff.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.sber.transport.tariff.grpc.dto.TariffDescriptor;
import ru.sber.transport.tariff.grpc.service.TariffServiceGrpc;
import ru.sber.transport.tariff.model.TripDto;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Employee;
import ru.sberbank.ditsib.transport.tariff.mappers.GeoMapper;
import ru.sberbank.ditsib.transport.tariff.service.CalculateService;
import ru.sberbank.ditsib.transport.tariff.service.EmployeeService;
import ru.sberbank.ditsib.transport.tariff.service.GeoService;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@GrpcService
public class TariffActionReplierImpl extends TariffServiceGrpc.TariffServiceImplBase {
    
    private final GeoMapper geoMapper;
    private final GeoService geoService;
    private final EmployeeService employeeService;
    private final CalculateService calculateService;
    
    @Override
    public void calculate(TariffDescriptor.CalculateRequest request, StreamObserver<TariffDescriptor.CalculateResponse> responseObserver) {
        
        log.debug("Received message from grpc, transport type is - " + request.getTransportType());
        
        var trip = toTripDto(request);
        
        var response = geoService.getRoute(trip.getWaypoints());
        
        trip.setTime(Duration.ofMillis(Long.parseLong(String.valueOf(response.getTime()))));
        trip.setDistance(response.getDistance());
        
        var employeeId = UUID.fromString(request.getEmployeeId());
        var employee = employeeService.get(employeeId).orElseThrow(() -> new EntityNotFoundException(Employee.class, employeeId));
        var transportTypes = List.of(TransportTypeEnum.valueOf(request.getTransportType()));
        log.debug("Using calculate, transport types - " + transportTypes.stream().map(TransportTypeEnum::name).collect(
                Collectors.joining(",")));
        var calculatedDtos = calculateService.calculate(trip,
                                                        employee,
                                                        false,
                                                        transportTypes);
        if (calculatedDtos.size() != 1) {
            responseObserver.onError(new RuntimeException("Calculate is empty or more than one"));
            return;
        }
        
        var calculate = calculatedDtos.stream().findFirst().get();
        var builder = response.toBuilder();
        builder.setTariffId(calculate.getId().toString());
        builder.setCost(calculate.getCost());
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
    
    private TripDto toTripDto(TariffDescriptor.CalculateRequest request) {
        var trip = TripDto.builder();
        var waypoints = geoMapper.toWaypoints(request.getWaypointsList());
        trip.waypoints(waypoints)
            .startPoint(waypoints.get(0))
            .countPoint(waypoints.size())
            .employeeId(UUID.fromString(request.getEmployeeId()))
            .organizationId(UUID.fromString(request.getOrganizationId()))
            .timeZone(request.getTimeZone())
            .tripDate(Instant.ofEpochSecond(request.getDesireDate().getSeconds(), request.getDesireDate().getNanos()).atZone(ZoneId.of("UTC"))
                             .toLocalDateTime());
        return trip.build();
    }
}
