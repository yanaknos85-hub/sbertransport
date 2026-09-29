package ru.sberbank.ditsib.geo.grpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.NullValue;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.util.StringUtils;
import ru.sber.transport.geo.grpc.dto.GeoDescriptor;
import ru.sber.transport.geo.grpc.service.GeoServiceGrpc;
import ru.sberbank.ditsib.geo.config.properties.routing.DistanceUnit;
import ru.sberbank.ditsib.geo.config.properties.routing.RouteType;
import ru.sberbank.ditsib.geo.dto.*;
import ru.sberbank.ditsib.geo.mappers.AddressMapper;
import ru.sberbank.ditsib.geo.model.Address;
import ru.sberbank.ditsib.geo.model.RouteRecreationCoordinates;
import ru.sberbank.ditsib.geo.service.AddressService;
import ru.sberbank.ditsib.geo.service.RouteService;

import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@GrpcService
class GeoAddressActionReplierImpl extends GeoServiceGrpc.GeoServiceImplBase {

    private final AddressService addressService;

    private final RouteService routeService;

    private final ObjectMapper objectMapper;

    private final AddressMapper mapper;

    @Override
    public void getAddress(GeoDescriptor.AddressRequestString request, StreamObserver<GeoDescriptor.AddressResponse> responseObserver) {
        try {
            var addresses = addressService.getAddressByLocation(
                    AddressRequestDto.builder().location(request.getSearch()).build());
            for (var address : addresses) {
                responseObserver.onNext(toGrpc(address));
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Getting addresses failed", e);
            responseObserver.onError(e);
        }
    }

    public void getRoute(GeoDescriptor.RouteRequest request, StreamObserver<GeoDescriptor.RouteResponse> responseObserver) {
        try {
            var routes = routeService.routeRequest(request.getCoordinatesList()
                    .stream()
                    .map(this::toWaypointDto)
                    .collect(Collectors.toList()),
                    DistanceUnit.valueOf(request.getDistanceUnit()),
                    RouteType.CAR, null);

            for (var route : routes) {
                responseObserver.onNext(GeoDescriptor.RouteResponse.newBuilder()
                        .setDistance(route.getDistance())
                        .setTime(route.getTime().toMillis())
                        .addAllSegments(route.getSegments().stream().map(mapper::toSegment).toList())
                        .build());
            }

            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Getting route failed", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getAddressCoords(GeoDescriptor.AddressRequestCoordinates request, StreamObserver<GeoDescriptor.AddressResponse> responseObserver) {
        try {
            var addresses = addressService.getAddressByCoordinates(AddressRequestDto.builder()
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .sort(SortEnum.DISTANCE)
                    .requestType(RequestType.BUILDING)
                    .radius(10)
                    .build());

            for (var address : addresses) {
                responseObserver.onNext(toGrpc(address));
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Getting addresses failed", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getRouteByCoords(GeoDescriptor.RouteRecreationRequest request, StreamObserver<GeoDescriptor.RouteResponse> responseObserver) {
        try {
            var coords = request.getCoordinatesList()
                    .stream()
                    .map(this::toCoordinates)
                    .toList();

            var route = routeService.routeRequest(coords);
            responseObserver.onNext(GeoDescriptor.RouteResponse.newBuilder()
                    .setDistance(route.getDistance())
                    .setTime(route.getTime().toMillis())
                    .addAllSegments(route.getSegments().stream().map(mapper::toSegment).toList())
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Getting route by coords failed", e);
            responseObserver.onError(e);
        }
    }

    private GeoDescriptor.AddressResponse toGrpc(Address address) {
        return GeoDescriptor.AddressResponse.newBuilder()
                .setRegion(address.getRegion())
                .setCity(Optional.ofNullable(address.getCity()).orElse(""))
                .setStreet(createNullable(address.getStreet()))
                .setDistrict(createNullable(address.getDistrict()))
                .setCountry(address.getCountry())
                .setHouse(createNullable(address.getHouse()))
                .setLatitude(address.getLatitude())
                .setLongitude(address.getLongitude())

                .setSettlement(createNullable(address.getSettlement()))
                .setLivingArea(createNullable(address.getLivingArea()))
                .setPlace(createNullable(address.getPlace()))
                .setAttributeGroups(createNullableObj(address.getAttributeGroups()))
                .setNameEx(createNullableObj(address.getNameEx()))
                .setIsPaid(createNullable(address.getIsPaid()))
                .setPoint(createNullableObj(address.getPoint()))
                .setFullName(createNullable(address.getFullName()))
                .setName(createNullable(address.getName()))
                .setType(createNullable(address.getType()))
                .setGeometry(createNullableObj(address.getGeometry()))
                .setObjectId(createNullableObj(address.getObjectId()))
                .setPurposeName(createNullable(address.getPurposeName()))
                .build();
    }

    private WaypointDto toWaypointDto(GeoDescriptor.RouteAddress address) {
        return new WaypointDto(AddressDto.builder()
                .region(address.getRegion())
                .country(address.getCountry())
                .city(address.getCity())
                .district(getNullable(address.getDistrict()))
                .street(getNullable(address.getStreet()))
                .house(getNullable(address.getHouse()))
                .longitude(address.getLongitude())
                .latitude(address.getLatitude())
                .build(), null);
    }

    private RouteRecreationCoordinates toCoordinates(GeoDescriptor.RouteRecreationPoint coords) {
        return RouteRecreationCoordinates.builder()
                .time(coords.getTime())
                .latitude(coords.getLatitude())
                .longitude(coords.getLongitude())
                .build();
    }

    private String getNullable(GeoDescriptor.NullableString nullable) {
        if (nullable.getNull() == NullValue.NULL_VALUE && !StringUtils.hasText(nullable.getData())) {
            return null;
        }

        return nullable.getData();
    }

    private GeoDescriptor.NullableString createNullable(String source) {
        return Optional.ofNullable(source)
                .map(s -> GeoDescriptor.NullableString.newBuilder().setData(s).build())
                .orElse(GeoDescriptor.NullableString.newBuilder().setNull(NullValue.NULL_VALUE).build());
    }

    private GeoDescriptor.NullableBoolean createNullable(Boolean source) {
        return Optional.ofNullable(source)
                .map(s -> GeoDescriptor.NullableBoolean.newBuilder().setData(s).build())
                .orElse(GeoDescriptor.NullableBoolean.newBuilder().setNull(NullValue.NULL_VALUE).build());
    }

    private GeoDescriptor.NullableString createNullableObj(Object source) {
        return Optional.ofNullable(source)
                .map(s -> {
                    try {
                        return GeoDescriptor.NullableString.newBuilder().setData(objectMapper.writeValueAsString(s)).build();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElse(GeoDescriptor.NullableString.newBuilder().setNull(NullValue.NULL_VALUE).build());
    }
    
    
}
