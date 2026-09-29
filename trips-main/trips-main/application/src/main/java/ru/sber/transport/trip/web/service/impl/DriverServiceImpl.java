package ru.sber.transport.trip.web.service.impl;

import com.dynatrace.oneagent.sdk.OneAgentSDKFactory;
import com.dynatrace.oneagent.sdk.api.OneAgentSDK;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.trip.business.dto.*;
import ru.sber.transport.trip.business.model.*;
import ru.sber.transport.trip.business.providers.TripProvider;
import ru.sber.transport.trip.messaging.ChannelType;
import ru.sber.transport.trip.messaging.providers.*;
import ru.sber.transport.trip.messaging.senders.dispatcher.DriverSender;
import ru.sber.transport.trip.messaging.senders.dispatcher.TripSender;
import ru.sber.transport.trip.providers.checkin.CheckinProvider;
import ru.sber.transport.trip.providers.checkin.mapper.CheckinMapper;
import ru.sber.transport.trip.providers.driver.mapper.DriverMapper;
import ru.sber.transport.trip.providers.history.trip.TripHistoryProvider;
import ru.sber.transport.trip.providers.shift.mapper.ShiftMapper;
import ru.sber.transport.trip.web.service.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@RequiredArgsConstructor
@Slf4j
@Transactional
@Component
public class DriverServiceImpl implements DriverService {

    private final TripProvider tripProvider;

    private final VerificationService verificationService;

    private final ContractorProvider contractorProvider;

    private final DriverProvider driverProvider;

    private final DriverMapper driverMapper;

    private final CheckinProvider checkinProvider;

    private final CheckinMapper checkinMapper;

    private final ShiftProvider shiftProvider;

    private final ShiftService shiftService;

    private final ShiftMapper shiftMapper;

    private final UpdateTripService updateTripService;

    private final DriverSender driverSender;

    private final DispatcherProvider dispatcherProvider;

    private final VehicleProvider vehicleProvider;

    private final TripSender tripSender;

    private final TripHistoryProvider tripHistoryProvider;

    private final TripService tripService;

    private final OneAgentSDK oneAgentSdk = OneAgentSDKFactory.createInstance();

    private final AuthCheckService authCheckService;

    @Value("${driver.assign.minradius:3.0}")
    private double minRadius;

    @Value("${driver.assign.maxradius:15.0}")
    private double maxRadius;

    @Value("${driver.assign.autoAssignRadiusDelta:1.0}")
    private double autoAssignRadiusDelta;

    @Value("${driver.assign.timeSecs:180}")
    private int timeSecs;

    @Value("${driver.assign.finishSearchMin:10}")
    private int finishSearchMin;

    @Value("${driver.assign.reservedTimeMin:90}")
    private int reservedTimeMin;

    private static final double AVERAGE_RADIUS_OF_EARTH_KM = 6371;

    @Override
    public void checkCurrentTrip(UUID tripId, Driver driver) {
        if (driver.getActiveTripId() == null && tripId != null) {
            var currentTrip = tripProvider.get(tripId)
                    .orElseThrow(() -> new EntityNotFoundException(Trip.class, tripId));
            var hrid = "%s-%04d-%08d".formatted(Prefix.TP,
                    contractorProvider.getContractorDigitId(currentTrip.getContractorId()),
                    currentTrip.getDigitId());
            verificationService.checkIsTripCancelled(currentTrip, hrid);
            verificationService.checkDriverToTripRelation(driverProvider.get(currentTrip.getDriverId()).orElse(null), driver);
        }
    }

    @Override
    public void lastPointInfo(GeoWaypointDTO geoWaypointDTO, Driver driver) {
        driverProvider.saveLocationData(driver.getId(),
                geoWaypointDTO.getTimeZone(),
                OffsetDateTime.now(ZoneOffset.UTC),
                geoWaypointDTO.getLatitude(),
                geoWaypointDTO.getLongitude(),
                geoWaypointDTO.getAzimuth());
        driverSender.send(
                driverProvider.get(driver.getId()).orElseThrow(() -> new EntityNotFoundException(Driver.class, driver.getId())));
    }

