package ru.sber.transport.trip.providers.trips.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.SimpleType;
import lombok.SneakyThrows;
import org.jooq.JSON;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Lookup;
import ru.sber.transport.dispatcher.messages.ContractorUpdateTripMessage;
import ru.sber.transport.trip.business.dto.*;
import ru.sber.transport.trip.business.model.*;
import ru.sber.transport.trip.database.trips.tables.records.TripsRecord;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.lang.reflect.Type;
import java.sql.Array;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Маппер поездок.
 */
@Mapper(uses = {WaypointsMapper.class})
public interface TripMapper {

    /**
     * Сущность в модель.
     *
     * @param source исходная сущность.
     * @return бизнес-модель.
     */
    @Mapping(target = "requests", source = "source")
    Trip toModel(TripsRecord source);

    @SneakyThrows(JsonProcessingException.class)
    default Set<Request> requestToModel(TripsRecord source) {
        if (source == null) {
            return Set.of();
        }
        return objectMapper().readValue(source.getRequests().data(), new TypeReference<>() {
            @Override
            public Type getType() {
                return CollectionType.construct(Set.class, null, null, null, SimpleType.constructUnsafe(Request.class));
            }
        });
    }

    /**
     * Модель в сущность.
     *
     * @param trip исходная модель.
     * @return сущность.
     */
    @Mapping(target = "requests", source = "trip")
    TripsRecord toEntity(Trip trip);

    default Duration toDuration (Long val){
        return val != null ? Duration.ofMillis(val) : null;
    }

    default Long toLong (Duration val){
        return val != null ? val.toMillis() : null;
    }

    @SneakyThrows(JsonProcessingException.class)
    default JSON toJson(Trip source) {
        if (source == null) {
            return JSON.json("[]");
        }
        var requests = ReflectionUtils.castObjectToList(source.getRequests(), Request.class);
        return JSON.json(objectMapper().writeValueAsString(requests));
    }

    @SneakyThrows(JsonProcessingException.class)
    default JSON toJson(Trip.Information source) {
        if (source == null) {
            return JSON.json("{}");
        }

        return JSON.json(objectMapper().writeValueAsString(source));
    }


    @Lookup
    default ObjectMapper objectMapper() {
        return null;
    }

    @Mapping(target = "id", source = "trip.id")
    @Mapping(target = "driver", source = "driver")
    @Mapping(target = "dispatcher", source = "dispatcher")
    @Mapping(target = "vehicle", source = "vehicle")
    ContractorUpdateTripMessage toUpdateMessage(Trip trip, Driver driver, Dispatcher dispatcher, Vehicle vehicle);

    /**
     * Преобразовать в объект обмена данными для сокетов.
     *
     * @param trip исходная поездка.
     * @return целевой объект.
     */
    @Mapping(target = "requests", expression = "java(toMapList(trip))")
    @Mapping(target = "waypoints", qualifiedByName = "mapWaypoints")
    TripDto toDtoForSocket(Trip trip, boolean isNew);

    /**
     * Преобразовать в объект обмена данными для сокетов.
     *
     * @param trip исходная поездка.
     * @return целевой объект.
     */
    @Mapping(target = "requests",  expression = "java(toMapList(trip))")
    @Mapping(target = "id", source = "trip.id")
    @Mapping(target = "humanReadableId", source = "trip.humanReadableId")
    @Mapping(target = "contractorId", source = "trip.contractorId")
    @Mapping(target = "waypoints", qualifiedByName = "mapWaypoints")
    TripV2Dto toDtoForSocketV2(Trip trip, Driver driver, Dispatcher dispatcher, boolean isNew);

    @Mapping(target = "isNew", source = "newValue")
    TripV2Dto copyDtoForSocketV2WithIsNew(TripV2Dto tripV2Dto, boolean newValue);

