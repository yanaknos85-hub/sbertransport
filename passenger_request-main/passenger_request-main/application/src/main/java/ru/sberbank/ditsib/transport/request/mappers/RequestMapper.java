package ru.sberbank.ditsib.transport.request.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Lookup;
import ru.sber.transport.magenta.model.EmployeeDTO;
import ru.sber.transport.payout.messaging.message.RequestPayoutMessage;
import ru.sber.transport.request.messaging.OutContractorTaxiTripMessage;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.request.converter.StatusConverter;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.personal.PersonalTariff;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.TransportCompensation;
import ru.sberbank.ditsib.transport.request.dto.GetRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.GroupTransferRequestInformationDTO;
import ru.sberbank.ditsib.transport.request.dto.ShortGetRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.mapper.DriverDTOMapper;
import ru.sberbank.ditsib.transport.request.messaging.message.ChildSeatDetails;
import ru.sberbank.ditsib.transport.request.messaging.message.ReceiptScannerDocumentMessage;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Маппер запросов на поездку .
 */
@Mapper(uses = {
        WaypointsMapper.class,
        ExpectedDataMapper.class,
        DriverDTOMapper.class,
        FraudMapper.class
},
        imports = {
                StatusConverter.class,
                ZoneOffset.class,
                BigDecimal.class
        })
public interface RequestMapper {


    @Mapping(target = "factData", expression = "java(toFactData(request))")
    @Mapping(target = "authorId", source = "request.author.id")
    @Mapping(target = "passengerId", source = "request.passenger.id")
    @Mapping(target = "tripClass", source = "request.taxiClass")
    @Mapping(target = "approvalId", source = "request.approvedBy.id")
    @Mapping(target = "coopTrip", source = "request.coopTrip", defaultValue = "false")
    @Mapping(target = "purposeId", source = "request.purpose.id")
    @Mapping(target = "driverId", source = "request.driver.id")
    @Mapping(target = "driverData.lastName", source = "request.driver.lastName")
    @Mapping(target = "driverData.firstName", source = "request.driver.firstName")
    @Mapping(target = "driverData.patronymic", source = "request.driver.patronymic")
    @Mapping(target = "driverData.phoneNumber", source = "request.driver.contactPhone")
    @Mapping(target = "deleted", source = "deleted")
    @Mapping(target = "resolution", source = "request.resolution")
    @Mapping(target = "deadline", source = "request.driverArrivedDeadline")
    @Mapping(target = "vehicleData.brand", source = "request.taxiTrip.assignedCar.brandName")
    @Mapping(target = "vehicleData.model", source = "request.taxiTrip.assignedCar.model")
    @Mapping(target = "vehicleData.stateNumber", source = "request.taxiTrip.assignedCar.registrationNumber")
    @Mapping(target = "vehicleData.color", source = "request.taxiTrip.assignedCar.color")
    @Mapping(target = "passenger.departmentId", source = "request.passenger.department.id")
    @Mapping(target = "driverWaitingTime", source = "request.factWaitingTime")
    @Mapping(target = "economyData.costSharePart", source = "request.costSharePart")
    @Mapping(target = "economyData.savingsCash", source = "request.savingsCash")
    @Mapping(target = "economyData.savingsProcents", source = "request.savingsProcents")
    @Mapping(target = "tariff", expression = "java(baseTariffToMap(request.getTariff()))")
    @Mapping(target = "outcomeTariff", expression = "java(baseTariffToMap(request.getOutcomeTariff()))")
    @Mapping(target = "requestClosedDatetime", source = "request.requestClosedDatetime")
    @Mapping(target = "taxiTripHrId", source = "request.taxiTrip.humanReadableId")
    @Mapping(target = "minTaxiTariffCost", source = "request.minTariffTaxi.cost")
    @Mapping(target = "fraudData", source = "request.fraudData")
    @Mapping(target = "metricsStatus", expression = "java(StatusConverter.tripRequestStatusToMetricsStatus(request.getStatus()))")
    RequestMessage toMessage(RequestForTaxi request, boolean deleted);

