package ru.sberbank.ditsib.service.impl;

import com.google.protobuf.NullValue;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.sber.transport.geo.grpc.dto.GeoDescriptor;
import ru.sber.transport.geo.grpc.service.GeoServiceGrpc;
import ru.sberbank.ditsib.dto.GeoAddress;
import ru.sberbank.ditsib.dto.RouteInfo;
import ru.sberbank.ditsib.exception.RouteNotFoundException;
import ru.sberbank.ditsib.service.GeoService;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GeoServiceImpl implements GeoService {

    private static final String DISTANCE_UNIT_KILOMETERS = "KILOMETERS";

    @GrpcClient("geo")
    private GeoServiceGrpc.GeoServiceBlockingStub geoServiceStub;

    @Override
    public RouteInfo getRouteInfo(List<GeoAddress> addresses) {
        var routeAddresses = addresses.stream().map(this::mapToRouteAddress).toList();

        var routeRequest = GeoDescriptor.RouteRequest.newBuilder()
                .addAllCoordinates(routeAddresses)
                .setDistanceUnit(DISTANCE_UNIT_KILOMETERS)
                .build();

        var routeResponses = geoServiceStub.getRoute(routeRequest);
        return mapToRouteInfo(routeResponses);
    }

    @Override
    public List<GeoAddress> getGeoAddress(@NonNull String address) {
        var request = GeoDescriptor.AddressRequestString.newBuilder()
                .setSearch(address)
                .build();
        var result = geoServiceStub.getAddress(request);
        return mapToGeoAddress(result);
    }

    private GeoDescriptor.RouteAddress mapToRouteAddress(GeoAddress address) {
        return GeoDescriptor.RouteAddress.newBuilder()
                .setCountry(address.country())
                .setRegion(address.region())
                .setCity(address.city())
                .setDistrict(createNullableString(null)) // district отсутствует в GeoAddress
                .setStreet(createNullableString(address.street()))
                .setHouse(createNullableString(address.house()))
                .setBuilding(createNullableString(address.building()))
                .setStructure(createNullableString(address.structure()))
                .setLatitude(address.latitude().doubleValue())
                .setLongitude(address.longitude().doubleValue())
                .build();
    }

    private List<GeoAddress> mapToGeoAddress(Iterator<GeoDescriptor.AddressResponse> addresses) {
        var result = new ArrayList<GeoAddress>();
        while (addresses.hasNext()) {
            var address = addresses.next();
            result.add(new GeoAddress(
                    address.getCountry(),
                    address.getRegion(),
                    address.getCity(),
                    getNullable(address.getStreet()),
                    getNullable(address.getHouse()),
                    getNullable(address.getBuilding()),
                    getNullable(address.getStructure()),
                    BigDecimal.valueOf(address.getLatitude()),
                    BigDecimal.valueOf(address.getLongitude())
            ));
        }
        return result;
    }

    private RouteInfo mapToRouteInfo(Iterator<GeoDescriptor.RouteResponse> routeResponses) {
        if (!routeResponses.hasNext()) {
            throw new RouteNotFoundException();
        }
        var routeResponse = routeResponses.next();
        return new RouteInfo(
                BigDecimal.valueOf(routeResponse.getDistance()),
                routeResponse.getTime(),
                DISTANCE_UNIT_KILOMETERS
        );
    }

    private String getNullable(GeoDescriptor.NullableString nullable) {
        if (nullable.getNull() == NullValue.NULL_VALUE && !StringUtils.hasText(nullable.getData())) {
            return null;
        }
        return nullable.getData();
    }

    private GeoDescriptor.NullableString createNullableString(String value) {
        if (value == null) {
            return GeoDescriptor.NullableString.newBuilder()
                    .setNull(NullValue.NULL_VALUE)
                    .build();
        }
        return GeoDescriptor.NullableString.newBuilder()
                .setData(value)
                .build();
    }
}