    @Mapping(target = "id", source = "request.requestId")
    @Mapping(target = "expectedStartTime", source = "request.planStartTime")
    @Mapping(target = "waypoints", expression = "java(mapWaypoints(request.getRoutePoints()))")
    @Mapping(target = "contractorId", source = "contractorId")
    @Mapping(target = "humanReadableId", ignore = true)
    @Mapping(target = "requests", ignore = true)
    @Mapping(target = "taxiClass", source = "request.tripClass")
    @Mapping(target = "timeZone", expression = "java(getTimeZone(request.getPlanStartTime()))")
    @Mapping(target = "externalHumanReadableId", source = "request.humanReadableId")
    @Mapping(target = "expectedVehicleId", source = "request.expected.vehicleId")
    @Mapping(target = "expectedTime", source = "request.expected.time")
    @Mapping(target = "expectedDistance", source = "request.expected.distance")
    void fillTrip(@MappingTarget Trip trip, IntegrationRequestDTO request, UUID contractorId);

    /**
     * Преобразовать в объект обмена данными.
     *
     * @param trip исходная поездка.
     * @return целевой объект.
     */
    @Mapping(target = "isNew", ignore = true)
    @Mapping(target = "requests",  expression = "java(toMapList(trip))")
    @Mapping(target = "waypoints", qualifiedByName = "mapWaypoints")
    TripDto toDto(Trip trip);

    @Mapping(target = "isNew", ignore = true)
    @Mapping(target = "requests",  expression = "java(toMapList(source))")
    @Mapping(target = "id", source = "source.id")
    @Mapping(target = "humanReadableId", source = "source.humanReadableId")
    @Mapping(target = "contractorId", source = "source.contractorId")
    @Mapping(target = "waypoints", qualifiedByName = "mapWaypoints")
    @Mapping(target = "creationTime", expression = "java(mapTime(source.getCreationTime(), source.getRequests(), source.getTimeZone()))")
    @Mapping(target = "factEndTime", expression = "java(mapTime(source.getFactEndTime(), source.getRequests(), source.getTimeZone()))")
    @Mapping(target = "factStartTime", expression = "java(mapTime(source.getFactStartTime(), source.getRequests(), source.getTimeZone()))")
    @Mapping(target = "expectedEndTime", expression = "java(mapTime(source.getExpectedEndTime(), source.getRequests(), source.getTimeZone()))")
    @Mapping(target = "expectedStartTime", expression = "java(mapTime(source.getExpectedStartTime(), source.getRequests(), source.getTimeZone()))")
    @Mapping(target = "dispatcher.id", source = "dispatcher.id")
    @Mapping(target = "dispatcher.contactPhone", source = "dispatcher.phone")
    @Mapping(target = "dispatcher.lastName", source = "dispatcher.lastName")
    @Mapping(target = "dispatcher.firstName", source = "dispatcher.firstName")
    @Mapping(target = "dispatcher.patronymic", source = "dispatcher.patronymic")
    @Mapping(target = "dispatcher.contractorId", source = "dispatcher.contractorId")
    @Mapping(target = "dispatcher.humanReadableId", source = "dispatcher.humanReadableId")
    TripV2Dto toDtoV2(Trip source, Driver driver, Dispatcher dispatcher, Vehicle vehicle);

    @Mapping(target = "orderPartnerId", source = "source.humanReadableId")
    @Mapping(target = "statusCode", source = "source.status.code")
    @Mapping(target = "orderSbertransportId", source = "source.id")
    @Mapping(target = "createOrderTime", expression = "java(applyTimeZone(source.getCreationTime(), source.getTimeZone()))")
    @Mapping(target = "collectionTime", expression = "java(applyTimeZone(source.getFactStartTime(), source.getTimeZone()))")
    @Mapping(target = "price", source = "source.factCost")
    @Mapping(target = "distance", source = "source.factDistance")
    @Mapping(target = "finishTime", expression = "java(applyTimeZone(source.getFactEndTime(), source.getTimeZone()))")
    @Mapping(target = "performerArrivalTime", expression = "java(applyTimeZone(source.getArrivedDate(), source.getTimeZone()))")
    @Mapping(target = "driver", expression = "java(mapDriver(driver, vehicle))")
    @Mapping(target = "waitTime", source = "source.driverWaitingTime")
    @Mapping(target = "waitTimeOW", expression = "java(calculateFactTripTime(source.getFactStartTime(), source.getFactEndTime()))")
    GetTripResponse.Order toOrder(Trip source, Driver driver, Vehicle vehicle);