    ShortGetRequestDTO toShortDTO(GetRequestDTO requestDTO);

    default Instant toInstant(LocalDateTime localDateTime) {
        return Optional.ofNullable(localDateTime)
                .map(l -> l.toInstant(ZoneOffset.UTC))
                .orElse(null);
    }

    @Mapping(target = "authorId", source = "request.author.id")
    @Mapping(target = "passengerId", source = "request.passenger.id")
    @Mapping(target = "waypoints", source = "request.waypoints")
    @Mapping(target = "approvalId", source = "request.approvedBy.id")
    @Mapping(target = "coopTrip", source = "request.coopTrip", defaultValue = "false")
    @Mapping(target = "purposeId", source = "request.purpose.id")
    @Mapping(target = "deleted", source = "deleted")
    @Mapping(target = "sharedRideOwner", source = "request.sharedRideOwner")
    @Mapping(target = "orderPaymentFormationFinishingDate", source = "request.orderPaymentFormationFinishingDate")
    @Mapping(target = "orderPaymentFormationStartDate", source = "request.orderPaymentFormationStartDate")
    @Mapping(target = "isSlaExpired", source = "request.slaExpired")
    @Mapping(target = "passenger.departmentId", source = "request.passenger.department.id")
    @Mapping(target = "vehicleData", ignore = true)
    @Mapping(target = "economyData.costSharePart", source = "request.costSharePart")
    @Mapping(target = "economyData.savingsCash", source = "request.savingsCash")
    @Mapping(target = "economyData.savingsProcents", source = "request.savingsProcents")
    @Mapping(target = "passenger.mvz", source = "request.passenger.costCenter")
    @Mapping(target = "tariff", expression = "java(baseTariffToMap(request.getTariff()))")
    @Mapping(target = "outcomeTariff", expression = "java(baseTariffToMap(request.getOutcomeTariff()))")
    @Mapping(target = "minTaxiTariffCost", source = "request.minTariffTaxi.cost")
    @Mapping(target = "deadline", source = "request.paymentDoneDeadline")
    @Mapping(target = "deadlineState", source = "request.paymentDoneDeadlineState")
    @Mapping(target = "tripStartTime", source = "request.tripStartTime")
    @Mapping(target = "fraudData", source = "request.fraudData")
    @Mapping(target = "metricsStatus", expression = "java(StatusConverter.tripRequestStatusToMetricsStatus(request.getStatus()))")
    RequestMessage toMessage(RequestForPersonal request, boolean deleted);

    @Mapping(target = "authorId", source = "request.author.id")
    @Mapping(target = "passengerId", source = "request.passenger.id")
    @Mapping(target = "waypoints", source = "request.waypoints")
    @Mapping(target = "transportCompensation", source = "request.transportCompensation")
    @Mapping(target = "approvalId", source = "request.approvedBy.id")
    @Mapping(target = "purposeId", source = "request.purpose.id")
    @Mapping(target = "deleted", source = "deleted")
    @Mapping(target = "publicCompensationDocumentExist", expression = "java(isPublicCompensationDocumentExist(request))")
    @Mapping(target = "isSlaExpired", source = "request.slaExpired")
    @Mapping(target = "orderPaymentFormationFinishingDate", source = "request.orderPaymentFormationFinishingDate")
    @Mapping(target = "orderPaymentFormationStartDate", source = "request.orderPaymentFormationStartDate")
    @Mapping(target = "passenger.departmentId", source = "request.passenger.department.id")
    @Mapping(target = "vehicleData", ignore = true)
    @Mapping(target = "tariff", expression = "java(baseTariffToMap(request.getTariff()))")
    @Mapping(target = "outcomeTariff", expression = "java(baseTariffToMap(request.getOutcomeTariff()))")
    @Mapping(target = "minTaxiTariffCost", source = "request.minTariffTaxi.cost")
    @Mapping(target = "deadlineState", source = "request.paymentDoneDeadlineState")
    @Mapping(target = "deadline", source = "request.paymentDoneDeadline")
    @Mapping(target = "fraudData", source = "request.fraudData")
    @Mapping(target = "metricsStatus", expression = "java(StatusConverter.tripRequestStatusToMetricsStatus(request.getStatus()))")
    RequestMessage toMessage(RequestForPublic request, boolean deleted);