    @Override
    public CheckinResponseDTO getTripCheckins(Driver driver, UUID tripId) {
        var trip = tripProvider.get(tripId).orElseThrow(() -> new EntityNotFoundException(Trip.class, tripId));
        verificationService.checkDriverToTripRelation(driverProvider.get(trip.getDriverId()).orElse(null), driver);
        var checkins = checkinProvider.findAllByTripId(tripId)
                .stream().map(checkinMapper::toDto)
                .sorted(Comparator.comparing(CheckinDTO::getTime))
                .toList();
        verificationService.checkCheckinsExistence(checkins.size(), trip.getDigitId());
        var checkinResponse = new CheckinResponseDTO();
        checkinResponse.setChekins(checkins);
        checkinResponse.setComplete(TripStatus.ORDER_FINISHED.equals(trip.getStatus()));
        var duration = Duration.between(checkins.get(0).getTime(), checkins.get(checkins.size() - 1).getTime());
        checkinResponse.setTripDuration(
                new TripDurationDTO(
                        duration.toDaysPart(),
                        duration.toHoursPart(),
                        duration.toMinutesPart(),
                        duration.toSecondsPart())
        );
        return checkinResponse;
    }

    @Override
    public Page<DriverShiftDTO> getDriversByCoordinates(UUID contractorId, DriverLocationSearchDTO driverLocationSearchDTO, Authentication authentication) {
        authCheckService.dispatcherAuthCheck(contractorId, (JwtAuthenticationToken) authentication);
        List<DriverShiftDTO> driverDTO;
        if (driverLocationSearchDTO.isForPlanning()) {
            driverDTO = getAllDriversForPlanning(driverLocationSearchDTO.getTripStartDate(), driverLocationSearchDTO.getTripEndDate(), contractorId, driverLocationSearchDTO.getName(), driverLocationSearchDTO.getAutoparkId());
        } else {
            if (driverLocationSearchDTO.isFullSearch()) {
                driverDTO = getAllDriversOnlineByContractor(contractorId, driverLocationSearchDTO.getSize(), driverLocationSearchDTO.getPage(), driverLocationSearchDTO.getName(), driverLocationSearchDTO.getAutoparkId());
            } else {
                driverDTO = getDriversAround(contractorId, driverLocationSearchDTO.getLatitude(),
                        driverLocationSearchDTO.getLongitude(), minRadius, driverLocationSearchDTO.getName(), driverLocationSearchDTO.getAutoparkId()).stream().map(driverMapper::toDriverShiftDto).collect(Collectors.toList()); // NOSONAR
            }
        }
        setShifts(driverDTO);
        var iterator = driverDTO.iterator();
        while (iterator.hasNext() && !driverLocationSearchDTO.isForPlanning()) {
            var dto = iterator.next();
            if (driverLocationSearchDTO.getTripStartDate() != null
                    && dto.getCurrentShift().getEndDate().isBefore(driverLocationSearchDTO.getTripStartDate())
                    && (!driverLocationSearchDTO.isFullSearch() || driverLocationSearchDTO.isEnableShiftFilter())) {
                iterator.remove();
                continue;
            }
            if (LocalDateTime.now(ZoneOffset.UTC).isAfter(dto.getCurrentShift().getEndDate())) {
                var driver = driverProvider.get(dto.getCurrentShift().getDriverId())
                        .orElseThrow(() -> new EntityNotFoundException(Driver.class, dto.getCurrentShift().getDriverId()));
                if (driver.getActiveTripId() == null && !driver.isServing()) {
                    iterator.remove();
                    continue;
                }
            }
            if (dto.getLatitude() != null && dto.getLongitude() != null) {
                dto.setDistanceInKilometer(calculateDistanceInKilometer(driverLocationSearchDTO.getLatitude(),
                        driverLocationSearchDTO.getLongitude(), dto.getLatitude(), dto.getLongitude()));
            } else {
                log.info("Расстояние от местоположения водителя {} до точки выполнения заказа не будет рассчитано из-за отсутствия данных по координатам водителя", dto.getHumanReadableId());
            }
        }
        var driversWithoutLocationData = driverDTO.stream()
                .filter(driver -> driver.getDistanceInKilometer() == null)
                .toList(); // NOSONAR

        if (!driversWithoutLocationData.isEmpty()) {
            driverDTO.removeAll(driversWithoutLocationData);
            var sortedList = driverDTO.stream()
                    .sorted(Comparator.comparing(DriverShiftDTO::getDistanceInKilometer))
                    .collect(Collectors.toCollection(ArrayList::new));
            sortedList.addAll(driversWithoutLocationData);
            return new PageImpl<>(sortedList,
                    PageRequest.of(driverLocationSearchDTO.getPage(), driverLocationSearchDTO.getSize()),
                    driverDTO.size());
        } else return new PageImpl<>(driverDTO.stream()
                .sorted(Comparator.comparing(DriverShiftDTO::getDistanceInKilometer)).toList(),
                PageRequest.of(driverLocationSearchDTO.getPage(), driverLocationSearchDTO.getSize()),
                driverDTO.size());
    }

