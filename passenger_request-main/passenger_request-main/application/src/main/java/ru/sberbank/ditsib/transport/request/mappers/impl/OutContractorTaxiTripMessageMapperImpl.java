package ru.sberbank.ditsib.transport.request.mappers.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.sber.transport.request.messaging.*;
import ru.sber.transport.srm.model.SrmRequestKpiDTO;
import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sber.transport.srm.model.SrmWaypointFinalDTO;
import ru.sber.transport.srm.model.SrmWaypointGetDTO;
import ru.sberbank.ditsib.transport.constants.TaxiExternalIntegrationType;
import ru.sberbank.ditsib.transport.constants.TaxiStopType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.constants.external.taxi.OutboundRequestStatus;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.Waypoint;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.Contractor;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.taxi.TaxiTariff;
import ru.sberbank.ditsib.transport.request.dto.GroupTransferRequestInformationDTO;
import ru.sberbank.ditsib.transport.request.mappers.OutContractorTaxiTripMessageMapper;

import java.time.Duration;
import java.util.*;
import java.util.stream.IntStream;

import static java.time.temporal.ChronoUnit.MINUTES;
import static ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus.ORDER_CANCELLED_BY_CLIENT;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OutContractorTaxiTripMessageMapperImpl implements OutContractorTaxiTripMessageMapper {

    @Override
    public OutContractorTaxiTripMessage transformSingleTaxiTripToContractorMessage(SingleTaxiTrip trip,
                                                                                   RequestForTaxi request,
                                                                                   TaxiTariff taxiTariff,
                                                                                   @NotNull Contractor contractor,
                                                                                   String organizationName,
                                                                                   Map<UUID, Employee> joinedPassengers) {
        var passengers = createPassengerMessagesList(Collections.singletonList(request));
        var startPoint = request.getWaypoints().isEmpty() ? new Waypoint() : request.getWaypoints().getFirst();
        var endPoint = request.getWaypoints().isEmpty() ? new Waypoint() : request.getWaypoints().getLast();
        var passenger = request.getPassenger();
        return new OutContractorTaxiTripMessage(
                String.valueOf(trip.getId()),
                trip.getHumanReadableId(),
                explainStatus(request, trip),
                request.getTimeZone(),
                Optional.ofNullable(taxiTariff).map(TaxiTariff::getWorkGroup).orElse(null),
                organizationName,
                Optional.of(contractor).map(Contractor::getIntegrationType).orElse(
                        TaxiExternalIntegrationType.JSON_API_1_0),
                getIntegrationParamsFromContractorId(contractor),
                null,
                getCommentForDriver(request, joinedPassengers),
                trip.getTaxiId(),
                false,
                request.getTaxiClass(),
                null,
                new RequestInformationMessage(
                        passengers.size(),
                        request.getDesiredDate(),
                        addressToString(startPoint.getAddress()),
                        addressToString(endPoint.getAddress()),
                        passengers,
                        passengers.getFirst(),
                        waypointsToContractorWaypointMessages(request.getWaypoints())
                ),
                String.valueOf(contractor.getId()),
                Optional.ofNullable(taxiTariff)
                        .map(TaxiTariff::getContractorTariffId)
                        .orElse(null),
                new Source(startPoint.getAddress().toStringTrimmed(),
                        startPoint.getAddress().getLatitude(),
                        startPoint.getAddress().getLongitude()),
                new Destination(endPoint.getAddress().toStringTrimmed(),
                        endPoint.getAddress().getLatitude(),
                        endPoint.getAddress().getLongitude(),
                        new Contact(passenger.getMobilePhone(),
                                passenger.getIO(),
                                passenger.getFirstName(),
                                passenger.getPatronymic(),
                                null)),
                transformToWaypointsSingle(request.getWaypoints(), request, joinedPassengers),
                TransportTypeEnum.TAXI,
                null,
                request.getExpected().getTime().toSeconds(),
                request.getExpected().getDistance(),
                null
        );
    }

    @Override
    public OutContractorTaxiTripMessage transformGroupTransferTripToContractorMessage(GroupTransferTrip trip,
                                                                                      RequestForGroupTransfer request,
                                                                                      GroupTransferTariff tariff,
                                                                                      @NotNull Contractor contractor,
                                                                                      String organizationName,
                                                                                      Map<UUID, Employee> joinedPassengers) {
        var passengers = createPassengerMessagesList(Collections.singletonList(request));
        var startPoint = request.getWaypoints().isEmpty() ? new Waypoint() : request.getWaypoints().getFirst();
        var endPoint = request.getWaypoints().isEmpty() ? new Waypoint() : request.getWaypoints().getLast();
        var passenger = request.getPassenger();
        var contact = new Contact(passenger.getMobilePhone(),
                passenger.getIO(),
                passenger.getFirstName(),
                passenger.getPatronymic(),
                null);
        return new OutContractorTaxiTripMessage(
                trip.getId().toString(),
                trip.getHumanReadableId(),
                explainStatus(request, trip),
                request.getTimeZone(),
                null,
                organizationName,
                Optional.of(contractor).map(Contractor::getIntegrationType).orElse(
                        TaxiExternalIntegrationType.JSON_API_1_0),
                getIntegrationParamsFromContractorId(contractor),
                null,
                request.getCommentForDriver(),
                trip.getGroupTransferId(),
                false,
                null,
                request.getGroupTransferClass(),
                new RequestInformationMessage(
                        request.getPassengerCount(),
                        request.getDesiredDate(),
                        addressToString(request.getWaypoints().getFirst().getAddress()),
                        addressToString(request.getWaypoints().getLast().getAddress()),
                        passengers,
                        passengers.getFirst(),
                        waypointsToContractorWaypointMessages(request.getWaypoints())
                ),
                contractor.getId().toString(),
                Optional.ofNullable(tariff).map(GroupTransferTariff::getContractorTariffId).orElse(null),
                new Source(startPoint.getAddress().toStringTrimmed(), startPoint.getAddress().getLatitude(),
                        startPoint.getAddress().getLongitude()),
                new Destination(endPoint.getAddress().toStringTrimmed(), endPoint.getAddress().getLatitude(),
                        endPoint.getAddress().getLongitude(), contact),
                transformToWaypointsSingle(request.getWaypoints(), request, joinedPassengers),
                TransportTypeEnum.GROUP_TRANSFER,
                Optional.ofNullable(request.getInformation()).map(GroupTransferRequestInformationDTO::getTransportId).orElse(null),
                request.getExpected().getTime().toSeconds(),
                request.getExpected().getDistance(),
                newRequestInformationDTOToGroupTransferRequestInformation(request.getInformation())
        );
    }

    @Override
    public OutContractorTaxiTripMessage transformCoopTaxiTripToContractorMessage(CoopTaxiTrip trip,
                                                                                 RequestForTaxi firstActiveRequest,
                                                                                 List<RequestForTaxi> activeRequests,
                                                                                 TaxiTariff tariff,
                                                                                 @NotNull Contractor contractor,
                                                                                 String organizationName,
                                                                                 Map<UUID, Employee> joinedPassengers,
                                                                                 SrmSharedRideDTO sharedRideDTO) {
        var passengers = createPassengerMessagesList(activeRequests);
        var optionalTariff = Optional.ofNullable(tariff);
        var status = explainStatus(trip, passengers);
        var startPoint = sharedRideDTO.getWaypoints().getFirst();
        var endPoint = sharedRideDTO.getWaypoints().getLast();
        var passenger = activeRequests.stream()
                .filter(requestForTaxi -> requestForTaxi.getId()
                        .equals(sharedRideDTO.getRequestKpiList().getFirst().getId()))
                .findAny()
                .orElseThrow()
                .getPassenger();
        var contact = new Contact(passenger.getMobilePhone(),
                passenger.getIO(),
                passenger.getFirstName(),
                passenger.getPatronymic(),
                null);
        return new OutContractorTaxiTripMessage(
                trip.getId().toString(),
                trip.getHumanReadableId(),
                status,
                sharedRideDTO.getTimeZone(),
                optionalTariff.map(TaxiTariff::getWorkGroup).orElse(null),
                organizationName,
                Optional.of(contractor).map(Contractor::getIntegrationType).orElse(
                        TaxiExternalIntegrationType.JSON_API_1_0),
                getIntegrationParamsFromContractorId(contractor),
                null,
                createCommentForDriverCoopTrip(activeRequests, joinedPassengers),
                trip.getTaxiId(),
                true,
                firstActiveRequest.getTaxiClass(),
                null,
                new RequestInformationMessage(
                        sharedRideDTO.getRequestKpiList().stream()
                                .mapToInt(SrmRequestKpiDTO::getRequiredPassengers)
                                .sum(),
                        sharedRideDTO.getWaypoints().getFirst().getStartTime().toLocalDateTime(),
                        sharedRideDTO.getWaypoints().getFirst().getAddress(),
                        sharedRideDTO.getWaypoints().getLast().getAddress(),
                        passengers,
                        passengers.isEmpty()
                                ? new PassengerMessage(null, null, null, null)
                                : passengers.getFirst(),
                        contractorWaypointsMessageFromSharedRequest(sharedRideDTO, activeRequests)
                ),
                contractor.getId().toString(),
                optionalTariff.map(TaxiTariff::getContractorTariffId).orElse(null),
                new Source(startPoint.getAddress(), startPoint.getLatitude(), startPoint.getLongitude()),
                new Destination(endPoint.getAddress(), endPoint.getLatitude(), endPoint.getLongitude(), contact),
                transformToWaypointsCoop(sharedRideDTO, activeRequests, joinedPassengers),
                TransportTypeEnum.TAXI,
                null,
                firstActiveRequest.getExpected().getTime().toSeconds(),
                firstActiveRequest.getExpected().getDistance(),
                null
        );
    }

    @Override
    public OutContractorTaxiTripMessage transformRejectedCoopTaxiTripToContractorMessage(CoopTaxiTrip trip,
                                                                                         RequestForTaxi firstActiveRequest,
                                                                                         List<RequestForTaxi> activeRequests,
                                                                                         TaxiTariff tariff,
                                                                                         @NotNull Contractor contractor,
                                                                                         String organizationName,
                                                                                         Map<UUID, Employee> joinedPassengers) {
        var passengers = passengerMessageListFromSharedRequest(activeRequests);
        var optionalTariff = Optional.ofNullable(tariff);
        var status = explainStatus(trip, passengers);

        return new OutContractorTaxiTripMessage(
                trip.getId().toString(),
                trip.getHumanReadableId(),
                status,
                "Etc/GMT-3",
                optionalTariff.map(TaxiTariff::getWorkGroup).orElse(null),
                organizationName,
                Optional.of(contractor).map(Contractor::getIntegrationType).orElse(
                        TaxiExternalIntegrationType.JSON_API_1_0),
                getIntegrationParamsFromContractorId(contractor),
                null,
                createCommentForDriverCoopTrip(activeRequests, joinedPassengers),
                trip.getTaxiId(),
                true,
                firstActiveRequest.getTaxiClass(),
                null,
                null,
                contractor.getId().toString(),
                optionalTariff.map(TaxiTariff::getContractorTariffId).orElse(null),
                null,
                null,
                null,
                TransportTypeEnum.TAXI,
                null,
                firstActiveRequest.getExpected().getTime().toSeconds(),
                firstActiveRequest.getExpected().getDistance(),
                null
        );
    }

    private List<PassengerMessage> createPassengerMessagesList(List<? extends Request> requests) {
        var resultList = new ArrayList<PassengerMessage>();
        var index = 1;
        for (var request : requests) {
            resultList.add(new PassengerMessage(
                    String.valueOf(index),
                    request.getPassenger().getIO(),
                    request.getPassenger().getMobilePhone(),
                    createDriverComment(request)
            ));
            index++;
        }
        return resultList;
    }

    private String createDriverComment(Request request) {
        if (request instanceof AbstractRequestForTnP abstractRequestForTnP) {
            return abstractRequestForTnP.getCommentForDriver();
        } else return "";
    }

    private String createCommentForDriverCoopTrip(List<RequestForTaxi> activeRequests, Map<UUID, Employee> joinedPassengers) {
        return activeRequests
                .stream()
                .map(request -> getCommentForDriver(request, joinedPassengers))
                .reduce("",
                        (partialString, element) -> partialString + element + "\n");
    }

    private List<PassengerMessage> passengerMessageListFromSharedRequest(List<RequestForTaxi> requests) {
        var resultList = new ArrayList<PassengerMessage>();
        var index = 1;
        for (var requestForTaxi : requests) {
            resultList.add(new PassengerMessage(
                    String.valueOf(index),
                    requestForTaxi.getPassenger().getIO(),
                    requestForTaxi.getPassenger().getMobilePhone(),
                    requestForTaxi.getCommentForDriver())
            );
            index++;
        }
        return resultList;
    }

    private List<ru.sber.transport.request.messaging.Waypoint> transformToWaypointsSingle(
            List<Waypoint> waypoints, Request request,
            Map<UUID, Employee> joinedPassengers) {
        return waypoints.stream()
                .map(waypoint -> new ru.sber.transport.request.messaging.Waypoint(
                        waypoint.getAddress().toStringTrimmed(),
                        waypoint.getAddress().getLatitude(),
                        waypoint.getAddress().getLongitude(),
                        waypoint.getWaitTime() != null ? (int) (waypoint.getWaitTime().toSeconds()) : 0,
                        getContacts(request, waypoint.getOrderingIndex() == 0
                                        ? TaxiStopType.BOARDING
                                        : getTaxiStopType(waypoints, waypoint),
                                joinedPassengers)))
                .toList();
    }

    @NotNull
    private static TaxiStopType getTaxiStopType(List<Waypoint> waypoints, Waypoint waypoint) {
        return waypoint.getOrderingIndex() == (waypoints.size() - 1) ? TaxiStopType.UNBOARDING : TaxiStopType.WAIT;
    }

    private List<Contact> getContacts(Request request, TaxiStopType type, Map<UUID, Employee> joinedPassengers) {
        var employee = request.getPassenger();
        var contacts = new ArrayList<Contact>();
        // Добавляем основного пассажира
        contacts.add(new Contact(employee.getMobilePhone(),
                employee.getIO(),
                employee.getFirstName(),
                employee.getPatronymic(),
                type));
        // Если есть, добавляем присоединённых
        if (request.getJoinedPassengerIds() != null && !request.getJoinedPassengerIds().isEmpty()) {
            for (var joinedPassenger : joinedPassengers.values()) {
                contacts.add(
                        new Contact(joinedPassenger.getMobilePhone(),
                                joinedPassenger.getIO(),
                                joinedPassenger.getFirstName(),
                                joinedPassenger.getPatronymic(),
                                type));
            }
        }
        return contacts;
    }

    private String getCommentForDriver(RequestForTaxi request, Map<UUID, Employee> joinedPassengers) {
        var commentForDriver = new StringBuilder(Optional.ofNullable(request.getCommentForDriver()).orElse(""));
        //Кол-во присоединённых пассажиров + создатель заявки
        if (!commentForDriver.isEmpty()) {
            commentForDriver.append("\n");
        }
        commentForDriver.append("В поездке участвует(-ют) %d пассажир(а):".formatted(request.getJoinedPassengerIds().size() + 1));
        var allPassengers = new ArrayList<Employee>();
        allPassengers.add(request.getPassenger());
        allPassengers.addAll(joinedPassengers.values());
        IntStream.range(0, allPassengers.size())
                .forEach(i -> commentForDriver.append("%n%d. %s, %s, %s".formatted(
                        i + 1,
                        allPassengers.get(i).getFirstName(),
                        allPassengers.get(i).getPatronymic(),
                        allPassengers.get(i).getMobilePhone())));
        return commentForDriver.toString();
    }

    private String addressToString(Address address) {
        StringBuilder resultBuilder = new StringBuilder();
        if (StringUtils.hasText(address.getCountry())) {
            resultBuilder.append(address.getCountry());
            resultBuilder.append(", ");
        }
        if (StringUtils.hasText(address.getRegion())) {
            resultBuilder.append(address.getRegion());
            resultBuilder.append(", ");
        }
        if (StringUtils.hasText(address.getCity())) {
            resultBuilder.append(address.getCity());
            resultBuilder.append(", ");
        }
        if (StringUtils.hasText(address.getStreet())) {
            resultBuilder.append(address.getStreet());
            resultBuilder.append(", ");
        }
        if (StringUtils.hasText(address.getHouse())) {
            resultBuilder.append(address.getHouse());
        }
        return resultBuilder.toString();
    }

    private List<OutContractorWaypointMessage> waypointsToContractorWaypointMessages(List<Waypoint> waypoints) {
        var resultList = new ArrayList<OutContractorWaypointMessage>();
        for (int index = 0; index < waypoints.size(); index++) {
            var waypoint = waypoints.get(index);
            resultList.add(new OutContractorWaypointMessage(
                    String.valueOf((index + 1)),
                    getType(waypoints, index),
                    addressToString(waypoint.getAddress()),
                    String.valueOf(waypoint.getAddress().getLatitude()),
                    String.valueOf(waypoint.getAddress().getLongitude()),
                    String.valueOf(1),
                    Optional.ofNullable(waypoint.getWaitTime()).orElse(Duration.ZERO)
            ));
        }
        return resultList;
    }

    private OutboundRequestStatus explainStatus(RequestForTaxi request, TaxiTrip trip) {
        if (TripRequestStatus.TAXI_CANCELLED.equals(request.getStatus())) {
            return OutboundRequestStatus.REJECT;
        }
        if (trip.getTaxiId() == null) {
            return OutboundRequestStatus.NEW;
        }
        return OutboundRequestStatus.IN_PROGRESS;
    }

    private OutboundRequestStatus explainStatus(RequestForGroupTransfer request, GroupTransferTrip trip) {
        if (TripRequestStatus.GROUP_TRANSFER_CANCELLED.equals(request.getStatus())) {
            return OutboundRequestStatus.REJECT;
        }
        if (trip.getGroupTransferId() == null) {
            return OutboundRequestStatus.NEW;
        }
        return OutboundRequestStatus.IN_PROGRESS;
    }

    private OutboundRequestStatus explainStatus(CoopTaxiTrip trip, List<PassengerMessage> passengers) {
        if (ORDER_CANCELLED_BY_CLIENT.equals(trip.getStatus()) || passengers.isEmpty()) {
            return OutboundRequestStatus.REJECT;
        }
        return trip.getTaxiId() == null ? OutboundRequestStatus.NEW : OutboundRequestStatus.IN_PROGRESS;
    }

    private IntegrationParamsDTO getIntegrationParamsFromContractorId(Contractor contractor) {
        if (contractor == null) {
            return null;
        }

        return IntegrationParamsDTO
                .builder().contractorName(contractor.getContractorName())
                .contractorRusName(contractor.getContractorRusName())
                .integrationEmail(contractor.getIntegrationEmail())
                .tin(contractor.getTin())
                .contractorUrl(contractor.getUrl())
                .contractorLogin(contractor.getLogin())
                .contractorPassword(contractor.getPassword())
                .integrationType(contractor.getIntegrationType())
                .build();
    }

    private TaxiStopType getType(List<Waypoint> waypoints, int index) {
        if (index == 0) {
            return TaxiStopType.BOARDING;
        }
        if (index == waypoints.size() - 1) {
            return TaxiStopType.UNBOARDING;
        }
        return TaxiStopType.WAIT;
    }

    private OutContractorTaxiTripMessage.GroupTransferRequestInformation newRequestInformationDTOToGroupTransferRequestInformation(
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

    private ru.sber.transport.request.messaging.ChildSeatDetails childSeatDetailsToChildSeatDetails(
            ru.sberbank.ditsib.transport.request.messaging.message.ChildSeatDetails childSeatDetails
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

    private List<OutContractorWaypointMessage> contractorWaypointsMessageFromSharedRequest(
            SrmSharedRideDTO sharedRequest,
            List<RequestForTaxi> activeRequests
    ) {
        Map<UUID, Integer> requestNumberMap = new HashMap<>();
        int index = 1;
        for (Request request : activeRequests) {
            requestNumberMap.put(request.getId(), index++);
        }
        var activeWaypoints = sharedRequest.getWaypoints().stream()
                .filter(SrmWaypointGetDTO::isActive)
                .toList();
        var resultList = new ArrayList<OutContractorWaypointMessage>();
        for (int i = 0; i < activeWaypoints.size(); i++) {
            var waypoint = activeWaypoints.get(i);
            var passengerId = requestNumberMap.get(waypoint.getRequestKpiId());
            resultList.add(new OutContractorWaypointMessage(
                    String.valueOf(i + 1),
                    TaxiStopType.valueOf(activeWaypoints.get(i).getEventType()),
                    waypoint.getAddress(),
                    String.valueOf(waypoint.getLatitude()),
                    String.valueOf(waypoint.getLongitude()),
                    String.valueOf(passengerId == null ? 1 : passengerId),
                    Optional.ofNullable(waypoint.getWaitingTime())
                            .map(waitingTime -> Duration.of(waitingTime, MINUTES))
                            .orElse(Duration.ZERO)
            ));
        }
        return resultList;
    }

    private List<ru.sber.transport.request.messaging.Waypoint> transformToWaypointsCoop(
            SrmSharedRideDTO sharedRideDTO,
            List<RequestForTaxi> requests,
            Map<UUID, Employee> joinedPassengers) {

        var waypoints = sharedRideDTO.getWaypointsFinal();

        return waypoints.stream()
                .map(srmWaypointFinalDTO -> new ru.sber.transport.request.messaging.Waypoint(
                        srmWaypointFinalDTO.getAddress(),
                        srmWaypointFinalDTO.getLatitude(),
                        srmWaypointFinalDTO.getLongitude(),
                        Optional.ofNullable(srmWaypointFinalDTO.getWaitingTime()).orElse(0),
                        getContacts(srmWaypointFinalDTO, requests, joinedPassengers)))
                .toList();
    }

    private List<Contact> getContacts(SrmWaypointFinalDTO waypoint,
                                      List<RequestForTaxi> requests,
                                      Map<UUID, Employee> joinedPassengers) {
        Objects.requireNonNull(waypoint, "waypoint must not be null");
        Objects.requireNonNull(requests, "requests must not be null");
        var contacts = new ArrayList<Contact>();

        // проходимся по всем заявкам в точке
        for (var requestData : waypoint.getRequestDataList()) {
            var request = requests.stream()
                    .filter(requestForTaxi -> requestForTaxi.getId().equals(requestData.getRequestKpiId()))
                    .findFirst()
                    .orElse(null);
            Objects.requireNonNull(request, "requests must have request with requestKpiId");

            var employee = request.getPassenger();
            // Добавляем основного пассажира
            contacts.add(new Contact(employee.getMobilePhone(),
                    employee.getIO(),
                    employee.getFirstName(),
                    employee.getPatronymic(),
                    TaxiStopType.valueOf(requestData.getEventType())));
            // Если есть, добавляем присоединённых
            if (request.getJoinedPassengerIds() != null && !request.getJoinedPassengerIds().isEmpty()) {
                for (var joinedPassenger : joinedPassengers.values()) {
                    contacts.add(
                            new Contact(joinedPassenger.getMobilePhone(),
                                    joinedPassenger.getIO(),
                                    joinedPassenger.getFirstName(),
                                    joinedPassenger.getPatronymic(),
                                    TaxiStopType.valueOf(requestData.getEventType())));
                }
            }
        }
        return contacts;
    }
}