    @Mapping(target = "authorId", source = "request.author.id")
    @Mapping(target = "passengerId", source = "request.passenger.id")
    @Mapping(target = "waypoints", source = "request.waypoints")
    @Mapping(target = "approvalId", source = "request.approvedBy.id")
    @Mapping(target = "coopTrip", source = "request.coopTrip", defaultValue = "false")
    @Mapping(target = "purposeId", source = "request.purpose.id")
    @Mapping(target = "carsharingClass", source = "request.carsharingClass")
    @Mapping(target = "deleted", source = "deleted")
    @Mapping(target = "passenger.departmentId", source = "request.passenger.department.id")
    @Mapping(target = "vehicleData", ignore = true)
    @Mapping(target = "economyData.costSharePart", source = "request.costSharePart")
    @Mapping(target = "economyData.savingsCash", source = "request.savingsCash")
    @Mapping(target = "economyData.savingsProcents", source = "request.savingsProcents")
    @Mapping(target = "rentId", source = "rentId")
    @Mapping(target = "tariff", expression = "java(baseTariffToMap(request.getTariff()))")
    @Mapping(target = "outcomeTariff", expression = "java(baseTariffToMap(request.getOutcomeTariff()))")
    @Mapping(target = "fraudData", source = "request.fraudData")
    @Mapping(target = "metricsStatus", expression = "java(StatusConverter.tripRequestStatusToMetricsStatus(request.getStatus()))")
    @Mapping(target = "deadlineState", constant = "NONE")
    RequestMessage toMessage(RequestForCarsharing request, Integer rentId, boolean deleted);

    @Mapping(target = "groupTransferClass", source = "request.groupTransferClass")
    @Mapping(target = "authorId", source = "request.author.id")
    @Mapping(target = "passengerId", source = "request.passenger.id")
    @Mapping(target = "approvalId", source = "request.approvedBy.id")
    @Mapping(target = "purposeId", source = "request.purpose.id")
    @Mapping(target = "driverId", source = "request.driver.id")
    @Mapping(target = "driverData.lastName", source = "request.driver.lastName")
    @Mapping(target = "driverData.firstName", source = "request.driver.firstName")
    @Mapping(target = "driverData.patronymic", source = "request.driver.patronymic")
    @Mapping(target = "driverData.phoneNumber", source = "request.driver.contactPhone")
    @Mapping(target = "deleted", source = "deleted")
    @Mapping(target = "resolution", source = "request.resolution")
    @Mapping(target = "deadline", source = "request.driverArrivedDeadline")
    @Mapping(target = "vehicleData.brand", source = "request.trip.assignedCar.brandName")
    @Mapping(target = "vehicleData.model", source = "request.trip.assignedCar.model")
    @Mapping(target = "vehicleData.stateNumber", source = "request.trip.assignedCar.registrationNumber")
    @Mapping(target = "vehicleData.color", source = "request.trip.assignedCar.color")
    @Mapping(target = "passenger.departmentId", source = "request.passenger.department.id")
    @Mapping(target = "driverWaitingTime", source = "request.factWaitingTime")
    @Mapping(target = "tariff", expression = "java(baseTariffToMap(request.getTariff()))")
    @Mapping(target = "outcomeTariff", expression = "java(baseTariffToMap(request.getOutcomeTariff()))")
    @Mapping(target = "requestClosedDatetime", source = "request.requestClosedDatetime")
    @Mapping(target = "information", expression = "java(newRequestInformationDTOToNewRequestInformationDTO(request.getInformation()))")
    @Mapping(target = "minTaxiTariffCost", source = "request.minTariffTaxi.cost")
    @Mapping(target = "fraudData", source = "request.fraudData")
    @Mapping(target = "metricsStatus", expression = "java(StatusConverter.tripRequestStatusToMetricsStatus(request.getStatus()))")
    RequestMessage toMessage(RequestForGroupTransfer request, boolean deleted);

