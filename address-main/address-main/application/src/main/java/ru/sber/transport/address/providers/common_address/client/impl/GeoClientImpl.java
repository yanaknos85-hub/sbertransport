package ru.sber.transport.address.providers.common_address.client.impl;

import com.google.protobuf.NullValue;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.sber.transport.address.business.model.GeoAddress;
import ru.sber.transport.address.providers.common_address.client.GeoClient;
import ru.sber.transport.geo.grpc.dto.GeoDescriptor;
import ru.sber.transport.geo.grpc.service.GeoServiceGrpc;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
class GeoClientImpl implements GeoClient {
    @GrpcClient("grpc-geo")
    private GeoServiceGrpc.GeoServiceBlockingStub geoServiceBlockingStub;

    @Override
    public List<GeoAddress> getAddresses(@NonNull String search, BigDecimal viewportTopLeftLatitude, BigDecimal viewportTopLeftLongitude, BigDecimal viewportBottomRightLatitude, BigDecimal viewportBottomRightLongitude) {
        var viewportBuilder = GeoDescriptor.AddressRequestString.Viewport.newBuilder();
        Optional.ofNullable(viewportBottomRightLatitude).ifPresent(bd -> viewportBuilder.setBottomRightLat(bd.doubleValue()));
        Optional.ofNullable(viewportBottomRightLongitude).ifPresent(bd -> viewportBuilder.setBottomRightLng(bd.doubleValue()));
        Optional.ofNullable(viewportTopLeftLatitude).ifPresent(bd -> viewportBuilder.setTopLeftLat(bd.doubleValue()));
        Optional.ofNullable(viewportTopLeftLongitude).ifPresent(bd -> viewportBuilder.setTopLeftLng(bd.doubleValue()));

        var request = GeoDescriptor.AddressRequestString.newBuilder()
                .setSearch(search)
                .setViewport(viewportBuilder.build()).build();

        var addresses = geoServiceBlockingStub.getAddress(request);

        return mapResult(addresses);
    }

    @Override
    public List<GeoAddress> getAddress(@NonNull BigDecimal latitude, @NonNull BigDecimal longitude) {
        var request = GeoDescriptor.AddressRequestCoordinates.newBuilder()
                .setLatitude(latitude.doubleValue())
                .setLongitude(longitude.doubleValue())
                .build();

        var addresses = geoServiceBlockingStub.getAddressCoords(request);
        return mapResult(addresses);
    }

    private List<GeoAddress> mapResult(Iterator<GeoDescriptor.AddressResponse> addresses) {
        var result = new ArrayList<GeoAddress>();

        while (addresses.hasNext()) {
            var address = addresses.next();

            result.add(new GeoAddress(
                Optional.of(address.getId()).filter(s -> !s.isBlank()).map(UUID::fromString).orElse(UUID.randomUUID()),
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

    private String getNullable(GeoDescriptor.NullableString nullable) {
        if (nullable.getNull() == NullValue.NULL_VALUE && !StringUtils.hasText(nullable.getData())) {
            return null;
        }

        return nullable.getData();
    }
}
