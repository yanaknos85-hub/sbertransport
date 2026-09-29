package ru.sber.transport.request.external.providers.grpc;

import com.google.protobuf.NullValue;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.business.providers.GeoZonesProvider;
import ru.sber.transport.geo_zones.grpc.dto.GeoZonesDescriptor;
import ru.sber.transport.geo_zones.grpc.service.GeoZonesServiceGrpc;
import ru.sber.transport.request.external.business.exception.TimeZoneRetrievalException;
import ru.sber.transport.request.external.model.geozone.GeoZoneDTO;
import ru.sber.transport.request.external.model.waypoint.WaypointDTO;

@Slf4j
@RequiredArgsConstructor
public class GeoZonesGrpcProviderImpl implements GeoZonesProvider {

    private final GeoZonesServiceGrpc.GeoZonesServiceBlockingStub stub;

    @Override
    public GeoZoneDTO get(WaypointDTO waypoint) {
        try {
            final var response = stub.region(createRequest(waypoint));
            return new GeoZoneDTO(response.getTimeZone());
        } catch (Exception e) {
            log.error("Failed to get timezone from GeoZones service for waypoint: {}", waypoint, e);
            throw new TimeZoneRetrievalException("Failed to retrieve timezone from GeoZones service for waypoint: " + waypoint, e);
        }
    }

    private GeoZonesDescriptor.Waypoint createRequest(WaypointDTO waypoint) {
        return GeoZonesDescriptor.Waypoint.newBuilder()
                .setCountry(Optional.ofNullable(waypoint.country()).orElse(""))
                .setRegion(Optional.ofNullable(waypoint.region()).orElse(""))
                .setCity(Optional.ofNullable(waypoint.city()).orElse(""))
                .setStreet(Optional.ofNullable(waypoint.street()).orElse(""))
                .setHouse(Optional.ofNullable(waypoint.house()).orElse(""))
                .setBuilding(createNullable(waypoint.building()))
                .setStructure(createNullable(waypoint.structure()))
                .setDistrict(GeoZonesDescriptor.NullableString.newBuilder().setNull(NullValue.NULL_VALUE).build())
                .build();
    }

    private GeoZonesDescriptor.NullableString createNullable(String source) {
        return Optional.ofNullable(source)
                .map(s -> GeoZonesDescriptor.NullableString.newBuilder().setData(s).build())
                .orElse(GeoZonesDescriptor.NullableString.newBuilder().setNull(NullValue.NULL_VALUE).build());
    }
}