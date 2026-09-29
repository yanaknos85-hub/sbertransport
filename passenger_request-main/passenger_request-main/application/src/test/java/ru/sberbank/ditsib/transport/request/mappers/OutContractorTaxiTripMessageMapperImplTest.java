package ru.sberbank.ditsib.transport.request.mappers;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.request.messaging.*;
import ru.sber.transport.srm.model.SrmRequestKpiDTO;
import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sber.transport.srm.model.SrmWaypointFinalDTO;
import ru.sber.transport.srm.model.SrmWaypointGetDTO;
import ru.sberbank.ditsib.transport.constants.TaxiStopType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;
import ru.sberbank.ditsib.transport.constants.external.taxi.OutboundRequestStatus;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.Contractor;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.taxi.TaxiTariff;
import ru.sberbank.ditsib.transport.request.mappers.impl.OutContractorTaxiTripMessageMapperImpl;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

@ExtendWith(MockitoExtension.class)
class OutContractorTaxiTripMessageMapperImplTest {

    @InjectMocks
    private OutContractorTaxiTripMessageMapperImpl outContractorTaxiTripMessageMapper;

    @Test
    void transformSingleTaxiTripToContractorMessage() {
        var trip = Instancio.of(SingleTaxiTrip.class)
                .set(field(SingleTaxiTrip::getStatus), InboundTaxiTripStatus.WAITING_FOR_ASSIGNMENT)
                .set(field(SingleTaxiTrip::getTaxiId), null)
                .create();
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getStatus), TripRequestStatus.TAXI_APPROVED)
                .create();
        var taxiTariff = Instancio.create(TaxiTariff.class);
        var contractor = Instancio.create(Contractor.class);
        var organizationName = Instancio.create(String.class);
        var joinedPassengers = Instancio.ofMap(UUID.class, Employee.class)
                .size(2)
                .create();
        var passengerMessage = new PassengerMessage(
                "1",
                request.getPassenger().getIO(),
                request.getPassenger().getMobilePhone(),
                request.getCommentForDriver()
        );
        var startWaypoint = request.getWaypoints().getFirst().getAddress();
        var endWaypoint = request.getWaypoints().getLast().getAddress();
        var information = Instancio.of(RequestInformationMessage.class)
                .set(field(RequestInformationMessage::passengers), Collections.singletonList(passengerMessage))
                .set(field(RequestInformationMessage::initiator), passengerMessage)
                .set(field(RequestInformationMessage::countPassengers), 1)
                .set(field(RequestInformationMessage::planStart), request.getDesiredDate())
                .set(field(RequestInformationMessage::startAddress), "%s, %s, %s, %s, %s"
                        .formatted(startWaypoint.getCountry(),
                                startWaypoint.getRegion(),
                                startWaypoint.getCity(),
                                startWaypoint.getStreet(),
                                startWaypoint.getHouse()))
                .set(field(RequestInformationMessage::endAddress), "%s, %s, %s, %s, %s"
                        .formatted(endWaypoint.getCountry(),
                                endWaypoint.getRegion(),
                                endWaypoint.getCity(),
                                endWaypoint.getStreet(),
                                endWaypoint.getHouse()))
                .create();

        var expected = Instancio.of(OutContractorTaxiTripMessage.class)
                .set(field(OutContractorTaxiTripMessage::tripId), String.valueOf(trip.getId()))
                .set(field(OutContractorTaxiTripMessage::taxiId), trip.getTaxiId())
                .set(field(OutContractorTaxiTripMessage::taxiClass), request.getTaxiClass())
                .set(field(OutContractorTaxiTripMessage::humanId), trip.getHumanReadableId())
                .set(field(OutContractorTaxiTripMessage::commentForDriver), request.getCommentForDriver())
                .set(field(OutContractorTaxiTripMessage::information), information)
                .set(field(OutContractorTaxiTripMessage::status), OutboundRequestStatus.NEW)
                .set(field(OutContractorTaxiTripMessage::timezone), request.getTimeZone())
                .set(field(OutContractorTaxiTripMessage::contractorId), String.valueOf(contractor.getId()))
                .set(field(OutContractorTaxiTripMessage::organization), organizationName)
                .set(field(OutContractorTaxiTripMessage::integrationType), contractor.getIntegrationType())
                .set(field(OutContractorTaxiTripMessage::source), new Source(
                        startWaypoint.toStringTrimmed(),
                        startWaypoint.getLatitude(),
                        startWaypoint.getLongitude()))
                .set(field(OutContractorTaxiTripMessage::destination), new Destination(
                        endWaypoint.toStringTrimmed(),
                        endWaypoint.getLatitude(),
                        endWaypoint.getLongitude(),
                        new Contact(request.getPassenger().getMobilePhone(),
                                request.getPassenger().getIO(),
                                request.getPassenger().getFirstName(),
                                request.getPassenger().getPatronymic(),
                                null)))
                .set(field(OutContractorTaxiTripMessage::time), request.getExpected().getTime().toSeconds())
                .set(field(OutContractorTaxiTripMessage::distance), request.getExpected().getDistance())
                .set(field(OutContractorTaxiTripMessage::groupTransferClass), null)
                .set(field(OutContractorTaxiTripMessage::inn), null)
                .set(field(OutContractorTaxiTripMessage::transferInformation), null)
                .set(field(OutContractorTaxiTripMessage::transportId), null)
                .set(field(OutContractorTaxiTripMessage::transportType), TransportTypeEnum.TAXI)
                .set(field(OutContractorTaxiTripMessage::coop), false)
                .set(field(OutContractorTaxiTripMessage::workGroup), taxiTariff.getWorkGroup())
                .create();
        var actual = outContractorTaxiTripMessageMapper.transformSingleTaxiTripToContractorMessage(
                trip,
                request,
                taxiTariff,
                contractor,
                organizationName,
                joinedPassengers);
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields(
                        "commentForDriver",
                        "contractorTariffId",
                        "integrationParams",
                        "waypoints",
                        "information.waypoints")
                .isEqualTo(expected);
    }

    @Test
    void transformGroupTransferTripToContractorMessage() {
        var trip = Instancio.of(GroupTransferTrip.class)
                .set(field(GroupTransferTrip::getStatus), InboundTaxiTripStatus.WAITING_FOR_ASSIGNMENT)
                .set(field(GroupTransferTrip::getGroupTransferId), null)
                .create();
        var request = Instancio.of(RequestForGroupTransfer.class)
                .set(field(RequestForGroupTransfer::getStatus), TripRequestStatus.GROUP_TRANSFER_APPROVED)
                .create();
        var taxiTariff = Instancio.create(GroupTransferTariff.class);
        var contractor = Instancio.create(Contractor.class);
        var organizationName = Instancio.create(String.class);
        var joinedPassengers = Instancio.ofMap(UUID.class, Employee.class)
                .size(2)
                .create();
        var passengerMessage = new PassengerMessage(
                "1",
                request.getPassenger().getIO(),
                request.getPassenger().getMobilePhone(),
                ""
        );
        var startWaypoint = request.getWaypoints().getFirst().getAddress();
        var endWaypoint = request.getWaypoints().getLast().getAddress();
        var requestInformationMessage = Instancio.of(RequestInformationMessage.class)
                .set(field(RequestInformationMessage::passengers), Collections.singletonList(passengerMessage))
                .set(field(RequestInformationMessage::initiator), passengerMessage)
                .set(field(RequestInformationMessage::countPassengers), request.getPassengerCount())
                .set(field(RequestInformationMessage::planStart), request.getDesiredDate())
                .set(field(RequestInformationMessage::startAddress), "%s, %s, %s, %s, %s"
                        .formatted(startWaypoint.getCountry(),
                                startWaypoint.getRegion(),
                                startWaypoint.getCity(),
                                startWaypoint.getStreet(),
                                startWaypoint.getHouse()))
                .set(field(RequestInformationMessage::endAddress), "%s, %s, %s, %s, %s"
                        .formatted(endWaypoint.getCountry(),
                                endWaypoint.getRegion(),
                                endWaypoint.getCity(),
                                endWaypoint.getStreet(),
                                endWaypoint.getHouse()))
                .create();
        var information = request.getInformation();
        var transferInformation = Instancio.of(OutContractorTaxiTripMessage.GroupTransferRequestInformation.class)
                .set(field(OutContractorTaxiTripMessage.GroupTransferRequestInformation::childSeat), information.isChildSeat())
                .set(field(OutContractorTaxiTripMessage.GroupTransferRequestInformation::childSeatDetails), Instancio.of(ru.sber.transport.request.messaging.ChildSeatDetails.class)
                        .set(field(ru.sber.transport.request.messaging.ChildSeatDetails::group1), information.getChildSeatDetails().group1())
                        .set(field(ru.sber.transport.request.messaging.ChildSeatDetails::group2), information.getChildSeatDetails().group2())
                        .set(field(ru.sber.transport.request.messaging.ChildSeatDetails::booster), information.getChildSeatDetails().booster())
                        .set(field(ru.sber.transport.request.messaging.ChildSeatDetails::newborn), information.getChildSeatDetails().newborn())
                        .create())
                .set(field(OutContractorTaxiTripMessage.GroupTransferRequestInformation::bugs), information.isBugs())
                .set(field(OutContractorTaxiTripMessage.GroupTransferRequestInformation::bugsComment), information.getBugsComment())
                .set(field(OutContractorTaxiTripMessage.GroupTransferRequestInformation::bugsOversized), information.isBugsOversized())
                .set(field(OutContractorTaxiTripMessage.GroupTransferRequestInformation::bugsOversizedComment), information.getBugsOversizedComment())
                .set(field(OutContractorTaxiTripMessage.GroupTransferRequestInformation::animal), information.isAnimal())
                .set(field(OutContractorTaxiTripMessage.GroupTransferRequestInformation::animalComment), information.getAnimalComment())
                .set(field(OutContractorTaxiTripMessage.GroupTransferRequestInformation::addContactFIO), information.getAddContactFIO())
                .set(field(OutContractorTaxiTripMessage.GroupTransferRequestInformation::addContactPhone), information.getAddContactPhone())
                .set(field(OutContractorTaxiTripMessage.GroupTransferRequestInformation::numberFlight), information.getNumberFlight())
                .set(field(OutContractorTaxiTripMessage.GroupTransferRequestInformation::dateFlight), information.getDateFlight())
                .set(field(OutContractorTaxiTripMessage.GroupTransferRequestInformation::phoneHotel), information.getPhoneHotel())
                .set(field(OutContractorTaxiTripMessage.GroupTransferRequestInformation::typeVehicle), information.getTypeVehicle())
                .set(field(OutContractorTaxiTripMessage.GroupTransferRequestInformation::transportId), information.getTransportId())
                .create();
        var expected = Instancio.of(OutContractorTaxiTripMessage.class)
                .set(field(OutContractorTaxiTripMessage::tripId), String.valueOf(trip.getId()))
                .set(field(OutContractorTaxiTripMessage::taxiId), trip.getGroupTransferId())
                .set(field(OutContractorTaxiTripMessage::taxiClass), null)
                .set(field(OutContractorTaxiTripMessage::humanId), trip.getHumanReadableId())
                .set(field(OutContractorTaxiTripMessage::commentForDriver), request.getCommentForDriver())
                .set(field(OutContractorTaxiTripMessage::information), requestInformationMessage)
                .set(field(OutContractorTaxiTripMessage::status), OutboundRequestStatus.NEW)
                .set(field(OutContractorTaxiTripMessage::timezone), request.getTimeZone())
                .set(field(OutContractorTaxiTripMessage::contractorId), String.valueOf(contractor.getId()))
                .set(field(OutContractorTaxiTripMessage::organization), organizationName)
                .set(field(OutContractorTaxiTripMessage::integrationType), contractor.getIntegrationType())
                .set(field(OutContractorTaxiTripMessage::source), new Source(
                        startWaypoint.toStringTrimmed(),
                        startWaypoint.getLatitude(),
                        startWaypoint.getLongitude()))
                .set(field(OutContractorTaxiTripMessage::destination), new Destination(
                        endWaypoint.toStringTrimmed(),
                        endWaypoint.getLatitude(),
                        endWaypoint.getLongitude(),
                        new Contact(request.getPassenger().getMobilePhone(),
                                request.getPassenger().getIO(),
                                request.getPassenger().getFirstName(),
                                request.getPassenger().getPatronymic(),
                                null)))
                .set(field(OutContractorTaxiTripMessage::time), request.getExpected().getTime().toSeconds())
                .set(field(OutContractorTaxiTripMessage::distance), request.getExpected().getDistance())
                .set(field(OutContractorTaxiTripMessage::groupTransferClass), request.getGroupTransferClass())
                .set(field(OutContractorTaxiTripMessage::inn), null)
                .set(field(OutContractorTaxiTripMessage::transferInformation), transferInformation)
                .set(field(OutContractorTaxiTripMessage::transportId), request.getInformation().getTransportId())
                .set(field(OutContractorTaxiTripMessage::transportType), TransportTypeEnum.GROUP_TRANSFER)
                .set(field(OutContractorTaxiTripMessage::coop), false)
                .create();
        var actual = outContractorTaxiTripMessageMapper.transformGroupTransferTripToContractorMessage(
                trip,
                request,
                taxiTariff,
                contractor,
                organizationName,
                joinedPassengers);
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields(
                        "workGroup",
                        "contractorTariffId",
                        "integrationParams",
                        "waypoints",
                        "information.waypoints"
                )
                .isEqualTo(expected);
    }

    @Test
    void transformCoopTaxiTripToContractorMessage() {
        var trip = Instancio.of(CoopTaxiTrip.class)
                .set(field(CoopTaxiTrip::getTaxiId), null)
                .set(field(CoopTaxiTrip::getStatus), InboundTaxiTripStatus.WAITING_FOR_ASSIGNMENT)
                .create();
        var request1 = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getStatus), TripRequestStatus.TAXI_APPROVED)
                .create();
        var request2 = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getStatus), TripRequestStatus.TAXI_APPROVED)
                .create();
        var taxiTariff = Instancio.create(TaxiTariff.class);
        var contractor = Instancio.create(Contractor.class);
        var organizationName = Instancio.create(String.class);
        var joinedPassengers = Instancio.ofMap(UUID.class, Employee.class)
                .size(2)
                .create();
        var srmSharedRideDTO = Instancio.of(SrmSharedRideDTO.class)
                .set(field(SrmSharedRideDTO::getWaypoints), Instancio.ofList(SrmWaypointGetDTO.class)
                        .size(2)
                        .set(field(SrmWaypointGetDTO::getEventType), TaxiStopType.WAIT.name())
                        .create())
                .set(field(SrmSharedRideDTO::getWaypointsFinal), List.of(Instancio.of(SrmWaypointFinalDTO.class)
                                .set(field(SrmWaypointFinalDTO::getRequestDataList), Collections.singletonList(
                                        Instancio.of(SrmWaypointFinalDTO.RequestData.class)
                                                .set(field(SrmWaypointFinalDTO.RequestData::getRequestKpiId), request1.getId())
                                                .set(field(SrmWaypointFinalDTO.RequestData::getEventType), TaxiStopType.WAIT.name())
                                                .create()))
                                .create(),
                        Instancio.of(SrmWaypointFinalDTO.class)
                                .set(field(SrmWaypointFinalDTO::getRequestDataList), Collections.singletonList(
                                        Instancio.of(SrmWaypointFinalDTO.RequestData.class)
                                                .set(field(SrmWaypointFinalDTO.RequestData::getRequestKpiId), request2.getId())
                                                .set(field(SrmWaypointFinalDTO.RequestData::getEventType), TaxiStopType.WAIT.name())
                                                .create()))
                                .create()))
                .create();
        srmSharedRideDTO.getRequestKpiList().getFirst().setId(request1.getId());
        var passengerMessage1 = new PassengerMessage(
                "1",
                request1.getPassenger().getIO(),
                request1.getPassenger().getMobilePhone(),
                request1.getCommentForDriver()
        );
        var passengerMessage2 = new PassengerMessage(
                "2",
                request2.getPassenger().getIO(),
                request2.getPassenger().getMobilePhone(),
                request2.getCommentForDriver()
        );
        var startWaypoint = srmSharedRideDTO.getWaypoints().getFirst();
        var endWaypoint = srmSharedRideDTO.getWaypoints().getLast();
        var requestInformationMessage = Instancio.of(RequestInformationMessage.class)
                .set(field(RequestInformationMessage::passengers), List.of(passengerMessage1, passengerMessage2))
                .set(field(RequestInformationMessage::initiator), passengerMessage1)
                .set(field(RequestInformationMessage::countPassengers), srmSharedRideDTO.getRequestKpiList().stream()
                        .mapToInt(SrmRequestKpiDTO::getRequiredPassengers)
                        .sum())
                .set(field(RequestInformationMessage::planStart), srmSharedRideDTO.getWaypoints().getFirst().getStartTime().toLocalDateTime())
                .set(field(RequestInformationMessage::startAddress), startWaypoint.getAddress())
                .set(field(RequestInformationMessage::endAddress), endWaypoint.getAddress())
                .create();
        var expected = Instancio.of(OutContractorTaxiTripMessage.class)
                .set(field(OutContractorTaxiTripMessage::tripId), String.valueOf(trip.getId()))
                .set(field(OutContractorTaxiTripMessage::taxiId), trip.getTaxiId())
                .set(field(OutContractorTaxiTripMessage::taxiClass), request1.getTaxiClass())
                .set(field(OutContractorTaxiTripMessage::humanId), trip.getHumanReadableId())
                .set(field(OutContractorTaxiTripMessage::information), requestInformationMessage)
                .set(field(OutContractorTaxiTripMessage::status), OutboundRequestStatus.NEW)
                .set(field(OutContractorTaxiTripMessage::timezone), srmSharedRideDTO.getTimeZone())
                .set(field(OutContractorTaxiTripMessage::contractorId), String.valueOf(contractor.getId()))
                .set(field(OutContractorTaxiTripMessage::organization), organizationName)
                .set(field(OutContractorTaxiTripMessage::integrationType), contractor.getIntegrationType())
                .set(field(OutContractorTaxiTripMessage::source), new Source(
                        startWaypoint.getAddress(),
                        startWaypoint.getLatitude(),
                        startWaypoint.getLongitude()))
                .set(field(OutContractorTaxiTripMessage::destination), new Destination(
                        endWaypoint.getAddress(),
                        endWaypoint.getLatitude(),
                        endWaypoint.getLongitude(),
                        new Contact(request1.getPassenger().getMobilePhone(),
                                request1.getPassenger().getIO(),
                                request1.getPassenger().getFirstName(),
                                request1.getPassenger().getPatronymic(),
                                null)))
                .set(field(OutContractorTaxiTripMessage::time), request1.getExpected().getTime().toSeconds())
                .set(field(OutContractorTaxiTripMessage::distance), request1.getExpected().getDistance())
                .set(field(OutContractorTaxiTripMessage::groupTransferClass), null)
                .set(field(OutContractorTaxiTripMessage::inn), null)
                .set(field(OutContractorTaxiTripMessage::transferInformation), null)
                .set(field(OutContractorTaxiTripMessage::transportId), null)
                .set(field(OutContractorTaxiTripMessage::transportType), TransportTypeEnum.TAXI)
                .set(field(OutContractorTaxiTripMessage::coop), true)
                .set(field(OutContractorTaxiTripMessage::workGroup), taxiTariff.getWorkGroup())
                .create();
        var actual = outContractorTaxiTripMessageMapper.transformCoopTaxiTripToContractorMessage(
                trip,
                request1,
                List.of(request1, request2),
                taxiTariff,
                contractor,
                organizationName,
                joinedPassengers,
                srmSharedRideDTO);
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields(
                        "contractorTariffId",
                        "integrationParams",
                        "waypoints",
                        "information.waypoints",
                        "commentForDriver"
                )
                .isEqualTo(expected);
    }

    @Test
    void transformRejectedCoopTaxiTripToContractorMessage() {
        var trip = Instancio.of(CoopTaxiTrip.class)
                .set(field(CoopTaxiTrip::getStatus), InboundTaxiTripStatus.WAITING_FOR_ASSIGNMENT)
                .set(field(CoopTaxiTrip::getTaxiId), null)
                .create();
        var request1 = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getStatus), TripRequestStatus.TAXI_APPROVED)
                .create();
        var request2 = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getStatus), TripRequestStatus.TAXI_APPROVED)
                .create();
        var taxiTariff = Instancio.create(TaxiTariff.class);
        var contractor = Instancio.create(Contractor.class);
        var organizationName = Instancio.create(String.class);
        var joinedPassengers = Instancio.ofMap(UUID.class, Employee.class)
                .size(2)
                .create();
        var expected = Instancio.of(OutContractorTaxiTripMessage.class)
                .set(field(OutContractorTaxiTripMessage::tripId), String.valueOf(trip.getId()))
                .set(field(OutContractorTaxiTripMessage::taxiId), trip.getTaxiId())
                .set(field(OutContractorTaxiTripMessage::taxiClass), request1.getTaxiClass())
                .set(field(OutContractorTaxiTripMessage::humanId), trip.getHumanReadableId())
                .set(field(OutContractorTaxiTripMessage::information), null)
                .set(field(OutContractorTaxiTripMessage::status), OutboundRequestStatus.NEW)
                .set(field(OutContractorTaxiTripMessage::timezone), "Etc/GMT-3")
                .set(field(OutContractorTaxiTripMessage::contractorId), String.valueOf(contractor.getId()))
                .set(field(OutContractorTaxiTripMessage::organization), organizationName)
                .set(field(OutContractorTaxiTripMessage::integrationType), contractor.getIntegrationType())
                .set(field(OutContractorTaxiTripMessage::source), null)
                .set(field(OutContractorTaxiTripMessage::destination), null)
                .set(field(OutContractorTaxiTripMessage::time), request1.getExpected().getTime().toSeconds())
                .set(field(OutContractorTaxiTripMessage::distance), request1.getExpected().getDistance())
                .set(field(OutContractorTaxiTripMessage::groupTransferClass), null)
                .set(field(OutContractorTaxiTripMessage::inn), null)
                .set(field(OutContractorTaxiTripMessage::transferInformation), null)
                .set(field(OutContractorTaxiTripMessage::transportId), null)
                .set(field(OutContractorTaxiTripMessage::transportType), TransportTypeEnum.TAXI)
                .set(field(OutContractorTaxiTripMessage::coop), true)
                .set(field(OutContractorTaxiTripMessage::workGroup), taxiTariff.getWorkGroup())
                .create();
        var actual = outContractorTaxiTripMessageMapper.transformRejectedCoopTaxiTripToContractorMessage(
                trip,
                request1,
                List.of(request1, request2),
                taxiTariff,
                contractor,
                organizationName,
                joinedPassengers);
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields(
                        "contractorTariffId",
                        "integrationParams",
                        "waypoints",
                        "information.waypoints",
                        "commentForDriver"
                )
                .isEqualTo(expected);
    }
}