package ru.sber.transport.trip.web.service.impl.file_resolvers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sber.transport.trip.business.dto.CheckinType;
import ru.sber.transport.trip.business.dto.Prefix;
import ru.sber.transport.trip.business.dto.TripsExportDto;
import ru.sber.transport.trip.business.dto.TripsExportFiltersDTO;
import ru.sber.transport.trip.business.model.*;
import ru.sber.transport.trip.business.providers.TripProvider;
import ru.sber.transport.trip.messaging.providers.ContractorProvider;
import ru.sber.transport.trip.messaging.providers.DispatcherProvider;
import ru.sber.transport.trip.messaging.providers.DriverProvider;
import ru.sber.transport.trip.messaging.providers.VehicleProvider;
import ru.sber.transport.trip.providers.checkin.CheckinProvider;
import ru.sber.transport.trip.providers.history.trip.TripHistoryProvider;
import ru.sber.transport.trip.web.service.impl.file_resolvers.utils.StringHandlerUtil;
import ru.sber.transport.trip.web.dto.PassengersTripDTO;
import ru.sber.transport.trip.web.service.AuthCheckService;
import ru.sber.transport.trip.web.service.PassengerInfoExtractorService;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static ru.sber.transport.trip.web.service.impl.file_resolvers.utils.StringHandlerUtil.append;

@Slf4j
@Component
@RequiredArgsConstructor
@Deprecated(since = "D-03.022.000", forRemoval = true)
class TripsResolverImpl implements DataExporter<TripsExportDto> {

    private final TripProvider tripProvider;

    private final DispatcherProvider dispatcherProvider;

    private final VehicleProvider vehicleProvider;

    private final DriverProvider driverProvider;

    private final ContractorProvider contractorProvider;

    private final CheckinProvider checkinProvider;

    private final ObjectMapper objectMapper;

    private final AuthCheckService authCheckService;

    private final PassengerInfoExtractorService passengerInfoExtractorService;

    private final TripHistoryProvider tripHistoryProvider;

