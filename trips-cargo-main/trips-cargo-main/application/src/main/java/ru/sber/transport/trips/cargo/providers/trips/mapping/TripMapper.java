package ru.sber.transport.trips.cargo.providers.trips.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.SimpleType;
import lombok.SneakyThrows;
import org.jooq.JSON;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Lookup;
import ru.sber.transport.dispatcher.messages.ContractorUpdateTripMessage;
import ru.sber.transport.trips.cargo.business.dto.*;
import ru.sber.transport.trips.cargo.business.model.*;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.records.TripsRecord;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.lang.reflect.Type;
import java.time.*;
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
    default Set<CargoRequest> requestToModel(TripsRecord source) {
        if (source == null) {
            return Set.of();
        }
        return objectMapper().readValue(source.getRequests().data(), new TypeReference<>() {
            @Override
            public Type getType() {
                return CollectionType.construct(Set.class, null, null, null, SimpleType.constructUnsafe(CargoRequest.class));
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

    @SneakyThrows(JsonProcessingException.class)
    default JSON toJson(Trip source) {
        if (source == null) {
            return JSON.json("[]");
        }
        var requests = ReflectionUtils.castObjectToList(source.getRequests(), CargoRequest.class);
        return JSON.json(objectMapper().writeValueAsString(requests));
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
    @Mapping(target = "id", source = "trip.id")
    @Mapping(target = "humanReadableId", source = "trip.humanReadableId")
    @Mapping(target = "contractorId", source = "trip.contractorId")
    @Mapping(target = "waypoints", qualifiedByName = "mapWaypoints")
    TripV2Dto toDtoForSocketV2(Trip trip, Driver driver, Dispatcher dispatcher, boolean isNew);

    /**
     * Изменить флаг "Новая поездка"
     * @param tripV2Dto
     * @param newValue
     * @return
     */
    @Mapping(target = "isNew", source = "newValue")
    TripV2Dto copyDtoForSocketV2WithIsNew(TripV2Dto tripV2Dto, boolean newValue);

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
    @Mapping(target = "endTime", expression = "java(mapTime(source.getEndTime(), source.getRequests(), source.getTimeZone()))")
    @Mapping(target = "startTime", expression = "java(mapTime(source.getStartTime(), source.getRequests(), source.getTimeZone()))")
    @Mapping(target = "dispatcherStartTime", expression = "java(mapTime(source.getDispatcherStartTime(), source.getRequests(), source.getTimeZone()))")
    TripV2Dto toDtoV2(Trip source, Driver driver, Dispatcher dispatcher, Vehicle vehicle);

    @Mapping(target = "isNew", ignore = true)
    @Mapping(target = "requests",  expression = "java(toMapList(source))")
    @Mapping(target = "id", source = "source.id")
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "humanReadableId", source = "source.humanReadableId")
    @Mapping(target = "contractorId", source = "source.contractorId")
    @Mapping(target = "planned", expression = "java(fillPlanned(driver, vehicle))")
    @Mapping(target = "waypoints", qualifiedByName = "mapWaypoints")
    @Mapping(target = "creationTime", expression = "java(mapTime(source.getCreationTime(), source.getRequests(), source.getTimeZone()))")
    @Mapping(target = "endTime", expression = "java(mapTime(source.getEndTime(), source.getRequests(), source.getTimeZone()))")
    @Mapping(target = "startTime", expression = "java(mapTime(source.getStartTime(), source.getRequests(), source.getTimeZone()))")
    TripV2Dto toDtoV2WithPlanningData(Trip source, Driver driver, Dispatcher dispatcher, Vehicle vehicle);

    @Mapping(target = "creationTime", expression = "java(applyTimeZone(cargoRequest.getCreationTime(), cargoRequest.getTimeZone()))")
    @Mapping(target = "approvalDate", expression = "java(applyTimeZone(cargoRequest.getApprovalDate(), cargoRequest.getTimeZone()))")
    @Mapping(target = "desiredDate", expression = "java(applyTimeZone(cargoRequest.getDesiredDate(), cargoRequest.getTimeZone()))")
    @Mapping(target = "finishedTime", expression = "java(applyTimeZone(cargoRequest.getFinishedTime(), cargoRequest.getTimeZone()))")
    @Mapping(target = "transferTime", expression = "java(applyTimeZone(cargoRequest.getTransferTime(), cargoRequest.getTimeZone()))")
    @Mapping(target = "shipmentTime", expression = "java(applyTimeZone(cargoRequest.getShipmentTime(), cargoRequest.getTimeZone()))")
    @Mapping(target = "timeWorkStart", expression = "java(applyTimeZone(cargoRequest.getTimeWorkStart(), cargoRequest.getTimeZone()))")
    @Mapping(target = "timeWorkFinish", expression = "java(applyTimeZone(cargoRequest.getTimeWorkFinish(), cargoRequest.getTimeZone()))")
    CargoRequest applyTimeZone(CargoRequest cargoRequest);

    @Mapping(target = "orderParthnerId", source = "source.humanReadableId")
    @Mapping(target = "orderSbertransportId", source = "source.routeHumanReadableId")
    @Mapping(target = "createOrderTime", expression = "java(applyTimeZone(source.getCreationTime(), source.getTimeZone()))")
    @Mapping(target = "collectionTime", expression = "java(applyTimeZone(source.getStartTime(), source.getTimeZone()))")
    @Mapping(target = "price", source = "source.factCost")
    @Mapping(target = "distance", source = "source.factDistance")
    @Mapping(target = "statusCode", source = "source.status.code")
    @Mapping(target = "finishTime", expression = "java(applyTimeZone(source.getEndTime(), source.getTimeZone()))")
    @Mapping(target = "performerArrivalTime", expression = "java(applyTimeZone(source.getArrivedDate(), source.getTimeZone()))")
    @Mapping(target = "driver", expression = "java(mapDriver(driver, vehicle))")
    @Mapping(target = "waitTime", source = "source.driverWaitingTime")
    @Mapping(target = "factLoaders", source = "source.loaders")
    @Mapping(target = "requests", expression = "java(mapRequests(source))")
    GetTripResponse.Order toIntegrationResponse(Trip source, Driver driver, Vehicle vehicle);

    @SneakyThrows(JsonProcessingException.class)
    @Mapping(target = "driver.id", source = "driverBusynessRawResponse.driverData.id")
    @Mapping(target = "driver.humanReadableId", source = "driverBusynessRawResponse.driverData.humanReadableId")
    @Mapping(target = "trips", expression = "java(fillTrips(driverBusynessRawResponse.getTripData(), contractorDigitId))")
    DriverBusynessDTO.BusynessData toDto(DriverBusynessRawResponse driverBusynessRawResponse, Long contractorDigitId);

    @SneakyThrows(JsonProcessingException.class)
    @Mapping(target = "vehicleId", source = "vehicleBusynessRawResponse.vehicleId")
    @Mapping(target = "trips", expression = "java(fillTrips(vehicleBusynessRawResponse.getTripData(), contractorDigitId))")
    VehicleBusynessDTO toDto(VehicleBusynessRawResponse vehicleBusynessRawResponse, Long contractorDigitId);

    default List<GetTripResponse.Request> mapRequests(Trip trip) {
        return trip.getWaypoints().stream().flatMap(waypoint -> waypoint.contacts().stream())
                .flatMap(contact -> contact.requests().stream())
                .collect(Collectors.toMap(
                        Waypoint.RouteRequest::getHumanReadableId,
                        Waypoint.RouteRequest::getQrs,
                        (a, b) -> {
                            a.addAll(b);
                            return a;
                        }))
                .entrySet().stream().map(entry -> new GetTripResponse.Request(entry.getKey(), entry.getValue()))
                .toList();
    }

    default List<Map<String, Object>> toMapList(Trip trip) {
        var mapFunction = (Function<CargoRequest, Map<String, Object>>) this::toMap;
        return trip.getRequests().stream().map(mapFunction).toList();
    }

    default Map<String, Object> toMap(CargoRequest request) {
        return objectMapper().convertValue(request, new TypeReference<>() {});
    }

    @Named("mapWaypoints")
    default RequestDto.Waypoint toWaypointDto(Waypoint waypoint){
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
                waypoint.address(),
                mapContact(waypoint.contacts())
        );
    }

    default List<RequestDto.Contact> mapContact(List<Waypoint.Contact> contactList){
        var contacts = new ArrayList<RequestDto.Contact>();
        if(contactList!=null) {
            contactList.forEach(contact -> {
                var contactData = new RequestDto.ContactData(contact.contact().fullName(), contact.contact().phone());
                var newContact = new RequestDto.Contact(contactData, mapRouteRequest(contact.requests()));
                contacts.add(newContact);
            });
            return contacts;
        } else return Collections.emptyList();
    }

    default List<RequestDto.RouteRequest> mapRouteRequest(List<Waypoint.RouteRequest> requestList){
        var requests = new ArrayList<RequestDto.RouteRequest>();
        if(requestList!=null) {
            requestList.forEach(request -> {
                var newRouteRequest = new RequestDto.RouteRequest(
                        request.getHumanReadableId(),
                        request.getType(),
                        request.getOrganization(),
                        mapCargoData(request.getCargo()),
                        request.getLoaders(),
                        request.getComment(),
                        mapPack(request.getPack()),
                        request.getQrs()
                );
                requests.add(newRouteRequest);
            });
            return requests;
        } else return Collections.emptyList();
    }

    default List<RequestDto.CargoData> mapCargoData(List<Waypoint.CargoData> cargoDataList){
        var cargoData = new ArrayList<RequestDto.CargoData>();
        if(cargoDataList!=null) {
            cargoDataList.forEach(data -> {
                var newCargoData = new RequestDto.CargoData(
                        data.orderingIndex(),
                        data.cargoName(),
                        data.weight(),
                        data.volume(),
                        data.occupiedPlacesCount(),
                        data.height(),
                        data.length(),
                        data.width(),
                        data.fragile()
                );
                cargoData.add(newCargoData);
            });
            return cargoData;
        } else return Collections.emptyList();
    }

    default List<RequestDto.Pack> mapPack(List<Waypoint.Pack> packList){
        var packs = new ArrayList<RequestDto.Pack>();
        if(packList!=null) {
            packList.forEach(pack -> {
                var newPack = new RequestDto.Pack(
                        pack.name(),
                        pack.unit(),
                        pack.count()
                );
                packs.add(newPack);
            });
            return packs;
        } else return Collections.emptyList();
    }
    default OffsetDateTime mapTime(OffsetDateTime offsetDateTime, Set<CargoRequest> cargoRequests, String timeZone){
        if(offsetDateTime != null) {
            if(!cargoRequests.isEmpty()) {
                var mainRequestTimeZone = cargoRequests.stream()
                        .min(Comparator.comparing(CargoRequest::getCreationTime))
                        .map(CargoRequest::getTimeZone);
                if (mainRequestTimeZone.isPresent()) {
                    var zoneOffset = ZoneOffset.of(ZoneId.of(mainRequestTimeZone.get()).normalized().getId());
                    return offsetDateTime.withOffsetSameInstant(zoneOffset);
                } else return offsetDateTime;
            } else return applyTimeZone(offsetDateTime, timeZone);
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

    @SneakyThrows(JsonProcessingException.class)
    default List<DriverBusynessDTO.TripData> fillTrips(DriverBusynessRawResponse.TripData tripData, Long contractorDigitId) {
        Set<CargoRequest> requests = objectMapper().readValue(tripData.getRequests().data(), new TypeReference<>() {
            @Override
            public Type getType() {
                return CollectionType.construct(Set.class, null, null, null, SimpleType.constructUnsafe(CargoRequest.class));
            }
        });
        var model = new DriverBusynessDTO.VehicleModelData();
        model.setBrand(tripData.getVehicleData().getBrand());
        model.setName(tripData.getVehicleData().getModel());
        var vehicle = new DriverBusynessDTO.VehicleData();
        vehicle.setId(tripData.getVehicleData().getId());
        vehicle.setStateNumber(tripData.getVehicleData().getStateNumber());
        vehicle.setVehicleType("CARGO");
        vehicle.setModel(model);
        var trip = new DriverBusynessDTO.TripData();
        trip.setId(tripData.getId());
        trip.setHumanReadableId("%s-%04d-%08d".formatted(Prefix.TC, contractorDigitId, tripData.getDigitId()));
        trip.setStatus(tripData.getStatus());
        trip.setStartTime(mapTime(tripData.getStartTime(), requests, null));
        trip.setDispatcherStartTime(mapTime(tripData.getDispatcherStartTime(), requests, null));
        trip.setEndTime(mapTime(tripData.getEndTime(), requests, null));
        trip.setVehicle(vehicle);
        var list = new ArrayList<DriverBusynessDTO.TripData>();
        list.add(trip);
        return list;
    }

    @SneakyThrows(JsonProcessingException.class)
    default List<VehicleBusynessDTO.TripData> fillTrips(VehicleBusynessRawResponse.TripData tripData, Long contractorDigitId) {
        Set<CargoRequest> requests = objectMapper().readValue(tripData.getRequests().data(), new TypeReference<>() {
            @Override
            public Type getType() {
                return CollectionType.construct(Set.class, null, null, null, SimpleType.constructUnsafe(CargoRequest.class));
            }
        });
        var trip = new VehicleBusynessDTO.TripData();
        trip.setId(tripData.getId());
        trip.setHumanReadableId("%s-%04d-%08d".formatted(Prefix.TC, contractorDigitId, tripData.getDigitId()));
        trip.setStatus(tripData.getStatus());
        trip.setExpectedStartTime(mapTime(tripData.getExpectedStartTime(), requests, null));
        trip.setExpectedEndTime(mapTime(tripData.getExpectedEndTime(), requests, null));
        var list = new ArrayList<VehicleBusynessDTO.TripData>();
        list.add(trip);
        return list;
    }

    default Duration mapper(Long value) {
        if (value != null) {
            return Duration.ofMillis(value);
        } else return null;
    }

    default Long mapper(Duration value) {
        if (value != null) {
            return value.toMillis();
        } else return null;
    }

    default Duration getTripDuration(Trip source){
        if (source.getStatus().isTerminal()){
            return Duration.ofSeconds(source.getEndTime().toEpochSecond() - source.getStartTime().toEpochSecond());
        } else return null;
    }

    default GetTripResponse.Driver mapDriver(Driver driver, Vehicle vehicle){
        if(driver != null && vehicle != null) {
            return new GetTripResponse.Driver(
                    driver.getLastName(),
                    driver.getFirstName(),
                    driver.getPatronymic(),
                    driver.getContactPhone(),
                    new GetTripResponse.Vehicle(
                            vehicle.getBrand(),
                            vehicle.getModel(),
                            vehicle.getStateNumber(),
                            vehicle.getColor()
                    )
            );
        } else return null;
    }

    default TripV2Dto.Planned fillPlanned(Driver driver, Vehicle vehicle){
        var driverDto = new DriverShortDTO(driver.getId(), driver.getHumanReadableId(), driver.getContractorId(),
                driver.getLastName(), driver.getFirstName(), driver.getPatronymic(), driver.getContactPhone(),
                driver.getRating(), driver.isActive(), driver.getShiftId());
        var vehicleDto = new VehicleDTO(vehicle.getId(), vehicle.getBrand(), vehicle.getModel(), vehicle.getStateNumber(),
                vehicle.getColor(), vehicle.getContractorId(), vehicle.isDeleted(), "CARGO");
        return new TripV2Dto.Planned(driverDto, vehicleDto);
    }

}
