package ru.sberbank.ditsib.geo.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.geo.grpc.dto.GeoDescriptor;
import ru.sberbank.ditsib.geo.dto.AddressDto;
import ru.sberbank.ditsib.geo.model.Address;
import ru.sberbank.ditsib.geo.model.Coordinates;
import ru.sberbank.ditsib.geo.model.Segment;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Mapper
public interface AddressMapper {

    @Mapping(target = "latitude", source = "id.latitude")
    @Mapping(target = "longitude", source = "id.longitude")
    @Mapping(target = "city", source = "city", defaultValue = "")
    AddressDto toDto(Address address);

    default Long durationToLong(Duration duration){
        if (duration == null){
            return null;
        }
        return  duration.toMillis();
    }

    default GeoDescriptor.Segment toSegment(Segment segment){
        return GeoDescriptor.Segment.newBuilder()
                .setDistance(segment.getDistance())
                .setTime(durationToLong(segment.getTime()))
                .addAllCoordinates(mapCoordinates(segment.getCoordinates()))
                .build();
    }
    default List<GeoDescriptor.AddressRequestCoordinates> mapCoordinates(List<Coordinates> coordinates){
        var mappedCoordinates = new ArrayList<GeoDescriptor.AddressRequestCoordinates>();
        coordinates.forEach(coordinate -> mappedCoordinates.add(GeoDescriptor.AddressRequestCoordinates.newBuilder()
                .setLatitude(coordinate.getLatitude())
                .setLongitude(coordinate.getLongitude())
                .build()));
        return mappedCoordinates;
    }
}