    @Override
    public void setOnline(Driver driver, boolean state) {
        driver.setOnline(state);
        if (state) {
            checkCanOnline(driver);
        } else {
            deactivateShift(driver);
        }
    }

    @Override
    public Driver assignDriver(Trip trip) {
        log.trace("assignDriver: processing trip %s".formatted(trip.getId()));
        var startWaypoint = trip.getWaypoints().get(0);
        var requestLatitude = startWaypoint.latitude();
        var requestLongitude = startWaypoint.longitude();
        int autoassignCounter = 0;
        if (trip.getAutoassignCounter() != null) {
            autoassignCounter = trip.getAutoassignCounter();
        }
        double radius = minRadius + (autoassignCounter * autoAssignRadiusDelta);
        if (radius > maxRadius) {
            radius = maxRadius;
        }
        // список отсортировать по рейтингу водителя (driver.rating)
        var driverSortedList =
                getDriversAround(trip.getContractorId(), requestLatitude, requestLongitude, radius, null, trip.getAutoparkId()).stream()
                        .map(driverMapper::toDriverShiftDto)
                        .collect(Collectors.toList()); // NOSONAR
        log.trace("assignDriver: for trip " + trip.getId() + " found near drivers " + driverSortedList.size());
        if (!driverSortedList.isEmpty()) {
            setShifts(driverSortedList);
            var iterator = driverSortedList.iterator();
            while (iterator.hasNext()) {
                var dto = iterator.next();
                if (dto.getCurrentShift().getEndDate().isBefore(trip.getExpectedStartTime().withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime())) {
                    iterator.remove();
                    continue;
                }
                if (LocalDateTime.now(ZoneOffset.UTC).isAfter(dto.getCurrentShift().getEndDate())) {
                    var driver = driverProvider.get(dto.getCurrentShift().getDriverId()).orElseThrow(() -> new EntityNotFoundException(Driver.class, dto.getCurrentShift().getDriverId()));
                    if (driver.getActiveTripId() == null && !driver.isServing()) {
                        iterator.remove();
                    }
                }
            }
            if (driverSortedList.isEmpty()) {
                log.trace("assignDriver: for trip " + trip.getId() + " no driver shift found");
            } else {
                log.trace("assignDriver: for trip " + trip.getId() + " found matching drivers " + driverSortedList.size());
                // choose one least occupied
                int minUsed = Integer.MAX_VALUE;
                UUID chosenDriverId = null;
                for (DriverShiftDTO driverShiftDTO : driverSortedList) {
                    int num = compluteNumberOfTrips(driverShiftDTO);
                    if (num < minUsed) {
                        minUsed = num;
                        chosenDriverId = driverShiftDTO.getId();
                    }
                }
                log.trace("assignDriver: for trip " + trip.getId() + " chosen driver id " + chosenDriverId);
                var driver = driverProvider.get(chosenDriverId).orElse(null);
                if (driver != null) {
                    var before = driver.getActiveTripId();
                    driver.setActiveTripId(trip.getId());
                    log.debug("ActiveTripId changing! Driver = " + driver.getId() + "; Before = " + before + "; After = " + driver.getActiveTripId() + "; " + new Throwable().getStackTrace()[0].getFileName() + " " + new Throwable().getStackTrace()[0].getLineNumber());
                    driverProvider.save(driver);
                    log.trace("assignDriver: setActiveTripId " + trip.getId() + " to driver id " + driver.getId());
                    return driver;
                }
            }
        }
        return null;
    }

