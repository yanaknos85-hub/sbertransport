package ru.sber.transport.request.external.messaging.mapper;

import java.time.OffsetDateTime;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.payout.messaging.message.RequestPayoutMessage;
import ru.sber.transport.request.external.messaging.message.RequestMessage;
import ru.sber.transport.request.external.model.EditTripOrderData;
import ru.sber.transport.request.external.model.TripOrderData;

@Mapper(uses = WaypointMapper.class)
public interface RequestMapper {

    @Mapping(target = "passengerId", source = "source.passenger.id")
    @Mapping(target = "planned.cost", source = "source.planned.cost")
    @Mapping(target = "planned.duration", source = "source.planned.duration")
    @Mapping(target = "planned.distance", source = "source.planned.distance")
    @Mapping(target = "actual.cost", source = "source.actual.cost")
    @Mapping(target = "organizationId", source = "source.passenger.organizationId")
    @Mapping(target = "departmentId", source = "source.passenger.departmentId")
    @Mapping(target = "approverId", source = "source.approver.id")
    @Mapping(target = "approvalDate", expression = "java(source.getApprovalDate() == null ? null : source.getApprovalDate().toInstant())")
    @Mapping(target = "date", expression = "java(source.getDate().toInstant())")
    @Mapping(target = "assessments", ignore = true)
    ru.sber.transport.messages.request.external.avro.RequestMessage toAvroRequestMessage(TripOrderData source);

    @Mapping(target = "passengerId", source = "source.passenger.id")
    @Mapping(target = "planned.cost", source = "source.planned.cost")
    @Mapping(target = "planned.duration", source = "source.planned.duration")
    @Mapping(target = "planned.distance", source = "source.planned.distance")
    @Mapping(target = "actual.cost", source = "source.actual.cost")
    @Mapping(target = "organizationId", source = "source.passenger.organizationId")
    @Mapping(target = "departmentId", source = "source.passenger.departmentId")
    @Mapping(target = "approverId", source = "source.approver.id")
    @Mapping(target = "approvalDate", expression = "java(source.getApprovalDate() == null ? null : source.getApprovalDate().toInstant())")
    @Mapping(target = "date", expression = "java(source.getDate().toInstant())")
    @Mapping(target = "assessments", source = "assessments")
    RequestMessage toRequestMessage(TripOrderData source, List<RequestMessage.Assessment> assessments);

    @Mapping(target = "id", source = "edited.id")
    @Mapping(target = "humanReadableId", source = "edited.humanReadableId")
    @Mapping(target = "costCenter", constant = "4661")
    @Mapping(target = "resource", constant = "26511")
    @Mapping(target = "organizationId", source = "edited.passenger.organizationId")
    @Mapping(target = "departmentId", source = "edited.passenger.departmentId")
    @Mapping(target = "actualCost", source = "newData.actual.cost")
    @Mapping(target = "employeeId", source = "edited.passenger.id")
    @Mapping(target = "changeDate", source = "modifiedAt")
    @Mapping(target = "transportType", constant = "YANDEX_TAXI")
    @Mapping(target = "employeeDriverId", ignore = true)
    @Mapping(target = "expectedDistance", ignore = true)
    @Mapping(target = "personalCarId", ignore = true)
    @Mapping(target = "tariffId", ignore = true)
    @Mapping(target = "additionalSum", ignore = true)
    @Mapping(target = "ownerInfo", ignore = true)
    @Mapping(target = "engineVolume", ignore = true)
    @Mapping(target = "trustIdx", ignore = true)
    @Mapping(target = "ticketsCost", ignore = true)
    @Mapping(target = "ticketsCount", ignore = true)
    @Mapping(target = "compensationType", ignore = true)
    @Mapping(target = "orderPaymentFormationStartDate", ignore = true)
    RequestPayoutMessage toRequestPayoutMessage(EditTripOrderData newData, TripOrderData edited, OffsetDateTime modifiedAt);
}
