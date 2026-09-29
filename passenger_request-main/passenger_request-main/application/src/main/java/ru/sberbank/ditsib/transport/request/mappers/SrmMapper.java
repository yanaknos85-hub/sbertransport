package ru.sberbank.ditsib.transport.request.mappers;

import com.google.protobuf.NullValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.srm.grpc.dto.SrmDescriptor;
import ru.sber.transport.srm.model.SrmRequestKpiDTO;
import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sber.transport.srm.model.SrmWaypointFinalDTO;
import ru.sber.transport.srm.model.SrmWaypointGetDTO;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

@Mapper
public interface SrmMapper {
    
    @Mapping(source = "waypointsList", target = "waypoints")
    @Mapping(source = "waypointsFinalList", target = "waypointsFinal")
    @Mapping(source = "requestKpisList", target = "requestKpiList")
    SrmSharedRideDTO mapToDTO(SrmDescriptor.SrmGetResponse response);
    
    SrmWaypointGetDTO mapToDTO(SrmDescriptor.SrmWaypoint waypoint);
    
    @Mapping(source = "requestDataListList", target = "requestDataList")
    SrmWaypointFinalDTO mapToDTO(SrmDescriptor.SrmWaypointFinal waypoint);
    
    SrmRequestKpiDTO mapToDTO(SrmDescriptor.SrmRequestKpi kpi);
    
    default UUID mapToUUID(String s) {
        if (s != null && !s.isEmpty()) {
            return UUID.fromString(s);
        }
        return null;
    }
    
    default ZonedDateTime convertGoogleTimestampToZonedDateTime(com.google.protobuf.Timestamp timestamp) {
        var ldt = convertGoogleTimestampToLocalDateTime(timestamp);
        return ldt.atZone(ZoneOffset.UTC);
    }
    
    default LocalDateTime convertGoogleTimestampToLocalDateTime(com.google.protobuf.Timestamp timestamp) {
        return Instant
                .ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos())
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();
    }
    
    default Double getNullableValue(SrmDescriptor.NullableDouble source) {
        if (source.getNull().equals(NullValue.NULL_VALUE)) {
            return null;
        } else {
            return source.getData();
        }
    }
}
