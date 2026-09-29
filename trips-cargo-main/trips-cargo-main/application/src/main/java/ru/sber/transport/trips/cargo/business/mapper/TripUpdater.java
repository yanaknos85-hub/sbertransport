package ru.sber.transport.trips.cargo.business.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Lookup;
import ru.sber.transport.trips.cargo.business.dto.IntegrationRequestDTO;
import ru.sber.transport.trips.cargo.business.model.*;
import ru.sberbank.ditsib.transport.request.messaging.RouteMessage;

import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

@Mapper
public interface TripUpdater {

    @Mapping(target = "startTime", source = "requests", qualifiedByName = "startTime")
    @Mapping(target = "dispatcherStartTime", source = "requests", qualifiedByName = "startTime")
    @Mapping(target = "endTime", source = "requests", qualifiedByName = "endTime")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "requests", source = "requests", qualifiedByName = "mapCargoRequests")
    @Mapping(target = "capacity", source = "auto.capacity")
    @Mapping(target = "expectedCost", source = "cost")
    @Mapping(target = "expectedDistance", source = "distance")
    @Mapping(target = "routeHumanReadableId", source = "humanReadableId")
    @Mapping(target = "humanReadableId", ignore = true)
    @Mapping(target = "timeZone", ignore = true)
    @Mapping(target = "loaders", source = "route", qualifiedByName = "calcLoadersByRoute")
    void update(@MappingTarget Trip trip, RouteMessage route);

    @Mapping(target = "startTime", source = "route.desiredDate")
    @Mapping(target = "endTime", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "waypoints", expression = "java(mapWaypoints(route.getWaypoints()))")
    @Mapping(target = "contractorId", source = "contractorId")
    @Mapping(target = "routeHumanReadableId", source = "route.humanReadableId")
    @Mapping(target = "expectedCost", source = "route.cost")
    @Mapping(target = "expectedDistance", source = "route.distance")
    @Mapping(target = "timeZone", expression = "java(getTimeZone(route.getDesiredDate()))")
    @Mapping(target = "authorName", source = "route.author.name")
    @Mapping(target = "authorPhone", source = "route.author.phone")
    void fillTrip(@MappingTarget Trip trip, IntegrationRequestDTO route, UUID contractorId);

    @Named("mapCargoRequests")
    default Set<CargoRequest> mapCargoRequests(List<RouteMessage.Request> source) {
        return source.stream().map(this::mapCargoRequest).collect(Collectors.toSet());
    }

    @Mapping(target = "author.mobilePhone", source = "author.phone")
    @Mapping(target = "sender.mobilePhone", source = "sender.phone")
    @Mapping(target = "recipient.mobilePhone", source = "recipient.phone")
    @Mapping(target = "approvedBy.mobilePhone", source = "approvedBy.phone")
    CargoRequest mapCargoRequest(RouteMessage.Request source);

    @Mapping(target = "contact.fullName", source = "contact.contact.name")
    Waypoint.Contact mapWaypoints(IntegrationRequestDTO.Contact contact);

    default Map<String, Object> mapExpected(RouteMessage.Expected expected) {
        return objectMapper().convertValue(expected, new TypeReference<>() {
        });
    }

    default List<Map<String, Object>> mapCargoData(List<RouteMessage.CargoData> cargoData) {
        return objectMapper().convertValue(cargoData, new TypeReference<>() {
        });
    }

    default OffsetDateTime toOffset(LocalDateTime localDateTime) {
        if (localDateTime != null) {
            return OffsetDateTime.of(localDateTime, ZoneOffset.UTC);
        } else return null;
    }

    @Named("startTime")
    default OffsetDateTime getStartTime(List<RouteMessage.Request> source) {
        return source.stream().min(Comparator.comparing(RouteMessage.Request::getDesiredDate))
                .map(r -> ZonedDateTime.of(r.getDesiredDate(), ZoneId.of(r.getTimeZone())).toOffsetDateTime())
                .orElse(null);
    }

    @Named("endTime")
    default OffsetDateTime getEndTimeTime(List<RouteMessage.Request> source) {
        return source.stream().max(Comparator.comparing(RouteMessage.Request::getDesiredDate))
                .map(r -> ZonedDateTime.of(r.getDesiredDate(), ZoneId.of(r.getTimeZone())).toOffsetDateTime())
                .orElse(null);
    }

    @Lookup
    default ObjectMapper objectMapper() {
        return null;
    }

    default List<Waypoint> mapWaypoints(List<IntegrationRequestDTO.RouteWaypointDto> waypointDtoList) {
        var waypoints = new ArrayList<Waypoint>();
        waypointDtoList.forEach(waypointDto -> {
            var waypoint = new Waypoint(
                    waypointDto.getId(),
                    waypointDto.getAddress().getCoordinates().getLatitude(),
                    waypointDto.getAddress().getCoordinates().getLongitude(),
                    waypointDto.getOrderingIndex(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    waypointDto.getAddress().getAddressStringRepresentation(),
                    waypointDto.getContacts().stream().map(this::mapWaypoints).collect(Collectors.toList())
            );
            waypoints.add(waypoint);
        });
        return waypoints;
    }

    default String getTimeZone(OffsetDateTime offsetDateTime) {
        if (offsetDateTime != null) {
            return offsetDateTime.getOffset().toString();
        } else return null;
    }

    @Named("calcLoadersByRoute")
    default int calcLoadersByRoute(RouteMessage routeMessage) {
        return routeMessage.requests().stream()
                .flatMapToInt(r -> IntStream.of(r.getSourceLoaders(), r.getDestinationLoaders()))
                .max()
                .orElse(0);
    }
}
