package ru.sberbank.ditsib.transport.request.dto.mapper;

import jakarta.validation.constraints.NotNull;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValueMappingStrategy;
import ru.sber.transport.constants.PointMatchingType;
import ru.sber.transport.srm.model.SrmRequestDTO;
import ru.sber.transport.srm.model.SrmWaypointPostDTO;
import ru.sberbank.ditsib.transport.constants.PublicCompensationType;
import ru.sberbank.ditsib.transport.constants.PublicTransportType;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.mappers.FraudMapper;
import ru.sberbank.ditsib.transport.request.messaging.message.EmployeeMessage;
import ru.sberbank.ditsib.transport.request.messaging.message.PositionMessage;
import ru.sberbank.ditsib.transport.request.database.model.AbstractRequestForTnP;
import ru.sberbank.ditsib.transport.request.database.model.Address;
import ru.sberbank.ditsib.transport.request.database.model.Coordinates;
import ru.sberbank.ditsib.transport.request.database.model.ExpectedData;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.database.model.RequestForCarsharing;
import ru.sberbank.ditsib.transport.request.database.model.RequestForGroupTransfer;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPersonal;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPublic;
import ru.sberbank.ditsib.transport.request.database.model.RequestForTaxi;
import ru.sberbank.ditsib.transport.request.database.model.RequestHistoryElement;
import ru.sberbank.ditsib.transport.request.database.model.RequestRating;
import ru.sberbank.ditsib.transport.request.database.model.RouteSegment;
import ru.sberbank.ditsib.transport.request.database.model.TripPurpose;
import ru.sberbank.ditsib.transport.request.database.model.Waypoint;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingTrip;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Position;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.CompensationDocument;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.TransportCompensation;
import ru.sberbank.ditsib.transport.request.dto.CoordinatesDTO;
import ru.sberbank.ditsib.transport.request.dto.ExpectedDataDTO;
import ru.sberbank.ditsib.transport.request.dto.GetCarsharingRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.GetGroupTransferRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.GetPersonalRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.GetPublicRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.GetRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.GetRequestWithFactDataDTO;
import ru.sberbank.ditsib.transport.request.dto.NewRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.PublicCompensationTypeDTO;
import ru.sberbank.ditsib.transport.request.dto.PublicTransportTypeDTO;
import ru.sberbank.ditsib.transport.request.dto.RequestDTO;
import ru.sberbank.ditsib.transport.request.dto.RequestHistoryElementDTO;
import ru.sberbank.ditsib.transport.request.dto.RequestRatingDTO;
import ru.sberbank.ditsib.transport.request.dto.RouteSegmentDTO;
import ru.sberbank.ditsib.transport.request.dto.TripPurposeDTO;
import ru.sberbank.ditsib.transport.request.dto.WaypointDTO;
import ru.sberbank.ditsib.transport.request.dto.carsharing.CarsharingTripDTO;
import ru.sberbank.ditsib.transport.request.dto.publicTransport.CompensationDocumentDTO;
import ru.sberbank.ditsib.transport.request.dto.publicTransport.NewRequestForCompensationDTO;
import ru.sberbank.ditsib.transport.request.dto.publicTransport.NewTransportCompensationDTO;
import ru.sberbank.ditsib.transport.request.dto.publicTransport.RequestForCompensationDTO;
import ru.sberbank.ditsib.transport.request.dto.publicTransport.TransportCompensationDTO;
import ru.sberbank.ditsib.transport.request.mappers.EmployeeMapper;
import ru.sberbank.ditsib.transport.request.mappers.VehicleMapper;

import java.time.Duration;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Mapper for converting  entity to dto and back
 */
@Mapper(uses = { EmployeeMapper.class, VehicleMapper.class, FraudMapper.class})
public interface EntityDTOMapper {
    
    @Mapping(target = "waitTime", source = "waypointDTO.waitTime")
    @Mapping(target = "address", source = "waypointDTO")
    @Mapping(target = "orderingIndex", source = "orderIndex")
    Waypoint dtoToWaypoint(WaypointDTO waypointDTO, int orderIndex);
    