    @Mapping(target = "id", source = "source.id")
    @Mapping(target = "humanReadableId", source = "source.humanReadableId")
    @Mapping(target = "costCenter", source = "source.passenger.costCenter")
    @Mapping(target = "resource", constant = "29015")
    @Mapping(target = "organizationId", source = "source.organizationId")
    @Mapping(target = "actualCost", expression = "java(BigDecimal.valueOf(source.getExpected().getCost().longValue(), 2))")
    @Mapping(target = "employeeId", source = "source.passenger.id")
    @Mapping(target = "changeDate", expression = "java(changeAt.atOffset(ZoneOffset.UTC))")
    @Mapping(target = "transportType", constant = "PERSONAL")
    @Mapping(target = "employeeDriverId", source = "source.employeeDriverId")
    @Mapping(target = "expectedDistance", expression = "java(source.getExpected() != null && source.getExpected().getDistance() != null ? BigDecimal.valueOf(source.getExpected().getDistance()) : null)")
    @Mapping(target = "personalCarId", source = "source.personalCarId")
    @Mapping(target = "tariffId", source = "source.tariffId")
    @Mapping(target = "additionalSum", source = "source.additionalSum")
    @Mapping(target = "ownerInfo", expression = "java(source.getPersonalCar() != null ? source.getPersonalCar().getOwnerInfo() : null)")
    @Mapping(target = "engineVolume", expression = "java(source.getPersonalCar() != null ? source.getPersonalCar().getEngineVolume() : null)")
    @Mapping(target = "trustIdx", expression = "java(getTrustIdx(source.getTariff()))")
    @Mapping(target = "departmentId", source = "source.passenger.department.id")
    @Mapping(target = "orderPaymentFormationStartDate", expression = "java(source.getOrderPaymentFormationStartDate() != null ? java.time.LocalDate.from(source.getOrderPaymentFormationStartDate().atZone(java.time.ZoneOffset.UTC)) : null)")
    RequestPayoutMessage requestToRequestPayoutMessage(RequestForPersonal source, LocalDateTime changeAt);

    @Mapping(target = "id", source = "source.id")
    @Mapping(target = "humanReadableId", source = "source.humanReadableId")
    @Mapping(target = "costCenter", source = "source.passenger.costCenter")
    @Mapping(target = "resource", constant = "26511")
    @Mapping(target = "organizationId", source = "source.organizationId")
    @Mapping(target = "actualCost", expression = "java(BigDecimal.valueOf(source.getExpected().getCost().longValue(), 2))")
    @Mapping(target = "employeeId", source = "source.passenger.id")
    @Mapping(target = "changeDate", expression = "java(changeAt.atOffset(ZoneOffset.UTC))")
    @Mapping(target = "transportType", constant = "PUBLIC")
    @Mapping(target = "ticketsCost", expression = "java(aggregateTicketsCost(source))")
    @Mapping(target = "ticketsCount", expression = "java(aggregateTicketsCount(source))")
    @Mapping(target = "compensationType", expression = "java(firstCompensationType(source))")
    @Mapping(target = "orderPaymentFormationStartDate", expression = "java(source.getOrderPaymentFormationStartDate() != null ? java.time.LocalDate.from(source.getOrderPaymentFormationStartDate().atZone(java.time.ZoneOffset.UTC)) : null)")
    RequestPayoutMessage requestToRequestPayoutMessage(RequestForPublic source, LocalDateTime changeAt);

