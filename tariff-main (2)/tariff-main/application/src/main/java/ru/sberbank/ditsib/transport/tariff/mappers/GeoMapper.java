package ru.sberbank.ditsib.transport.tariff.mappers;

import com.google.protobuf.NullValue;
import org.mapstruct.Mapper;
import ru.sber.transport.geo.grpc.dto.GeoDescriptor;
import ru.sber.transport.tariff.grpc.dto.TariffDescriptor;
import ru.sber.transport.tariff.model.WaypointDTO;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface GeoMapper {
    WaypointDTO toWaypoint(TariffDescriptor.Waypoint waypoint);
    
    List<WaypointDTO> toWaypoints(List<TariffDescriptor.Waypoint> waypoints);
    
    GeoDescriptor.RouteAddress toAddress(WaypointDTO waypointDTO);
    
    List<GeoDescriptor.RouteAddress> toAddressList(List<WaypointDTO> waypointDTOList);
    
    default GeoDescriptor.NullableString map(String str) {
        if (str == null) {
            return GeoDescriptor.NullableString.newBuilder().setNull(NullValue.NULL_VALUE).build();
        }
        return GeoDescriptor.NullableString.newBuilder().setData(str).build();
    }
    
    default String map(TariffDescriptor.NullableString str) {
        return switch (str.getKindCase()) {
            case DATA -> str.getData();
            case NULL, KIND_NOT_SET -> null;
        };
    }
    
    default TariffDescriptor.Segment toSegment(GeoDescriptor.Segment segment) {
        return TariffDescriptor.Segment.newBuilder()
                                       .setDistance(segment.getDistance())
                                       .setTime(segment.getTime())
                                       .addAllCoordinates(mapCoordinates(segment.getCoordinatesList()))
                                       .build();
        
    }
    
    default List<TariffDescriptor.Coordinate> mapCoordinates(List<GeoDescriptor.AddressRequestCoordinates> coordinates) {
        var coordinatesList = new ArrayList<TariffDescriptor.Coordinate>();
        coordinates.forEach(coordinate -> coordinatesList.add(TariffDescriptor.Coordinate.newBuilder()
                                                                                         .setLongitude(coordinate.getLongitude())
                                                                                         .setLatitude(coordinate.getLatitude())
                                                                                         .build()));
        return coordinatesList;
    }
    
}