    default WaypointDTO waypointToDto(@NotNull Waypoint source) {
        return WaypointDTO.builder()
                          .building(source.getAddress().getBuilding())
                          .city(source.getAddress().getCity())
                          .country(source.getAddress().getCountry())
                          .latitude(source.getAddress().getLatitude())
                          .longitude(source.getAddress().getLongitude())
                          .house(source.getAddress().getHouse())
                          .region(source.getAddress().getRegion())
                          .street(source.getAddress().getStreet())
                          .structure(source.getAddress().getStructure())
                          .waitTime(source.getWaitTime())
                          .checkinAutomatic(source.isCheckinAutomatic())
                          .checkinManual(source.isCheckinManual())
                          .absenceReason(source.getAbsenceReason())
                          .checkinOnlyManual(source.isCheckinOnlyManual())
                          .active(source.isActive())
                          .existInVspGosbTbRegistry(Optional.ofNullable(source.getAddress().isExistInVspGosbTbRegistry()).orElse(false))
                          .build();
    }
    
    default List<TransportCompensationDTO> toTransportCompensation(RequestForPublic request) {
        List<TransportCompensationDTO> transportCompensationDTOS = transportCompensationsToDto(request.getTransportCompensation());
        
        if (request.getCompensationDocuments() != null && !request.getCompensationDocuments().isEmpty()) {
            for (TransportCompensationDTO compensationDTO : transportCompensationDTOS) {
                if (compensationDTO.getAttachedDocumentId() == null) {
                    continue;
                }
                
                request.getCompensationDocuments().stream().
                       filter(doc -> Objects.equals(doc.getId(), compensationDTO.getAttachedDocumentId())).
                       findFirst().
                       ifPresent(doc -> compensationDTO.setCompensationDocumentDTO(documentToDto(doc)));
            }
        }
        return transportCompensationDTOS;
    }
    
    default Employee employeeFromId(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        Employee employee = new Employee();
        employee.setId(uuid);
        return employee;
    }
    
    //Действительные методы по маппингу для RequestForPublic
    CompensationDocumentDTO documentToDto(CompensationDocument doc);
    
    CompensationDocument dtoToDoc(CompensationDocumentDTO docDto);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    List<CompensationDocumentDTO> documentsToDto(List<CompensationDocument> docs);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    List<CompensationDocument> dtoToDocuments(List<CompensationDocumentDTO> docsDto);
    
    default PublicCompensationTypeDTO publicCompensationTypeToDto(PublicCompensationType type) {
        return PublicCompensationTypeDTO.builder()
                                        .name(type.name())
                                        .rusName(type.getRusName())
                                        .attachmentDocumentRequired(type.isAttachmentDocumentRequired())
                                        .expirationDatesRequired(type.isExpirationDatesRequired())
                                        .build();
    }
    
    default PublicCompensationType dtoToPublicCompensationType(PublicCompensationTypeDTO dto) {
        return PublicCompensationType.getByName(dto.getName()).orElse(null);
    }
    
    default PublicTransportTypeDTO publicTransportTypeToDto(PublicTransportType type) {
        return PublicTransportTypeDTO.builder()
                                     .name(type.name())
                                     .rusName(type.getRusName())
                                     .publicCompensationType(type.getPublicCompensationType().name())
                                     .build();
    }
    
    default PublicTransportType dtoToPublicTransportType(PublicTransportTypeDTO dto) {
        return PublicTransportType.getByName(dto.getName()).orElse(null);
    }
    
    TransportCompensationDTO compensationsToDto(TransportCompensation doc);
    
    TransportCompensation dtoToCompensations(TransportCompensationDTO docDto);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    List<TransportCompensationDTO> transportCompensationsToDto(List<TransportCompensation> data);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    List<TransportCompensation> dtoToTransportCompensations(List<TransportCompensationDTO> dtos);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    List<TransportCompensationDTO> newDtoToTransportCompensations(List<NewTransportCompensationDTO> dtos);
    
    @Mapping(source = "segmentsJSON", target = "segments")
    @Mapping(target = "transportCompensation", expression = "java(toTransportCompensation(request))")
    @Mapping(target = "statusCodeDescription", expression = "java(getStatusCodeDescription(request))")
    @Mapping(source = "fraudData", target = "fraudComment")
    RequestForCompensationDTO compensationToPublicDto(RequestForPublic request);
    
