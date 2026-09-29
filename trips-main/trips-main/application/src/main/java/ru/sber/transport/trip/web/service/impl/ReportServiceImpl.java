package ru.sber.transport.trip.web.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.trip.business.dto.CheckinType;
import ru.sber.transport.trip.business.dto.Prefix;
import ru.sber.transport.trip.business.dto.TripsExportDto;
import ru.sber.transport.trip.business.model.*;
import ru.sber.transport.trip.business.providers.TripProvider;
import ru.sber.transport.trip.database.trips.tables.records.TripsRecord;
import ru.sber.transport.trip.messaging.providers.ContractorProvider;
import ru.sber.transport.trip.messaging.providers.DispatcherProvider;
import ru.sber.transport.trip.messaging.providers.DriverProvider;
import ru.sber.transport.trip.messaging.providers.VehicleProvider;
import ru.sber.transport.trip.messaging.senders.ReportSender;
import ru.sber.transport.trip.providers.checkin.CheckinProvider;
import ru.sber.transport.trip.providers.trips.mapping.TripMapper;
import ru.sber.transport.trip.web.dto.PassengersTripDTO;
import ru.sber.transport.trip.web.service.PassengerInfoExtractorService;
import ru.sber.transport.trip.web.service.ReportService;
import ru.sber.transport.trip_reports.message.TripReportMessage;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {



    @Value("${report.processor.selection.limit:100}")
    private int limit;

    private final TripProvider tripProvider;

    private final DispatcherProvider dispatcherProvider;

    private final VehicleProvider vehicleProvider;

    private final DriverProvider driverProvider;

    private final ContractorProvider contractorProvider;

    private final CheckinProvider checkinProvider;

    private final ObjectMapper objectMapper;

    private final ReportSender reportSender;

    private final TripMapper tripMapper;

    private final PassengerInfoExtractorService passengerInfoExtractorService;

    @Override
    public void processReport() {
        var trips = tripProvider.findAllByReportCreatedFalseAndTerminalStatusesAndLimit(limit);
        var tripIds = trips.stream().map(TripsRecord::getId).collect(Collectors.toList());
        var checkins = checkinProvider.findAllByTripIds(tripIds);
        var reportedTripsIds = new ArrayList<UUID>();
        trips.forEach(tripsRecord -> {
            try {
                var item = tripMapper.toModel(tripsRecord);
                var requests = objectMapper.convertValue(item.getRequests(), new TypeReference<List<Request>>() {});
                var waypoints = new LinkedList<>(item.getWaypoints().stream().sorted(Comparator.comparing(Waypoint::orderingIndex)).toList());
                var vehicle = Optional.ofNullable(item.getVehicleId()).flatMap(vehicleProvider::get);
                var driver = Optional.ofNullable(item.getDriverId())
                        .flatMap(driverProvider::get);
                var itemDispatcher = Optional.ofNullable(item.getDispatcherId())
                        .flatMap(dispatcherProvider::get);

                PassengersTripDTO passengers = passengerInfoExtractorService.extractPassengersInfo(requests, item);
                String firstAddress = null;
                String lastAddress = null;
                String middleAddresses = null;
                if (!waypoints.isEmpty()) {
                    firstAddress = mapAddress(waypoints.getFirst());
                    lastAddress = mapAddress(waypoints.getLast());
                    middleAddresses = waypoints.stream().skip(1).limit(waypoints.size() - 2L).map(this::mapAddress).collect(Collectors.joining("; "));
                }

                var checkinsOfCurrentTrip = checkins.stream()
                        .filter(checkin -> checkin.getTripId().equals(item.getId()))
                        .sorted(Comparator.comparing(Checkin::getTime))
                        .collect(Collectors.toList());
                var checkinData = getCheckinData(checkinsOfCurrentTrip, waypoints);
                var reportMessage = new TripReportMessage(
                        item.getId(),
                        item.getContractorId(),
                        "%s-%04d-%08d".formatted(Prefix.TP,
                                contractorProvider.getContractorDigitId(item.getContractorId()), item.getDigitId()),
                        passengers.requestHumanReadableIds(),
                        passengers.timeZone(),
                        item.getExpectedStartTime(),
                        item.getFactStartTime(),
                        firstAddress,
                        middleAddresses,
                        lastAddress,
                        checkinData.quantity(),
                        checkinData.status(),
                        checkinData.waypointConfirmation(),
                        mapStatus(item.getStatus()),
                        passengers.comments(),
                        passengers.passengers(),
                        null,
                        vehicle.map(Vehicle::getStateNumber).orElse(null),
                        driver.map(this::mapFullName).orElse(null),
                        item.getCreationTime(),
                        item.getExpectedDistance(),
                        item.getExpectedTime() != null ? Duration.ofSeconds(item.getExpectedTime()) : null,
                        waypoints.stream().map(Waypoint::waitingTime).filter(Objects::nonNull).reduce(Duration.ZERO, Duration::plus),
                        null,
                        item.getFactDistance(),
                        getTripFactDuration(checkinsOfCurrentTrip),
                        item.getDriverWaitingTime(),
                        passengers.type(),
                        itemDispatcher.map(this::mapName).orElse(null),
                        itemDispatcher.map(Dispatcher::getPhone).orElse(null),
                        getTaxiClass(item.getTaxiClass()),
                        passengers.count()
                );
                reportSender.send(reportMessage);
                reportedTripsIds.add(item.getId());
            } catch (Exception e){
                log.error("Error while report processing by trip %s, exception class - %s".formatted(tripsRecord.getId(), e.getClass().toString()));
                e.printStackTrace();
            }
        });
        tripProvider.setReportCreatedIsTrueByIds(reportedTripsIds);
    }

    private String mapAddress(Waypoint waypoint) {
        if (waypoint == null) {
            return null;
        }
        if(waypoint.fullAddress() != null) {
            return waypoint.fullAddress();
        } else {
            var address = new StringBuilder();
            append(address, waypoint.country(), ", ");
            append(address, waypoint.region(), ", ");
            append(address, waypoint.city(), ", ");
            append(address, waypoint.street(), ", ");
            append(address, waypoint.house(), ", ");
            append(address, waypoint.building(), ", ");
            return address.toString();
        }
    }

    private void append(@NonNull StringBuilder builder, String source, String delimiter) {
        if (source != null) {
            if (!builder.isEmpty()) {
                builder.append(delimiter);
            }
            builder.append(source);
        }
    }

    private String mapName(HasName hasName) {
        if (hasName == null) {
            return null;
        }
        var nameBuilder = new StringBuilder();
        append(nameBuilder, hasName.getFirstName(), " ");
        append(nameBuilder, hasName.getPatronymic(), " ");
        append(nameBuilder, Optional.ofNullable(hasName.getLastName()).map(s -> s.charAt(0)).map(c -> c + ".").orElse(""), " ");
        return nameBuilder.toString();
    }

    private String mapFullName(HasName hasName) {
        if (hasName == null) {
            return null;
        }
        var nameBuilder = new StringBuilder();
        append(nameBuilder, hasName.getFirstName(), " ");
        append(nameBuilder, hasName.getPatronymic(), " ");
        append(nameBuilder, hasName.getLastName(), " ");
        return nameBuilder.toString();
    }

    private String mapStatus(TripStatus status) {
        return switch (status) {
            case ORDER_EXPIRED -> "Поездка просрочена";
            case DRIVER_ARRIVED -> "Водитель прибыл в пункт отправления";
            case ORDER_FINISHED -> "Поездка завершена";
            case DRIVER_ASSIGNED -> "Водитель назначен";
            case TRIP_IN_PROGRESS -> "Водитель везет клиента";
            case INTERMEDIATE_WAYPOINT_ARRIVED -> "Остановка в промежуточной точке маршрута";
            case DRIVER_ON_THE_WAY -> "Водитель в пути";
            case SENT_TO_CONTRACTOR -> "Отправлена перевозчику";
            case WAITING_FOR_ASSIGNMENT -> "Ожидает назначения водителя";
            case ORDER_CANCELLED_BY_CLIENT -> "Отменена клиентом";
            case ORDER_CANCELLED_BY_DRIVER -> "Отменена водителем";
            case UNDEFINED -> "Не определено";
        };
    }

    private String mapCheckinType(CheckinType checkinType) {
        return switch (checkinType) {
            case AUTO -> "Да";
            case MANUAL -> "Нет";
        };
    }

    private Duration getTripFactDuration(List<Checkin> checkins) {
        if (checkins == null || checkins.isEmpty()) {
            return null;
        } else return Duration.between(checkins.get(0).getTime(), checkins.get(checkins.size() - 1).getTime());
    }

    private TripsExportDto.Checkin getCheckinData(List<Checkin> checkins, List<Waypoint> waypoints) {
        if (checkins == null || checkins.isEmpty()) {
            if (waypoints == null || waypoints.isEmpty()) {
                return new TripsExportDto.Checkin(null, null, null);
            }
            return new TripsExportDto.Checkin(waypoints.size(), null, null);
        } else {
            var desiredStatuses = List.of(TripStatus.DRIVER_ARRIVED,
                    TripStatus.INTERMEDIATE_WAYPOINT_ARRIVED, TripStatus.ORDER_FINISHED);
            var filteredCheckins = checkins.stream()
                    .filter(checkin -> desiredStatuses.contains(checkin.getStatus()))
                    .toList();
            var statuses = new StringBuilder();
            var types = new StringBuilder();
            for (int i = 0; i < filteredCheckins.size(); i++) {
                var count = i + 1;
                if (count == filteredCheckins.size()) {
                    statuses.append(count).append(") ").append(mapStatus(filteredCheckins.get(i).getStatus()));
                    types.append(count).append(") ").append(mapCheckinType(filteredCheckins.get(i).getType()));
                } else {
                    statuses.append(count).append(") ").append(mapStatus(filteredCheckins.get(i).getStatus())).append("\n");
                    types.append(count).append(") ").append(mapCheckinType(filteredCheckins.get(i).getType())).append("\n");
                }
            }
            return new TripsExportDto.Checkin(waypoints.size(), statuses.toString(), types.toString());
        }
    }

    private String getTaxiClass(String taxiClass) {
        if (taxiClass != null) {
            var classes = Arrays.stream(TaxiClass.values()).map(TaxiClass::name).toList();
            if (classes.contains(taxiClass)) {
                return TaxiClass.valueOf(taxiClass).getRusName();
            } else return "Групповой трансфер";
        } else return "Н/Д";
    }
}