    @Override
    public DriverBusynessDTO getDriverBusyness(Authentication authentication, DriverBusynessRequest driverBusynessRequest) {
        var contractorId = authCheckService.userAuthCheck(driverBusynessRequest.contractorId(), (JwtAuthenticationToken) authentication);
        var contractorDigitId = contractorProvider.getContractorDigitId(contractorId);
        var busynessData = new ArrayList<DriverBusynessDTO.BusynessData>();
        var ordersData = new ArrayList<DriverBusynessDTO.TripData>();

        if (driverBusynessRequest.driverIds() != null && !driverBusynessRequest.driverIds().isEmpty()) {
            var rawData = tripProvider.findAllPlanningDriverBusyness(driverBusynessRequest, contractorId, contractorDigitId);
            if (!driverBusynessRequest.onlyPlanning()) {
                rawData.addAll(tripProvider.findAllDriverBusyness(driverBusynessRequest, contractorId, contractorDigitId));
            }
            rawData.parallelStream()
                    .collect(Collectors.toMap(
                            DriverBusynessDTO.BusynessData::getDriver,
                            DriverBusynessDTO.BusynessData::getTrips,
                            (o1, o2) -> {
                                o1.addAll(o2);
                                return o1;
                            }
                    )).forEach((driverData, tripData) -> busynessData.add(new DriverBusynessDTO.BusynessData(driverData, tripData)));
        }

        if (driverBusynessRequest.includeOrderedVehicles()) {
            ordersData.addAll(tripProvider.findAllOrderedVehicles(driverBusynessRequest, contractorId, contractorDigitId));
        }

        var busynessDataTripMap = busynessData.parallelStream()
                .map(DriverBusynessDTO.BusynessData::getTrips)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(DriverBusynessDTO.TripData::getId, Function.identity()));

        var tripIds = new ArrayList<>(busynessDataTripMap.keySet());

        if (isNotEmpty(tripIds)) {
            checkinProvider.findAllByTripIdsAndStatus(tripIds, TripStatus.DRIVER_ON_THE_WAY)
                    .parallelStream()
                    .forEach(c -> busynessDataTripMap.computeIfPresent(c.getTripId(), (k, v) -> {
                        v.setDriverProcessingTime(c.getTime().toOffsetDateTime());
                        return v;
                    }));
        }