    @SneakyThrows
    @Override
    public List<TripsExportDto> exportData(Map<String, ?> map, JwtAuthenticationToken authentication) {
        TripsExportFiltersDTO filters = new TripsExportFiltersDTO();
        if (map.containsKey("filters")) {
            var encodedFilters = map.get("filters").toString();
            var stringFilters = new String(Base64.getDecoder().decode(encodedFilters), StandardCharsets.UTF_8);
            filters = objectMapper.readValue(stringFilters, TripsExportFiltersDTO.class);
        }
        var dispatcher = authCheckService.dispatcherAuthCheck(null, authentication);
        var trips = tripProvider.findAllByContractorIdOrderByDigitId(dispatcher == null ? filters.getContractorId() : dispatcher.getContractorId(), filters);
        var tripIds = trips.stream().map(Trip::getId).collect(Collectors.toList());
        var checkins = checkinProvider.findAllByTripIds(tripIds);
        var historyList = tripHistoryProvider.getHistoryByTripIds(tripIds);
        var result = new LinkedList<TripsExportDto>();
        for (var i = 0; i < trips.size(); i++) {
            var item = trips.get(i);
            var requests = objectMapper.convertValue(item.getRequests(), new TypeReference<List<Request>>() {});
            var waypoints = new LinkedList<>(item.getWaypoints().stream().sorted(Comparator.comparing(Waypoint::orderingIndex)).toList());
            var vehicle = Optional.ofNullable(item.getVehicleId()).flatMap(vehicleProvider::get);
            var driver = Optional.ofNullable(item.getDriverId())
                    .flatMap(driverProvider::get);
            var itemDispatcher = Optional.ofNullable(item.getDispatcherId())
                    .flatMap(dispatcherProvider::get);

            PassengersTripDTO passengers = passengerInfoExtractorService.extractPassengersInfo(requests, item);
            OffsetDateTime factStartTime = null;
            if (item.getFactStartTime() != null) {
                factStartTime = item.getFactStartTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of(passengers.timeZone()).normalized().getId()));
            }
            var expectedStartTime = item.getExpectedStartTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of(passengers.timeZone()).normalized().getId()));
            OffsetDateTime creationTime = null;
            if (item.getCreationTime() != null) {
                creationTime = item.getCreationTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of(passengers.timeZone()).normalized().getId()));
            }

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
                    .collect(Collectors.toList()); //NOSONAR
            var chekinData = getCheckinData(checkinsOfCurrentTrip, waypoints);
            var tripHistory = historyList.stream()
                    .filter(history -> history.getTripId().equals(item.getId())).toList();
            var offset = item.getTimeZone() != null ? ZoneOffset.of(ZoneId.of(item.getTimeZone()).normalized().getId()) : ZoneOffset.UTC;
            var statusChangedHistoryObject = tripHistory.stream()
                    .sorted(Comparator.comparing(TripHistoryItem::getChangeTime).reversed())
                    .filter(history -> ActionType.STATUS_CHANGING.equals(history.getAction()))
                    .findFirst().orElse(null);
            var statusChangedTime = statusChangedHistoryObject != null ? statusChangedHistoryObject.getChangeTime()
                    .atOffset(offset) : null;
            var dispatcherTakeToWorkHistoryObject = tripHistory.stream()
                    .filter(history -> ActionType.DISPATCHER_TAKE_TO_WORK.equals(history.getAction()))
                    .findFirst().orElse(null);
            var dispatcherTakeToWorkTime = dispatcherTakeToWorkHistoryObject != null ? dispatcherTakeToWorkHistoryObject.getChangeTime()
                    .atOffset(offset) : null;

            result.add(new TripsExportDto(
                    i + 1,
                    "%s-%04d-%08d".formatted(Prefix.TP,
                            contractorProvider.getContractorDigitId(item.getContractorId()), item.getDigitId()),
                    passengers.requestHumanReadableIds(),
                    creationTime != null ? creationTime.toLocalDate() : null,
                    creationTime != null ? creationTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES) : null,
                    expectedStartTime.toLocalDate(),
                    expectedStartTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES),
                    factStartTime != null ? factStartTime.toLocalDate() : null,
                    factStartTime != null ? factStartTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES) : null,
                    dispatcherTakeToWorkTime != null ? dispatcherTakeToWorkTime.toLocalDate() : null,
                    dispatcherTakeToWorkTime != null ? dispatcherTakeToWorkTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES) : null,
                    statusChangedTime != null ? statusChangedTime.toLocalDate() : null,
                    statusChangedTime != null ? statusChangedTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES) : null,
                    firstAddress,
                    middleAddresses,
                    lastAddress,
                    chekinData.quantity(),
                    chekinData.status(),
                    chekinData.waypointConfirmation(),
                    mapStatus(item.getStatus()),
                    passengers.comments(),
                    passengers.passengers(),
                    vehicle.map(Vehicle::getStateNumber).orElse(null),
                    driver.map(this::mapFullName).orElse(null),
                    item.getExpectedDistance(),
                    item.getExpectedTime() != null ? Duration.ofSeconds(item.getExpectedTime()).toMinutes() : null,
                    waypoints.stream().map(Waypoint::waitingTime).filter(Objects::nonNull).reduce(Duration.ZERO, Duration::plus).toMinutes(),
                    item.getExpectedCost() != null ? item.getExpectedCost() / 100D : null,
                    item.getFactDistance(),
                    getTripFactDuration(checkinsOfCurrentTrip) != null ? getTripFactDuration(checkinsOfCurrentTrip).toMinutes() : null,
                    item.getDriverWaitingTime() != null ? item.getDriverWaitingTime().toMinutes() : null,
                    passengers.type(),
                    itemDispatcher.map(StringHandlerUtil::mapName).orElse(null),
                    itemDispatcher.map(Dispatcher::getPhone).orElse(null),
                    getTaxiClass(item.getTaxiClass()),
                    passengers.count()
            ));
        }
        return result;
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
            default -> "Н/Д";
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
            if(classes.contains(taxiClass)){
                return TaxiClass.valueOf(taxiClass).getRusName();
            } else return "Групповой трансфер";
        } else return "Н/Д";
    }
}
