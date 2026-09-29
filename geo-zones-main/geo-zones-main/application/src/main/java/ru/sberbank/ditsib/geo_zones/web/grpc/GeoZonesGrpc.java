package ru.sberbank.ditsib.geo_zones.web.grpc;

import com.google.protobuf.NullValue;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.sber.transport.geo_zones.grpc.dto.GeoZonesDescriptor;
import ru.sber.transport.geo_zones.grpc.service.GeoZonesServiceGrpc;
import ru.sberbank.ditsib.geo_zones.use_cases.GeoZoneCases;
import ru.sberbank.ditsib.geo_zones.use_cases.model.GeoZone;
import ru.sberbank.ditsib.geo_zones.web.http.dto.WaypointDto;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@GrpcService
class GeoZonesGrpc extends GeoZonesServiceGrpc.GeoZonesServiceImplBase {

    private final GeoZoneCases cases;

    @Override
    public void region(GeoZonesDescriptor.Waypoint request, StreamObserver<GeoZonesDescriptor.Region> responseObserver) {
        try {
            log.debug("Request for region with data: {}", request);
            var response = cases.search(createWaypointDto(request));
            log.info("Region found");
            responseObserver.onNext(createRegion(response));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void regionBranch(GeoZonesDescriptor.Waypoint request, StreamObserver<GeoZonesDescriptor.Region> responseObserver) {
        try {
            log.debug("Request for region branch with data: {}", request);
            var response = cases.searchBranch(createWaypointDto(request));
            log.info("Regions found: {}", response.size());
            for (var item : response) {
                responseObserver.onNext(createRegion(item));
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    private GeoZonesDescriptor.Region createRegion(GeoZone response) {
        return GeoZonesDescriptor.Region.newBuilder()
                .setCode(response.getCode())
                .setId(response.getId().toString())
                .setName(response.getName())
                .setParentId(createNullable(Optional.ofNullable(response.getParentId()).map(UUID::toString).orElse(null)))
                .setTimeZone(response.getTimeZone())
                .build();
    }

    private WaypointDto createWaypointDto(GeoZonesDescriptor.Waypoint request) {
        return WaypointDto.builder()
                .city(request.getCity())
                .country(request.getCountry())
                .house(request.getHouse())
                .region(request.getRegion())
                .street(request.getStreet())
                .district(request.getDistrict().getData())
                .build();
    }

    private GeoZonesDescriptor.NullableString createNullable(String source) {
        return Optional.ofNullable(source)
                .map(s -> GeoZonesDescriptor.NullableString.newBuilder().setData(s).build())
                .orElse(GeoZonesDescriptor.NullableString.newBuilder().setNull(NullValue.NULL_VALUE).build())
                ;
    }
}
