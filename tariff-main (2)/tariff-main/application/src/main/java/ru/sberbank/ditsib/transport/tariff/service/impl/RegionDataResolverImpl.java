package ru.sberbank.ditsib.transport.tariff.service.impl;

import com.google.protobuf.NullValue;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.sber.transport.geo_zones.grpc.dto.GeoZonesDescriptor;
import ru.sber.transport.geo_zones.grpc.service.GeoZonesServiceGrpc;
import ru.sber.transport.tariff.model.WaypointDTO;
import ru.sberbank.ditsib.transport.tariff.dto.RegionDto;
import ru.sberbank.ditsib.transport.tariff.service.RegionDataResolver;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
class RegionDataResolverImpl implements RegionDataResolver {
    
    @GrpcClient("grpc-geo-zones")
    private GeoZonesServiceGrpc.GeoZonesServiceBlockingStub stub;
    
    @Override
    public RegionDto getRegion(WaypointDTO waypoint) {
        var response = stub.region(createRequest(waypoint));
        return decodeResponse(response);
    }
    
    @Override
    public List<RegionDto> getRegionBranch(WaypointDTO waypoint) {
        var response = stub.regionBranch(createRequest(waypoint));
        var result = new LinkedList<RegionDto>();
        while (response.hasNext()) {
            result.add(decodeResponse(response.next()));
        }
        return result;
    }
    
    private RegionDto decodeResponse(GeoZonesDescriptor.Region response) {
        return RegionDto.builder()
                        .name(response.getName())
                        .id(UUID.fromString(response.getId()))
                        .code(response.getCode())
                        .parentId(Optional.ofNullable(getNullable(response.getParentId()))
                                          .map(UUID::fromString)
                                          .orElse(null))
                        .timeZone(response.getTimeZone())
                        .build();
    }
    
    private GeoZonesDescriptor.Waypoint createRequest(WaypointDTO waypoint) {
        return GeoZonesDescriptor.Waypoint.newBuilder()
                                          .setCountry(waypoint.getCountry())
                                          .setRegion(waypoint.getRegion())
                                          .setCity(waypoint.getCity())
                                          .setStreet(Optional.ofNullable(waypoint.getStreet()).orElse(""))
                                          .setHouse(Optional.ofNullable(waypoint.getHouse()).orElse(""))
                                          .setStructure(createNullable(waypoint.getStructure()))
                                          .setBuilding(createNullable(waypoint.getBuilding()))
                                          .setDistrict(createNullable(waypoint.getDistrict()))
                                          .build();
    }
    
    private GeoZonesDescriptor.NullableString createNullable(String source) {
        return Optional.ofNullable(source)
                       .map(s -> GeoZonesDescriptor.NullableString.newBuilder().setData(s).build())
                       .orElse(GeoZonesDescriptor.NullableString.newBuilder().setNull(NullValue.NULL_VALUE).build());
    }
    
    private String getNullable(GeoZonesDescriptor.NullableString nullable) {
        if (nullable.getNull() == NullValue.NULL_VALUE && !StringUtils.hasText(nullable.getData())) {
            return null;
        }
        
        return nullable.getData();
    }
}
