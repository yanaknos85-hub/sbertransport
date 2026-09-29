package ru.sber.transport.trips.cargo.web.service.impl.file_resolvers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sber.transport.trips.cargo.business.dto.CheckinType;
import ru.sber.transport.trips.cargo.business.dto.Prefix;
import ru.sber.transport.trips.cargo.business.dto.TripsExportDto;
import ru.sber.transport.trips.cargo.business.dto.TripsExportFiltersDTO;
import ru.sber.transport.trips.cargo.business.model.*;
import ru.sber.transport.trips.cargo.business.providers.TripProvider;
import ru.sber.transport.trips.cargo.messaging.providers.ContractorProvider;
import ru.sber.transport.trips.cargo.messaging.providers.DispatcherProvider;
import ru.sber.transport.trips.cargo.messaging.providers.DriverProvider;
import ru.sber.transport.trips.cargo.messaging.providers.VehicleProvider;
import ru.sber.transport.trips.cargo.providers.checkin.CheckinProvider;
import ru.sber.transport.trips.cargo.web.service.AuthCheckService;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
class TripsResolverImpl implements DataExporter<TripsExportDto> {

    private static final String DELIMITER = "; ";

    private final TripProvider tripProvider;

    private final DispatcherProvider dispatcherProvider;

    private final VehicleProvider vehicleProvider;

    private final DriverProvider driverProvider;

    private final ContractorProvider contractorProvider;

    private final CheckinProvider checkinProvider;

    private final ObjectMapper objectMapper;

    private final AuthCheckService authCheckService;