    //Вынес в отдельную функцию, тк есть проблемы с полем addContact при генерации через lombok
    default RequestMessage.NewRequestInformationDTO newRequestInformationDTOToNewRequestInformationDTO(
            GroupTransferRequestInformationDTO groupTransferRequestInformationDTO
    ) {
        if (groupTransferRequestInformationDTO == null) {
            return null;
        }

        RequestMessage.NewRequestInformationDTO.NewRequestInformationDTOBuilder
                newRequestInformationDTO1 = RequestMessage.NewRequestInformationDTO.builder();

        newRequestInformationDTO1.childSeat(groupTransferRequestInformationDTO.isChildSeat());
        newRequestInformationDTO1.childSeatDetails(childSeatDetailsToChildSeatDetails(groupTransferRequestInformationDTO.getChildSeatDetails()));
        newRequestInformationDTO1.bugs(groupTransferRequestInformationDTO.isBugs());
        newRequestInformationDTO1.bugsComment(groupTransferRequestInformationDTO.getBugsComment());
        newRequestInformationDTO1.bugsOversized(groupTransferRequestInformationDTO.isBugsOversized());
        newRequestInformationDTO1.bugsOversizedComment(groupTransferRequestInformationDTO.getBugsOversizedComment());
        newRequestInformationDTO1.animal(groupTransferRequestInformationDTO.isAnimal());
        newRequestInformationDTO1.animalComment(groupTransferRequestInformationDTO.getAnimalComment());
        newRequestInformationDTO1.addContact(groupTransferRequestInformationDTO.getAddContact());
        newRequestInformationDTO1.numberFlight(groupTransferRequestInformationDTO.getNumberFlight());
        newRequestInformationDTO1.dateFlight(groupTransferRequestInformationDTO.getDateFlight());
        newRequestInformationDTO1.phoneHotel(groupTransferRequestInformationDTO.getPhoneHotel());
        newRequestInformationDTO1.typeVehicle(groupTransferRequestInformationDTO.getTypeVehicle());

        String addContactFIO = groupTransferRequestInformationDTO.getAddContactFIO();
        String addContactPhone = groupTransferRequestInformationDTO.getAddContactPhone();

        newRequestInformationDTO1.addContactFIO(addContactFIO);
        newRequestInformationDTO1.addContactPhone(addContactPhone);
        newRequestInformationDTO1.addContact(addContactFIO + " " + addContactPhone);

        return newRequestInformationDTO1.build();
    }

    default ru.sber.transport.request.messaging.ChildSeatDetails childSeatDetailsToChildSeatDetails(
            ChildSeatDetails childSeatDetails
    ) {
        if (childSeatDetails == null) {
            return null;
        } else {
            return new ru.sber.transport.request.messaging.ChildSeatDetails(childSeatDetails.group1(),
                    childSeatDetails.group2(),
                    childSeatDetails.booster(),
                    childSeatDetails.newborn());
        }
    }

    //Вынес в отдельную функцию, тк есть проблемы с полем addContact при генерации через lombok
    default OutContractorTaxiTripMessage.GroupTransferRequestInformation newRequestInformationDTOToGroupTransferRequestInformation(
            GroupTransferRequestInformationDTO groupTransferRequestInformationDTO) {
        if (groupTransferRequestInformationDTO == null) {
            return null;
        } else {
            return new OutContractorTaxiTripMessage.GroupTransferRequestInformation(
                    groupTransferRequestInformationDTO.isChildSeat(),
                    childSeatDetailsToChildSeatDetails(groupTransferRequestInformationDTO.getChildSeatDetails()),
                    groupTransferRequestInformationDTO.isBugs(),
                    groupTransferRequestInformationDTO.getBugsComment(),
                    groupTransferRequestInformationDTO.isBugsOversized(),
                    groupTransferRequestInformationDTO.getBugsOversizedComment(),
                    groupTransferRequestInformationDTO.isAnimal(),
                    groupTransferRequestInformationDTO.getAnimalComment(),
                    groupTransferRequestInformationDTO.getAddContactFIO(),
                    groupTransferRequestInformationDTO.getAddContactPhone(),
                    groupTransferRequestInformationDTO.getNumberFlight(),
                    groupTransferRequestInformationDTO.getDateFlight(),
                    groupTransferRequestInformationDTO.getPhoneHotel(),
                    groupTransferRequestInformationDTO.getTypeVehicle(),
                    groupTransferRequestInformationDTO.getTransportId()

            );
        }
    }