        return new DriverBusynessDTO(busynessData, ordersData);
    }

    @Override
    @Deprecated
    public void liberateDrivers() {
        var tracer = oneAgentSdk.traceCustomService("trips", "driver liberator scheduler");
        tracer.start();
        var driversToLiberate = driverProvider.findDriversToLiberate();
        if (!driversToLiberate.isEmpty()) {
            driversToLiberate.forEach(driver -> {
                var tripId = driver.getActiveTripId();
                var errorMessage = "DRIVER WITH FROZEN TRIP FOUND! DriverId : " + driver.getId() + "; TripId : " + tripId + ";";
                log.error(errorMessage);
                tracer.error(errorMessage);
                driver.setActiveTripId(null);
                driverProvider.save(driver);
                log.trace("Driver " + driver.getId() + " was liberated from trip " + tripId + ";");
            });
        }
        tracer.end();
    }

    private int compluteNumberOfTrips(DriverShiftDTO driverShiftDTO) {
        List<TripStatus> tripStatuses = List.of(
                TripStatus.DRIVER_ASSIGNED,
                TripStatus.DRIVER_ON_THE_WAY,
                TripStatus.INTERMEDIATE_WAYPOINT_ARRIVED,
                TripStatus.DRIVER_ARRIVED,
                TripStatus.TRIP_IN_PROGRESS,
                TripStatus.ORDER_FINISHED
        );
        LocalDateTime minDateTime = LocalDateTime.now().minusHours(24);
        return tripProvider.findAllByDriverIdAndStartTimeAfterAndStatusInOrderByStartTimeDesc(driverShiftDTO.getId(),
                minDateTime, tripStatuses).size();
    }

    private void deactivateShift(Driver driver) {
        if (driver.getShiftId() != null) {
            var shift = shiftProvider.get(driver.getShiftId()).orElseThrow(() ->
                    new EntityNotFoundException(Shift.class, driver.getShiftId()));
            verificationService.checkManualOnlineSwitchAvailable(shift);
            verificationService.checkShiftIsDeleted(shift, ShiftOperationType.EXIT);
            verificationService.checkDriverBusynessForExitFromShift(driver);
            var trips = tripProvider.findAllByDriverAndStatusIn(driver.getId(), List.of(TripStatus.DRIVER_ASSIGNED));
            trips.parallelStream().forEach(trip -> {
                var historyItem = new TripHistoryItem();
                historyItem.setChangeTime(LocalDateTime.now(ZoneOffset.UTC));
                historyItem.setTripId(trip.getId());
                historyItem.setOldDispatcherId(trip.getDispatcherId());
                historyItem.setOldDriverId(trip.getDriverId());
                historyItem.setOldStatus(trip.getStatus());
                historyItem.setActorId(driver.getId());
                historyItem.setActorType(Actor.DRIVER);
                historyItem.setAction(ActionType.DRIVER_CHANGING);

                trip.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT);
                trip.setDriverId(null);
                trip.setVehicleId(null);
                var updatedTrip = updateTripService.updateTrip(null, trip, trip.getStatus());
                tripSender.send(trip, false, ChannelType.BOTH);
                historyItem.setNewDispatcherId(updatedTrip.getDispatcherId());
                historyItem.setNewDriverId(updatedTrip.getDriverId());
                historyItem.setNewStatus(updatedTrip.getStatus());
                tripHistoryProvider.save(historyItem);
            });
            shiftService.deactivate(shift);
        }
    }

    private void checkCanOnline(Driver driver) {
        var shifts = shiftProvider.getShiftByDriverIdAndCurrentDate(driver.getId(), LocalDateTime.now(ZoneOffset.UTC))
                .stream().sorted(Comparator.comparing(Shift::getStartDate)).toList();
        verificationService.checkShiftsExistence(shifts);
        var shift = shifts.get(0);
        verificationService.checkManualOnlineSwitchAvailable(shift);
        verificationService.checkShiftIsDeleted(shift, ShiftOperationType.ENTER);
        shiftService.activate(shift);
        tripService.processPlannedTrips(shift);
    }

    @NotNull
    private UUID getAuthenticated(Authentication authentication) {
        return UUID.fromString(((JwtAuthenticationToken) authentication).getToken().getId());
    }

    private List<DriverShiftDTO> getAllDriversOnlineByContractor(UUID contractorId, int size, int page, String name, UUID autoparkId) {
        var pageRequest = PageRequest.of(page, size);
        var drivers = driverProvider.findAllByContractorIdAndOnline(contractorId, true, pageRequest, name, autoparkId);
        return drivers.stream().map(driverMapper::toDriverShiftDto).collect(Collectors.toList()); //NOSONAR
    }

    private List<DriverShiftDTO> getAllDriversForPlanning(LocalDateTime tripStartTime, LocalDateTime tripEndTime, UUID contractorId, String name, UUID autoparkId) {
        var drivers = driverProvider.getAllDriversByShiftDateIn(tripStartTime, tripEndTime, contractorId, name, autoparkId);
        return drivers.stream().map(driverMapper::toDriverShiftDto).collect(Collectors.toList()); //NOSONAR
    }

    private List<Driver> getDriversAround(UUID contractorId, Double requestLatitude, Double requestLongitude, double radius, String name, UUID autoparkId) {
        log.trace("getDriversAround: radius = " + minRadius + ", timeSecs = " + timeSecs + ", finishSearchMin = " + finishSearchMin);
        if ((requestLatitude == null) || (requestLongitude == null)) {
            return Collections.emptyList();
        }
        var list = driverProvider.findAllByActiveAndOnlineAndContractorId(true, true, contractorId, name, autoparkId);
        log.trace("getDriversAround: found active online drivers: " + getDriverList(list));
        List<Driver> inradiusList = new ArrayList<>();
        for (Driver driver : list) {
            defineInRadiusDriver(requestLatitude, requestLongitude, driver, radius).ifPresent(inradiusList::add);
        }
        log.trace("getDriversAround: found total active online inradius drivers " + inradiusList.size());
        return inradiusList.stream().sorted(Comparator.comparing(Driver::getRating)).toList();
    }

    private String getDriverList(List<Driver> list) {
        return list.stream().map(Driver::getId).map(UUID::toString).collect(Collectors.joining(","));
    }

    private Optional<Driver> defineInRadiusDriver(double requestLatitude, double requestLongitude, Driver driver, double radius) {
        List<TripStatus> tripStatuses = List.of(
                TripStatus.DRIVER_ASSIGNED,
                TripStatus.DRIVER_ON_THE_WAY,
                TripStatus.INTERMEDIATE_WAYPOINT_ARRIVED,
                TripStatus.DRIVER_ARRIVED,
                TripStatus.TRIP_IN_PROGRESS
        );
        log.trace("defineInRadiusDriver: inspecting " + driver.getId()
                + " with ActiveTripId = " + driver.getActiveTripId()
                + ", serving = " + driver.isServing());
        LocalDateTime minDateTime = LocalDateTime.now(ZoneOffset.UTC).minusHours(24);
        List<Trip> tripList = tripProvider.findAllByDriverIdAndStartTimeAfterAndStatusInOrderByStartTimeDesc(driver.getId(),
                minDateTime, tripStatuses);
        List<Trip> stopList = new ArrayList<>();
        if (!tripList.isEmpty()) {
            var now = LocalDateTime.now(ZoneOffset.UTC);
            stopList = tripList.stream().filter(e -> {
                long minutes = ChronoUnit.MINUTES.between(now, e.getExpectedStartTime().withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime());
                return (minutes > 0) && (minutes < reservedTimeMin) || (e.getExpectedStartTime().isBefore(now.atOffset(ZoneOffset.UTC)) && e.getExpectedEndTime().isAfter(now.atOffset(ZoneOffset.UTC)));
            }).collect(Collectors.toList());
            if (!stopList.isEmpty()) {
                for (Trip curTrip : stopList) {
                    log.trace("defineInRadiusDriver: driver declined " + driver.getId()
                            + ": next trip too soon:"
                            + " tripId = " + curTrip.getId()
                            + ", reservedTimeMin =  " + reservedTimeMin
                            + ", now " + now
                            + ", startTime = " + curTrip.getExpectedStartTime()
                            + ", endTime = " + curTrip.getExpectedEndTime());
                }
            }
        }
        if (driver.getActiveTripId() == null && !driver.isServing() && stopList.isEmpty()) {
            log.trace("defineInRadiusDriver: driver " + driver.getId() + " found basically available");
            if (checkDriver(requestLatitude, requestLongitude, driver, radius)) {
                return Optional.of(driver);
            } else {
                log.trace("defineInRadiusDriver: driver " + driver.getId() + " not matched distance with radius " + radius);
            }
        }
        return Optional.empty();
    }

    private boolean checkDriver(double requestLatitude, double requestLongitude, Driver driver, double radius) {
        if (driver.getLatitude() == null || driver.getLongitude() == null) {
            log.warn("checkDriver: for driver " + driver.getId()
                    + ": driver.getLatitude = " + driver.getLatitude()
                    + ", driver.getLongitude() = " + driver.getLongitude());
            return false;
        }
        var now = ZonedDateTime.now(ZoneOffset.UTC);
        boolean insideTime = isInsideTime(driver.getPointTime(), now, timeSecs, driver.getId());
        boolean insideRadius = isWaypointInsideRadius(requestLatitude, requestLongitude,
                driver.getLatitude(),
                driver.getLongitude(),
                radius);
        log.trace("checkDriver: for driver " + driver.getId()
                + ": insideTime = " + insideTime
                + ", insideRadius = " + insideRadius);
        return insideTime && insideRadius;
    }

    private boolean isInsideTime(ZonedDateTime pointTime, ZonedDateTime anchorPoint, int timeSecs, UUID driverId) {
        if (pointTime == null || anchorPoint == null) {
            log.trace("checkDriver: for driver " + driverId
                    + ": pointTime = " + pointTime
                    + ", anchorPoint = " + anchorPoint);
            return false;
        }
        long seconds = ChronoUnit.SECONDS.between(anchorPoint, pointTime);
        boolean result = seconds < timeSecs;
        if (!result) {
            log.trace("checkDriver: for driver " + driverId
                    + ": pointTime = " + pointTime
                    + ", anchorPoint = " + anchorPoint
                    + ", duration secs = " + seconds);
        }
        return result;
    }

    private boolean isWaypointInsideRadius(double requestLatitude,
                                           double requestLongitude,
                                           double driverLatitude,
                                           double driverLongitude,
                                           double includeRadius) {
        double distance = calculateDistanceInKilometer(
                requestLatitude, requestLongitude,
                driverLatitude, driverLongitude);
        // *** здесь сравниваются километры с радиусом
        return distance <= includeRadius;
    }

    private double calculateDistanceInKilometer(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(lat1))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double result = (AVERAGE_RADIUS_OF_EARTH_KM * c);

        BigDecimal bd = BigDecimal.valueOf(result).setScale(3, RoundingMode.HALF_UP); // scale to meters
        return bd.doubleValue();
    }

    private void setShifts(List<DriverShiftDTO> driversAndVehicles) {
        for (var driverDTO : driversAndVehicles) {
            var shift = shiftProvider.get(driverDTO.getShiftId()).orElseThrow(() -> new EntityNotFoundException(Shift.class, driverDTO.getShiftId()));
            var vehicle = vehicleProvider.get(shift.getVehicleId()).orElseThrow(() -> new EntityNotFoundException(Vehicle.class, shift.getVehicleId()));
            driverDTO.setCurrentShift(shiftMapper.toShiftResponseDTO(shift, vehicle));
            driversAndVehicles.set(driversAndVehicles.indexOf(driverDTO), driverDTO);
        }
    }
}