    @SneakyThrows
    @Override
    public List<TripsExportDto> exportData(Map<String, ?> map, JwtAuthenticationToken authentication) {
        TripsExportFiltersDTO filters = new TripsExportFiltersDTO();
        if(map.containsKey("filters")){
            var encodedFilters = map.get("filters").toString();
            var stringFilters = new String(Base64.getDecoder().decode(encodedFilters), StandardCharsets.UTF_8);
            filters = objectMapper.readValue(stringFilters, TripsExportFiltersDTO.class);
        }
        var dispatcher = authCheckService.dispatcherAuthCheck(null, authentication);
        var trips = tripProvider.findAllByContractorIdOrderByDigitId(dispatcher == null ? filters.getContractorId() : dispatcher.getContractorId(), filters);
        var checkins = checkinProvider.findAllByTripIds(trips);
        var result = new LinkedList<TripsExportDto>();
        for (var i = 0; i < trips.size(); i++) {
            var item = trips.get(i);
            var hrid = "%s-%04d-%08d".formatted(Prefix.TC,
                    contractorProvider.getContractorDigitId(item.getContractorId()), item.getDigitId());
            if(TripStatus.ORDER_CANCELLED_BY_CLIENT.equals(item.getStatus()) || TripStatus.ORDER_CANCELLED_BY_DRIVER.equals(item.getStatus())){
                log.trace("Trip "+hrid+" will not be displayed because it has been canceled");
                continue;
            }
            var requests = objectMapper.convertValue(item.getRequests(), new TypeReference<List<CargoRequest>>() {});
            var waypoints = new LinkedList<>(item.getWaypoints().stream().sorted(Comparator.comparing(Waypoint::orderingIndex)).toList());
            var vehicle = Optional.ofNullable(item.getVehicleId()).flatMap(vehicleProvider::get);
            var driver = Optional.ofNullable(item.getDriverId())
                    .flatMap(driverProvider::get);
            var itemDispatcher = Optional.ofNullable(item.getDispatcherId())
                    .flatMap(dispatcherProvider::get);
            String timeZone;
            String requestHumanReadableIds;
            String comment;
            String author;
            if(requests.isEmpty()){
                if(item.getTimeZone() == null){
                    log.trace("Trip "+hrid+" will not be displayed due to data inconsistency");
                    continue;
                }
                timeZone = item.getTimeZone();

                requestHumanReadableIds = getHumanReadableIdFromWaypoints(item.getWaypoints());
                comment = item.getComment();
                author = "Н/Д";
            } else {
                timeZone = Optional.ofNullable(requests.get(0).getTimeZone()).orElse(ZoneOffset.UTC.getId());
                requestHumanReadableIds = requests.stream().map(CargoRequest::getHumanReadableId).map(this::mapNotNull).collect(Collectors.joining(DELIMITER));
                comment = requests.stream().map(CargoRequest::getComment).filter(Objects::nonNull).collect(Collectors.joining(DELIMITER));
                author = requests.stream().map(CargoRequest::getAuthor).map(this::mapName).map(this::mapNotNull).collect(Collectors.joining(DELIMITER));
            }
            var startTime = item.getStartTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of(timeZone).normalized().getId()));
            OffsetDateTime creationTime = null;
            if(item.getCreationTime()!=null) {
                creationTime = item.getCreationTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of(timeZone).normalized().getId()));
            }

            String firstAddress = null;
            String lastAddress = null;
            String middleAddresses = null;
            if (!waypoints.isEmpty()) {
                firstAddress = getAddressFromWaypoint(waypoints.getFirst());
                lastAddress = getAddressFromWaypoint(waypoints.getLast());
                middleAddresses = waypoints.stream().skip(1).limit(waypoints.size() - 2L).map(this::getAddressFromWaypoint).collect(Collectors.joining("; "));
            }

            var checkinsOfCurrentTrip = checkins.stream()
                    .filter(checkin -> checkin.getTripId().equals(item.getId()))
                    .sorted(Comparator.comparing(Checkin::getTime))
                    .collect(Collectors.toList()); //NOSONAR

            result.add(new TripsExportDto(
                    i + 1,
                    "%s-%04d-%08d".formatted(Prefix.TC,
                            contractorProvider.getContractorDigitId(item.getContractorId()), item.getDigitId()),
                    mapNotNull(item.getRouteHumanReadableId()),
                    mapNotNull(requestHumanReadableIds),
                    new TripsExportDto.StartTimeDto(startTime.toLocalDate(), startTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES)),
                    new TripsExportDto.Route(firstAddress, middleAddresses, lastAddress),
                    getCheckinData(checkinsOfCurrentTrip, waypoints),
                    mapStatus(item.getStatus()),
                    comment,
                    author,
                    vehicle.map(Vehicle::getStateNumber).orElse(null),
                    new TripsExportDto.NameDto(driver.map(this::mapFullName).orElse(null)),
                    creationTime != null ? new TripsExportDto.StartTimeDto(creationTime.toLocalDate(), creationTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES)) : null,
                    new TripsExportDto.Expected(item.getExpectedDistance(), waypoints.stream().map(Waypoint::waitingTime).filter(Objects::nonNull).reduce(Duration.ZERO, Duration::plus), item.getExpectedCost() != null ? item.getExpectedCost()/100D : null),
                    new TripsExportDto.Fact(item.getFactDistance(), null, getTripFactDuration(checkinsOfCurrentTrip), null),
                    "Грузовая",
                    null,
                    new TripsExportDto.Tariff(null, null),
                    null,
                    new TripsExportDto.DispatcherDto(itemDispatcher.map(this::mapName).orElse(null),
                            itemDispatcher.map(Dispatcher::getPhone).orElse(null)),
                    new TripsExportDto.DispatcherDataDto(item.getDriverWaitingTime()),
                    item.getLoaders()
            ));
        }
        return result;
    }

    private String getAddressFromWaypoint(Waypoint waypoint) {
        return waypoint.address() != null && !waypoint.address().isEmpty() ? waypoint.address() :  mapAddress(waypoint);
    }

    private String getHumanReadableIdFromWaypoints(List<Waypoint> waypointList) {
        if (isNullORBlank(waypointList) || isNullORBlank(waypointList.get(0).contacts())
                || isNullORBlank(waypointList.get(0).contacts().get(0).requests()))
        {
            return null;
        }

        return waypointList.stream()
                .flatMap(waypoint -> waypoint.contacts().stream())
                .flatMap(contact -> contact.requests().stream())
                .map(Waypoint.RouteRequest::getHumanReadableId)
                .filter(x -> java.util.Objects.nonNull(x) && !x.isBlank())
                .distinct()
                .collect(Collectors.joining(DELIMITER));
    }

    private <T extends List>  boolean isNullORBlank(T value) {
        return value == null || value.isEmpty();
    }

    private String mapNotNull(String source) {
        if (source == null) {
            return "Н/Д";
        }
        return source;
    }

    private String mapAddress(Waypoint waypoint) {
        if (waypoint == null) {
            return null;
        }
        var address = new StringBuilder();
        append(address, waypoint.region(), ", ");
        append(address, waypoint.city(), ", ");
        append(address, waypoint.street(), ", ");
        append(address, waypoint.house(), ", ");
        return address.toString();
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
            case DRIVER_ARRIVED -> "Водитель прибыл на погрузку";
            case ORDER_FINISHED -> "Заказ выполнен";
            case DRIVER_ASSIGNED -> "Закреплен за водителем";
            case TRIP_IN_PROGRESS -> "Водитель везет груз";
            case INTERMEDIATE_WAYPOINT_ARRIVED -> "Водитель на промежуточной точке";
            case DRIVER_ON_THE_WAY -> "Водитель выехал";
            case SENT_TO_CONTRACTOR -> "Опубликовано в системе исполнителя";
            case WAITING_FOR_ASSIGNMENT -> "Ожидает назначения";
            case ORDER_CANCELLED_BY_CLIENT -> "Поездка отменена клиентом";
            case ORDER_CANCELLED_BY_DRIVER -> "Поездка отменена водителем";
            default -> "Н/Д";
        };
    }

    private String mapCheckinType(CheckinType checkinType){
        return switch (checkinType){
            case AUTO -> "Да";
            case MANUAL -> "Нет";
        };
    }

    private Duration getTripFactDuration(List<Checkin> checkins){
        if(checkins==null||checkins.isEmpty()){
            return null;
        } else return Duration.between(checkins.get(0).getTime(), checkins.get(checkins.size() - 1).getTime());
    }

    private TripsExportDto.Checkin getCheckinData(List<Checkin> checkins, List<Waypoint> waypoints){
        if(checkins==null||checkins.isEmpty()){
            if(waypoints==null||waypoints.isEmpty()){
                return new TripsExportDto.Checkin(null,null, null);
            }
            return new TripsExportDto.Checkin(waypoints.size(),null, null);
        } else {
            var desiredStatuses = List.of(TripStatus.DRIVER_ARRIVED,
                    TripStatus.INTERMEDIATE_WAYPOINT_ARRIVED, TripStatus.ORDER_FINISHED);
            var filteredCheckins = checkins.stream()
                    .filter(checkin -> desiredStatuses.contains(checkin.getStatus()))
                    .collect(Collectors.toList()); //NOSONAR
            var statuses = new StringBuilder();
            var types = new StringBuilder();
            for (int i = 0; i<filteredCheckins.size(); i++){
                var count = i+1;
                if(count==filteredCheckins.size()){
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
}