    @Mapping(target = "isNew", ignore = true)
    @Mapping(target = "requests",  expression = "java(toMapList(source))")
    @Mapping(target = "id", source = "source.id")
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "vehicle", ignore = true)
    @Mapping(target = "humanReadableId", source = "source.humanReadableId")
    @Mapping(target = "contractorId", source = "source.contractorId")
    @Mapping(target = "planned", expression = "java(fillPlanned(driver, vehicle))")
    @Mapping(target = "waypoints", qualifiedByName = "mapWaypoints")
    @Mapping(target = "creationTime", expression = "java(mapTime(source.getCreationTime(), source.getRequests(), source.getTimeZone()))")
    @Mapping(target = "factEndTime", expression = "java(mapTime(source.getFactEndTime(), source.getRequests(), source.getTimeZone()))")
    @Mapping(target = "factStartTime", expression = "java(mapTime(source.getFactStartTime(), source.getRequests(), source.getTimeZone()))")
    @Mapping(target = "expectedEndTime", expression = "java(mapTime(source.getExpectedEndTime(), source.getRequests(), source.getTimeZone()))")
    @Mapping(target = "expectedStartTime", expression = "java(mapTime(source.getExpectedStartTime(), source.getRequests(), source.getTimeZone()))")
    TripV2Dto toDtoV2WithPlanningData(Trip source, Driver driver, Dispatcher dispatcher, Vehicle vehicle);

    @Mapping(target = "creationTime", expression = "java(applyTimeZone(request.getCreationTime(), request.getTimeZone()))")
    @Mapping(target = "desiredDate", expression = "java(applyTimeZone(request.getDesiredDate(), request.getTimeZone()))")
    Request applyTimeZone(Request request);

    @SneakyThrows(JsonProcessingException.class)
    @Mapping(target = "driver.id", source = "driverBusynessRawResponse.driverData.id")
    @Mapping(target = "driver.humanReadableId", source = "driverBusynessRawResponse.driverData.humanReadableId")
    @Mapping(target = "trips", expression = "java(fillTrips(driverBusynessRawResponse.getTripData(), contractorDigitId, isPlanning))")
    DriverBusynessDTO.BusynessData toDto(DriverBusynessRawResponse driverBusynessRawResponse, Long contractorDigitId, boolean isPlanning);

    @SneakyThrows(JsonProcessingException.class)
    @Mapping(target = "vehicleId", source = "vehicleBusynessRawResponse.vehicleId")
    @Mapping(target = "trips", expression = "java(fillTrips(vehicleBusynessRawResponse.getTripData(), contractorDigitId, isPlanning, isOrdered))")
    VehicleBusynessDTO toDto(VehicleBusynessRawResponse vehicleBusynessRawResponse, Long contractorDigitId, boolean isPlanning, boolean isOrdered);

    default List<Map<String, Object>> toMapList(Trip trip) {
        var mapFunction = (Function<Request, Map<String, Object>>) this::toMap;
        return trip.getRequests().stream().map(mapFunction).toList();
    }

    default Map<String, Object> toMap(Request request) {
        return objectMapper().convertValue(request, new TypeReference<>() {});
    }

    default OffsetDateTime mapTime(OffsetDateTime offsetDateTime, Set<Request> requests, String timeZone){
        if(offsetDateTime != null) {
            if(!requests.isEmpty()) {
                var mainRequestTimeZone = requests.stream()
                        .min(Comparator.comparing(Request::getCreationTime))
                        .map(Request::getTimeZone);
                if (mainRequestTimeZone.isPresent()) {
                    var zoneOffset = ZoneOffset.of(ZoneId.of(mainRequestTimeZone.get()).normalized().getId());
                    return offsetDateTime.withOffsetSameInstant(zoneOffset);
                } else return offsetDateTime;
            }
            if(timeZone != null && !timeZone.isEmpty()){
                var zoneOffset = ZoneOffset.of(ZoneId.of(timeZone).normalized().getId());
                return offsetDateTime.withOffsetSameInstant(zoneOffset);
            } else return offsetDateTime;
        } else return null;
    }

    default OffsetDateTime applyTimeZone(OffsetDateTime offsetDateTime, String timeZone){
        if(offsetDateTime != null) {
            if(timeZone != null && !timeZone.isEmpty()) {
                var zoneOffset = ZoneOffset.of(ZoneId.of(timeZone).normalized().getId());
                return offsetDateTime.withOffsetSameInstant(zoneOffset);
            } else return offsetDateTime;
        } else return null;
    }

    @Named("mapWaypoints")
    default RequestDto.Waypoint toWaypointDto(Waypoint waypoint){
        String phone = null;
        String name = null;
        if(waypoint.contact()!=null){
            phone = waypoint.contact().phone();
            name = waypoint.contact().name();
        }
        var passengerList = new ArrayList<RequestDto.Passenger>();
        if(waypoint.passengers()!=null && !waypoint.passengers().isEmpty()) {
            waypoint.passengers().forEach(p -> passengerList.add(new RequestDto.Passenger(
                    p.type(), p.firstName(), p.patronymic(), p.phone()
            )));
        }
        return new RequestDto.Waypoint(
                waypoint.id(),
                waypoint.country(),
                waypoint.region(),
                waypoint.city(),
                waypoint.street(),
                waypoint.house(),
                waypoint.building(),
                null,
                waypoint.latitude(),
                waypoint.longitude(),
                waypoint.orderingIndex(),
                waypoint.fullAddress(),
                new RequestDto.Contact(
                        phone,
                        name
                ),
                passengerList
        );
    }

    @SneakyThrows(JsonProcessingException.class)
    default List<DriverBusynessDTO.TripData> fillTrips(DriverBusynessRawResponse.TripData tripData, Long contractorDigitId, boolean isPlanning) {
        Set<Request> requests = objectMapper().readValue(tripData.getRequests().data(), new TypeReference<>() {
            @Override
            public Type getType() {
                return CollectionType.construct(Set.class, null, null, null, SimpleType.constructUnsafe(Request.class));
            }
        });
        List<Waypoint> waypoints = objectMapper().readValue(tripData.getWaypoints().data(), new TypeReference<>() {});
        var model = new DriverBusynessDTO.VehicleModelData();
        model.setBrand(tripData.getVehicleData().getBrand());
        model.setName(tripData.getVehicleData().getModel());
        var vehicle = new DriverBusynessDTO.VehicleData();
        vehicle.setId(tripData.getVehicleData().getId());
        vehicle.setStateNumber(tripData.getVehicleData().getStateNumber());
        vehicle.setVehicleType("PASSENGER");
        vehicle.setModel(model);
        var trip = new DriverBusynessDTO.TripData();
        trip.setId(tripData.getId());
        trip.setHumanReadableId("%s-%04d-%08d".formatted(Prefix.TP, contractorDigitId, tripData.getDigitId()));
        trip.setStatus(tripData.getStatus());
        trip.setExpectedStartTime(mapTime(tripData.getExpectedStartTime(), requests, tripData.getTimeZone()));
        trip.setExpectedEndTime(mapTime(tripData.getExpectedEndTime(), requests, tripData.getTimeZone()));
        trip.setFactStartTime(mapTime(tripData.getFactStartTime(), requests, tripData.getTimeZone()));
        trip.setFactEndTime(mapTime(tripData.getFactEndTime(), requests, tripData.getTimeZone()));
        trip.setPlanning(isPlanning);
        trip.setVehicle(vehicle);
        trip.setStartAddress(new DriverBusynessDTO.Address(waypoints.get(0).fullAddress(), waypoints.get(0).latitude(), waypoints.get(0).longitude()));
        if(waypoints.get(0).passengers() != null && !waypoints.get(0).passengers().isEmpty()) {
            var passengerList = new ArrayList<String>();
            waypoints.get(0).passengers().forEach(p -> passengerList.add(p.firstName()+" "+p.patronymic()));
            trip.setPassenger(passengerList);
        }
        var list = new ArrayList<DriverBusynessDTO.TripData>();
        list.add(trip);
        return list;
    }

    @SneakyThrows(JsonProcessingException.class)
    default List<VehicleBusynessDTO.TripData> fillTrips(VehicleBusynessRawResponse.TripData tripData, Long contractorDigitId, boolean isPlanning, boolean isOrdered) {
        Set<Request> requests = objectMapper().readValue(tripData.getRequests().data(), new TypeReference<>() {
            @Override
            public Type getType() {
                return CollectionType.construct(Set.class, null, null, null, SimpleType.constructUnsafe(Request.class));
            }
        });
        var trip = new VehicleBusynessDTO.TripData();
        trip.setId(tripData.getId());
        trip.setHumanReadableId("%s-%04d-%08d".formatted(Prefix.TP, contractorDigitId, tripData.getDigitId()));
        trip.setStatus(tripData.getStatus());
        trip.setExpectedStartTime(mapTime(tripData.getExpectedStartTime(), requests, tripData.getTimeZone()));
        trip.setExpectedEndTime(mapTime(tripData.getExpectedEndTime(), requests, tripData.getTimeZone()));
        trip.setFactStartTime(mapTime(tripData.getFactStartTime(), requests, tripData.getTimeZone()));
        trip.setFactEndTime(mapTime(tripData.getFactEndTime(), requests, tripData.getTimeZone()));
        trip.setPlanning(isPlanning);
        trip.setOrdered(isOrdered);
        var list = new ArrayList<VehicleBusynessDTO.TripData>();
        list.add(trip);
        return list;
    }

    default Duration getTripDuration(Trip source){
        if (source.getStatus().isTerminal()){
            return Duration.ofSeconds(source.getFactEndTime().toEpochSecond() - source.getFactStartTime().toEpochSecond());
        } else return null;
    }

    default TripV2Dto.Planned fillPlanned(Driver driver, Vehicle vehicle){
        var driverDto = new DriverShortDTO(driver.getId(), driver.getHumanReadableId(), driver.getContractorId(),
                driver.getLastName(), driver.getFirstName(), driver.getPatronymic(), driver.getContactPhone(),
                driver.getRating(), driver.isActive(), driver.getShiftId());
        var vehicleDto = new VehicleDTO(vehicle.getId(), vehicle.getBrand(), vehicle.getModel(), vehicle.getStateNumber(),
                vehicle.getColor(), vehicle.getContractorId(), vehicle.isDeleted(), "PASSENGER");
        return new TripV2Dto.Planned(driverDto, vehicleDto);
    }

    default List<Waypoint> mapWaypoints(IntegrationRequestDTO.OrderRoutePoints routePoints){
        var waypoints = new ArrayList<Waypoint>();
        routePoints.getWaypoints().forEach(point -> waypoints.add(toTripWaypoint(point, waypoints.size())));
        return waypoints;
    }


    default Waypoint toTripWaypoint(IntegrationRequestDTO.Waypoint waypoint, int index){
        var passengerList = new ArrayList<Waypoint.Passenger>();
        waypoint.getPassengers().forEach(p -> passengerList.add(
                new Waypoint.Passenger(PassengerActionType.valueOf(p.getType()),
                        p.getFirstName(), p.getPatronymic(), p.getMobilePhone())));
        return new Waypoint(
                null,
                waypoint.getLatitude(),
                waypoint.getLongitude(),
                index,
                null,
                null,
                null,
                null,
                null,
                null,
                Duration.ofSeconds(waypoint.getWaitTime()),
                waypoint.getName(),
                null,
                passengerList
        );
    }

    default GetTripResponse.DriverInfo mapDriver(Driver driver, Vehicle vehicle){
        if(driver != null && vehicle != null) {
            return new GetTripResponse.DriverInfo(
                    driver.getId(),
                    driver.getFirstName(),
                    driver.getPatronymic(),
                    driver.getLastName(),
                    driver.getContactPhone(),
                    driver.getLatitude(),
                    driver.getLongitude(),
                    new GetTripResponse.VehicleInfo(
                            vehicle.getBrand(),
                            vehicle.getModel(),
                            vehicle.getColor(),
                            vehicle.getStateNumber()
                    )
            );
        } else return null;
    }

    default String getTimeZone(OffsetDateTime offsetDateTime) {
        if (offsetDateTime != null) {
            return offsetDateTime.getOffset().toString();
        } else return null;
    }

    default Trip.Information jSONToInformation(JSON jSON) {
        if ( jSON == null ) {
            return null;
        }
        try {
            return objectMapper().readValue(jSON.data(), Trip.Information.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    default Integer calculateFactTripTime(OffsetDateTime factStartTime, OffsetDateTime factEndTime){
        if(factStartTime != null && factEndTime != null){
            return Math.toIntExact(ChronoUnit.SECONDS.between(factStartTime, factEndTime));
        } return null;
    }
}