    default boolean isPublicCompensationDocumentExist(RequestForPublic request) {
        return !Optional.ofNullable(request.getCompensationDocuments()).orElseGet(Collections::emptyList).isEmpty();
    }

    EmployeeDTO employeeToDto(Employee employee);

    default Map<String, Object> baseTariffToMap(BaseTariff tariff) {
        if (tariff != null) {
            return objectMapper().convertValue(tariff, new TypeReference<>() {
            });
        } else {
            return Collections.emptyMap();
        }
    }

    default Integer aggregateTicketsCost(RequestForPublic source) {
        if (source.getTransportCompensation() == null || source.getTransportCompensation().isEmpty()) {
            return null;
        }
        return source.getTransportCompensation().stream()
                .map(tc -> tc.getTicketsCost())
                .reduce(0, Integer::sum);
    }

    default Integer aggregateTicketsCount(RequestForPublic source) {
        if (source.getTransportCompensation() == null || source.getTransportCompensation().isEmpty()) {
            return null;
        }
        return source.getTransportCompensation().stream()
                .map(tc -> tc.getTicketsCount())
                .reduce(0, Integer::sum);
    }

    default String firstCompensationType(RequestForPublic source) {
        if (source.getTransportCompensation() == null || source.getTransportCompensation().isEmpty()) {
            return null;
        }
        return source.getTransportCompensation().get(0).getCompensationType().name();
    }

    default Double getTrustIdx(BaseTariff tariff) {
        if (tariff == null || !(tariff instanceof PersonalTariff)) {
            return null;
        }
        return ((PersonalTariff) tariff).getTrustIdx();
    }

    default RequestMessage.FactData toFactData(RequestForTaxi request) {
        var taxiTrip = request.getTaxiTrip();
        if (taxiTrip == null) {
            return null;
        }

        return new RequestMessage.FactData(
                taxiTrip.getTripFactPrice(),
                taxiTrip.getTripFactDistance(),
                request.getFactWaitingTime() == null ? null : request.getFactWaitingTime().toMillis(),
                taxiTrip.getTripStartTime(),
                taxiTrip.getTripFinishTime(),
                request.getDriverArrivedDatetime()
        );
    }

    @Lookup
    default ObjectMapper objectMapper() {
        return null;
    }

    /**
     * Маппинг заявки на сообщение для сканера чеков.
     * Объединяет документы с компенсациями по attachedDocumentId.
     */
    default ReceiptScannerDocumentMessage toReceiptScannerMessage(RequestForPublic request) {
        var ticketCosts = request.getTransportCompensation().stream()
                .filter(tc -> tc.getAttachedDocumentId() != null)
                .collect(Collectors.toMap(
                        TransportCompensation::getAttachedDocumentId,
                        TransportCompensation::getTicketsCost
                ));
        var totalTicketsCost = ticketCosts.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        var documents = request.getCompensationDocuments().stream()
                .map(doc -> ReceiptScannerDocumentMessage.FileData.builder()
                        .folderId(doc.getFolder())
                        .fileName(doc.getFileName())
                        .ticketCost(ticketCosts.get(doc.getId()))
                        .build())
                .toList();

        return ReceiptScannerDocumentMessage.builder()
                .requestId(request.getId())
                .transportType(request.getTransportType().getName())
                .desiredDate(request.getDesiredDate())
                .cost(totalTicketsCost)
                .files(documents)
                .build();
    }
}
