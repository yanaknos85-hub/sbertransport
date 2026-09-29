package ru.sberbank.transport.oto.cargo.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import ru.sberbank.ditsib.transport.request.messaging.CargoRequestMessage;
import ru.sberbank.ditsib.transport.request.messaging.CargoRequestMultiMessage;
import ru.sberbank.transport.oto.cargo.database.model.Employee;
import ru.sberbank.transport.oto.cargo.database.model.Request;
import ru.sberbank.transport.oto.cargo.dto.cargo.OtoEngineerCargoRequestDetailDTO;
import ru.sberbank.transport.oto.cargo.messaging.messages.RequestMessage;

import java.util.UUID;

@Mapper(uses = { ExpectedDataMapper.class, DateTimeMapper.class, WaypointMapper.class, EmployeeMapper.class })
public interface RequestMapper {

    @Mapping(source = "request.id", target = "id")
    @Mapping(source = "request.humanReadableId", target = "humanReadableId")
    @Mapping(source = "request.author.id", target = "authorId")
    @Mapping(source = "request.passenger.id", target = "passengerId")
    @Mapping(source = "request.creationTime", target = "creationTime")
    @Mapping(source = "request.transportType", target = "transportType")
    @Mapping(source = "request.waypoints", target = "waypoints")
    @Mapping(source = "request.approvedBy.id", target = "approvalId")
    @Mapping(source = "request.approvalDate", target = "approvalDate")
    @Mapping(source = "request.tariff", target = "tariffId")
    @Mapping(source = "request.desiredDate", target = "desiredDate")
    @Mapping(source = "request.status", target = "status")
    @Mapping(source = "request.commentForDriver", target = "commentForDriver")
    @Mapping(source = "request.contractor.id", target = "contractorId")
    @Mapping(source = "request.driver.id", target = "driverId")
    @Mapping(target = "tariff", ignore = true)
    @Mapping(target = "deadline", ignore = true)
    @Mapping(target = "resolution", ignore = true)
    @Mapping(target = "timeZone", ignore = true)
    @Mapping(target = "tripConfirmationDate", ignore = true)
    @Mapping(target = "isSlaExpired", ignore = true)
    @Mapping(target = "orderPaymentFormationStartDate", ignore = true)
    @Mapping(target = "orderPaymentFormationFinishingDate", ignore = true)
    @Mapping(target = "isSuburbTrip", ignore = true)
    @Mapping(target = "sharedRideOwner", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "taxiAwaitingSearchStartDate", ignore = true)
    @Mapping(target = "tripFactDuration", ignore = true)
    RequestMessage toMessage(Request request);

    @Mapping(target = "sender", expression = "java(getFullName(request.getSender(), request.getSenderName()))")
    @Mapping(target = "recipient", expression = "java(getFullName(request.getRecipient(), request.getRecipientName()))")
    @Mapping(target = "author", source = "authorName")
    @Mapping(target = "authorPhone", source = "authorPhone")
    @Mapping(target = "routeNumber", source = "request", qualifiedByName = "mapHumanReadableRouteNumber")
    @Mapping(target = "routeId", source = "tripId")
    @Mapping(target = "plannedPrice", source = "expected.cost")
    @Mapping(target = "plannedRange", source = "expected.distance")
    @Mapping(target = "cargoTransportType", source = "transportType")
    @Mapping(target = "commonComment", source = "commentForDriver")
    @Mapping(target = "template", expression = "java(toTemplateString(request.getTemplate()))")
    OtoEngineerCargoRequestDetailDTO toOtoCargoDetails(Request request);

    @Mapping(ignore = true, target = "sender")
    @Mapping(ignore = true, target = "recipient")
    @Mapping(target = "requestType", constant = "SINGLE")
    void update(@MappingTarget Request request, CargoRequestMessage message);

    @Mapping(ignore = true, target = "sender")
    @Mapping(ignore = true, target = "recipient")
    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "waypoints")
    @Mapping(ignore = true, target = "author")
    @Mapping(source = "cost", target = "expected.cost")
    @Mapping(source = "distance", target = "expected.distance")
    void update(@MappingTarget Request request, CargoRequestMultiMessage message);

    @Mapping(target = "tariff", ignore = true)
    @Mapping(target = "waypoints", ignore = true)
    @Mapping(target = "requestType", constant = "SINGLE")
    void update(@MappingTarget Request request, RequestMessage message);

    default String toTemplateString(Boolean template) {
        if (template!=null && template) {
            return "ДА";
        } return "НЕТ";
    }

    default String getFullName(Employee employee, String name) {
        if (employee == null || employee.getFIO() == null) {
            return name;
        }

        return employee.getFIO();
    }

    @Named("mapHumanReadableRouteNumber")
    default String toRouteNumber(Request request) {
        if (request.getCargoTripHumanReadableId() != null) {
            return request.getCargoTripHumanReadableId();
        }
        var tripId = request.getTripId();
        if (tripId != null) {
            return tripId.toString();
        }
        return "-";
    }

}