    @Mapping(target = "segmentsJSON", source = "segments")
    RequestForPublic newCompensationDtoToRequest(NewRequestForCompensationDTO newSuburbDto);
    
    @Mapping(source = "segments", target = "segmentsJSON")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "transportType", ignore = true)
    @Mapping(target = "transportCompensation", ignore = true)
    @Mapping(target = "waypoints", ignore = true)
    RequestForPublic updatePublicRequestFromCompensationDto(
            RequestForCompensationDTO dto, @MappingTarget RequestForPublic request
                                                           );
    
    void updateTransportCompensation(
            TransportCompensation source, @MappingTarget TransportCompensation target
                                    );
    
    Address waypointDTOToAddress(WaypointDTO waypointDTO);
    
    Coordinates dtoToCoordinates(CoordinatesDTO data);
    
    CoordinatesDTO coordinatesToDTO(Coordinates data);
    
    RouteSegment dtoToRouteSegment(RouteSegmentDTO data);
    
    ExpectedData dtoToExpectedData(ExpectedDataDTO data);
    
    
    @Mapping(source = "cost", target = "cost")
    @Mapping(source = "distance", target = "distance")
    @Mapping(source = "time", target = "time")
    ExpectedDataDTO expectedDataToDTO(ExpectedData data);
    
    
    @Mapping(source = "expected.cost", target = "cost")
    @Mapping(source = "expected.distance", target = "distance")
    @Mapping(source = "expected.time", target = "time")
    @Mapping(source = "expected.bonusCost", target = "bonusCost")
    @Mapping(source = "waypoints", target = "waypoints")
    @Mapping(source = "segmentsJSON", target = "segments")
    ExpectedDataDTO requestToExpectedDataDTO(Request data);
    
    
    @Mapping(source = "humanReadableId", target = "humanReadableId")
    @Mapping(source = "data", target = "expected")
    GetRequestDTO requestToGetDTO(Request data);
    
    
    @Mapping(source = "humanReadableId", target = "humanReadableId")
    @Mapping(source = "data", target = "expected")
    @Mapping(source = "resolution", target = "resolution")
    @Mapping(source = "rideId", target = "sharedRideId")
    @Mapping(expression = "java(getStatusCodeDescription(data))", target = "statusCodeDescription")
    @Mapping(source = "driverArrivedDeadline", target = "deadline")
    @Mapping(source = "fraudData", target = "fraudComment")
    GetRequestDTO requestToGetDTO(RequestForTaxi data);
    
    default String getStatusCodeDescription(RequestForTaxi source) {
        return Optional.ofNullable(source)
                       .map(Request::getStatusCode)
                       .flatMap(TripRequestStatus.TaxiStatusCode::findByCode)
                       .map(TripRequestStatus.TaxiStatusCode::getDescription)
                       .orElse(null);
    }
    
    @Mapping(target = "factDataDTO", source = "data.taxiTrip")
    @Mapping(target = "factParametersSettingTime", source = "data.taxiTrip.factParametersSettingTime")
    @Mapping(source = "resolution", target = "resolution")
    @Mapping(source = "rideId", target = "sharedRideId")
    GetRequestWithFactDataDTO requestToGetRequestWithFactDataDTO(RequestForTaxi data);
    
    default String getStatusCodeDescription(RequestForPersonal source) {
        return Optional.ofNullable(source)
                       .map(Request::getStatusCode)
                       .flatMap(TripRequestStatus.PersonalStatusCode::findByCode)
                       .map(TripRequestStatus.PersonalStatusCode::getDescription)
                       .orElse(null);
    }
    
    default Long longToMinutes(Duration deadline) {
        return Optional.ofNullable(deadline).map(Duration::toMillis).orElse(null);
    }
    
    
    @Mapping(source = "data", target = "expected")
    @Mapping(source = "personalCar", target = "personalCar")
    @Mapping(target = "driver", ignore = true)
    @Mapping(source = "rideId", target = "sharedRideId")
    @Mapping(expression = "java(getStatusCodeDescription(data))", target = "statusCodeDescription")
    GetPersonalRequestDTO requestToGetPersonalDTO(RequestForPersonal data);
    
    @Mapping(target = "expected", expression = "java(requestToExpectedDataDTO(data))")
    @Mapping(target = "statusCodeDescription", expression = "java(getStatusCodeDescription(data))")
    GetPublicRequestDTO requestToGetPublicDTO(RequestForPublic data);
    
    default String getStatusCodeDescription(RequestForPublic source) {
        return Optional.ofNullable(source)
                       .map(Request::getStatusCode)
                       .flatMap(TripRequestStatus.PublicStatusCode::findByCode)
                       .map(TripRequestStatus.PublicStatusCode::getDescription)
                       .orElse(null);
    }
    
    @Mapping(target = "expected", expression = "java(requestToExpectedDataDTO(data))")
    @Mapping(source = "rideId", target = "sharedRideId")
    @Mapping(target = "statusCodeDescription", expression = "java(getStatusCodeDescription(data))")
    GetCarsharingRequestDTO requestToGetCarsharingDTO(RequestForCarsharing data);
    
    @Mapping(target = "expected", expression = "java(requestToExpectedDataDTO(data))")
    GetGroupTransferRequestDTO requestToGetGroupTransferDTO(RequestForGroupTransfer data);
    
    default String getStatusCodeDescription(RequestForCarsharing source) {
        return Optional.ofNullable(source)
                       .map(Request::getStatusCode)
                       .flatMap(TripRequestStatus.CarsharingStatusCode::findByCode)
                       .map(TripRequestStatus.CarsharingStatusCode::getDescription)
                       .orElse(null);
    }
    
    CarsharingTripDTO carsharingTripToCarsharingTripDTO(CarsharingTrip carsharingTrip);
    
    @Mapping(source = "expected.waypoints", target = "waypoints")
    @Mapping(source = "expected.segments", target = "segmentsJSON")
    @Mapping(target = "resolution", ignore = true)
    RequestForTaxi newDTOToRequestForTaxi(NewRequestDTO dto);
    
    
    @Mapping(source = "expected.waypoints", target = "waypoints")
    @Mapping(source = "expected.segments", target = "segmentsJSON")
    RequestForPersonal dtoToRequestForPersonal(RequestDTO dto);
    
    
    @Mapping(source = "expected.waypoints", target = "waypoints")
    @Mapping(source = "expected.segments", target = "segmentsJSON")
    RequestForPersonal newDTOToRequestForPersonal(NewRequestDTO dto);
    
    
    @Mapping(source = "expected.waypoints", target = "waypoints")
    @Mapping(source = "expected.segments", target = "segmentsJSON")
    RequestForPublic newDTOToRequestForPublic(NewRequestDTO dto);
    
    
    @Mapping(expression = "java(toInitiatorFIO(source.getInitiator(), employeeMap))", target = "initiator")
    RequestHistoryElementDTO requestHistoryElementToDTO(RequestHistoryElement source, Map<UUID, Employee> employeeMap);
    
    default String toInitiatorFIO(UUID initiatorId, Map<UUID, Employee> employeeMap) {
        return Optional.ofNullable(employeeMap.get(initiatorId))
                       .map(e -> e.getLastName() + " " + e.getFirstName().charAt(0) + "." +
                                 (e.getPatronymic() != null ? e.getPatronymic().charAt(0) + "." : "")).orElse("");
    }
    
    @Mapping(target = "label", source = "purpose")
    TripPurposeDTO tripPurposeToDTO(TripPurpose data);
    
    @Mapping(target = "purpose", source = "label")
    TripPurpose dtoToTripPurpose(TripPurposeDTO data);
    
    RequestRating dtoToRequestRating(RequestRatingDTO data);
    
    RequestRatingDTO requestRatingToDTO(RequestRating data);
    
    
    @Mapping(source = "transportType.id", target = "transportTypeId")
    @Mapping(source = "transportType.name", target = "transportType", qualifiedByName = "toUpperCase")
    @Mapping(source = "humanReadableId", target = "humanReadableId")
    @Mapping(source = "data", target = "expected")
    RequestDTO requestToDTO(Request data);
    
    
    @Mapping(source = "transportType.id", target = "transportTypeId")
    @Mapping(source = "transportType.name", target = "transportType", qualifiedByName = "toUpperCase")
    @Mapping(source = "humanReadableId", target = "humanReadableId")
    @Mapping(source = "data", target = "expected")
    @Mapping(source = "resolution", target = "resolution")
    @Mapping(target = "timeZone", source = "timeZone")
    RequestDTO requestForTaxiToDTO(RequestForTaxi data);
    
    @Mapping(source = "transportType.id", target = "transportTypeId")
    @Mapping(source = "transportType.name", target = "transportType", qualifiedByName = "toUpperCase")
    @Mapping(source = "humanReadableId", target = "humanReadableId")
    @Mapping(source = "data", target = "expected")
    @Mapping(source = "personalCarId", target = "personalCarId")
    @Mapping(source = "personalCar", target = "personalCar")
    @Mapping(target = "timeZone", source = "timeZone")
    RequestDTO requestForPersonalToDTO(RequestForPersonal data);
    
    @Mapping(source = "transportType.id", target = "transportTypeId")
    @Mapping(source = "transportType.name", target = "transportType", qualifiedByName = "toUpperCase")
    @Mapping(source = "humanReadableId", target = "humanReadableId")
    @Mapping(source = "data", target = "expected")
    @Mapping(target = "timeZone", source = "timeZone")
    RequestDTO requestToDTO(RequestForPersonal data);
    
    @Mapping(source = "transportType.id", target = "transportTypeId")
    @Mapping(source = "transportType.name", target = "transportType", qualifiedByName = "toUpperCase")
    @Mapping(source = "humanReadableId", target = "humanReadableId")
    @Mapping(source = "data", target = "expected")
    RequestDTO requestToDTO(RequestForPublic data);
    
    @Mapping(source = "supervisorId", target = "supervisorId")
    @Mapping(source = "departmentId", target = "department.id")
    @Mapping(source = "positionId", target = "positionId")
    Employee employeeMessageToEmployeeEntity(EmployeeMessage employeeMessage);
    
    @Mapping(source = "organizationId", target = "organizationId")
    Position positionMessageToPosition(PositionMessage positionMessage);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<RouteSegment> routeSegmentDTOListToRouteSegmentList(List<RouteSegmentDTO> data);
    
    default List<Waypoint> waypointDTOListToWaypointList(List<WaypointDTO> list) {
        return IntStream.range(0, list.size())
                        .mapToObj(index -> dtoToWaypoint(list.get(index), index))
                        .collect(Collectors.toCollection(ArrayList::new));
    }
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<WaypointDTO> waypointListToDTOList(List<Waypoint> list);
    
    //upper case
    @Named("toUpperCase")
    static String toUpperCase(String string) {
        if (string == null) {
            return null;
        }
        return string.toUpperCase(Locale.ROOT);
    }
    
    default SrmRequestDTO requestToSrmRequestDTO(AbstractRequestForTnP source) {
        if (source == null) {
            return null;
        }
        SrmRequestDTO target = new SrmRequestDTO();
        target.setRequestId(source.getId());
        target.setRequiredPassengers(source.getPassengerCount());
        target.setTariffId(source.getTariffId());
        target.setTransportType(source.getTransportType());
        target.setPickupTime(source.getDesiredDate().atZone(ZoneId.of("UTC")));
        target.setDropTime(null);
        target.setTimeZone(source.getTimeZone());
        target.setPointMatchingType(PointMatchingType.MATCH_ALL_POINTS);
        target.setRequestPrice(source.getRequestPrice());
        
        List<SrmWaypointPostDTO> stops = new ArrayList<>();
        for (Waypoint waypoint : source.getWaypoints()) {
            SrmWaypointPostDTO srmWaypoint = new SrmWaypointPostDTO();
            if (waypoint.getWaitTime() != null) {
                srmWaypoint.setWaitingTime((int) waypoint.getWaitTime().toSeconds());
            }
            srmWaypoint.setAddress(waypoint.getAddress().toStringTrimmed());
            srmWaypoint.setLatitude(waypoint.getAddress().getLatitude());
            srmWaypoint.setLongitude(waypoint.getAddress().getLongitude());
            stops.add(srmWaypoint);
        }
        target.setWaypoints(stops);
        return target;
    }
}
