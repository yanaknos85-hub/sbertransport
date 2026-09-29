package ru.sber.transport.trips.cargo.web.service.impl;

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
import ru.sber.transport.trips.cargo.business.dto.*;
import ru.sber.transport.trips.cargo.business.model.*;
import ru.sber.transport.trips.cargo.business.providers.TripProvider;
import ru.sber.transport.trips.cargo.messaging.ChannelType;
import ru.sber.transport.trips.cargo.messaging.providers.*;
import ru.sber.transport.trips.cargo.messaging.senders.dispatcher.DriverSender;
import ru.sber.transport.trips.cargo.messaging.senders.dispatcher.TripSender;
import ru.sber.transport.trips.cargo.providers.checkin.CheckinProvider;
import ru.sber.transport.trips.cargo.providers.checkin.mapper.CheckinMapper;
import ru.sber.transport.trips.cargo.providers.driver.mapper.DriverMapper;
import ru.sber.transport.trips.cargo.providers.shift.mapper.ShiftMapper;
import ru.sber.transport.trips.cargo.web.service.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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

    private final VehicleProvider vehicleProvider;

    private final TripSender tripSender;

    private final TripService tripService;

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
        if(driver.getActiveTripId()==null && tripId != null){
            var currentTrip = tripProvider.get(tripId)
                    .orElseThrow(() -> new EntityNotFoundException(Trip.class, tripId));
            var hrid = "%s-%04d-%08d".formatted(Prefix.TC,
                    contractorProvider.getContractorDigitId(currentTrip.getContractorId()),
                    currentTrip.getDigitId());
            verificationService.checkIsTripCancelled(currentTrip, hrid);
            verificationService.checkDriverToTripRelation(driverProvider.get(currentTrip.getDriverId()).orElse(null), driver);
        }
    }

    @Override
    public void lastPointInfo(GeoWaypointDTO geoWaypointDTO, Driver driver) {
        driver.setTimeZone(geoWaypointDTO.getTimeZone());
        driver.setPointTime(ZonedDateTime.now(ZoneOffset.UTC));
        driver.setLatitude(geoWaypointDTO.getLatitude());
        driver.setLongitude(geoWaypointDTO.getLongitude());
        driver.setAzimuth(geoWaypointDTO.getAzimuth());
        driverProvider.save(driver);
        driverSender.send(driver);
    }

    @Override
    public CheckinResponseDTO getTripCheckins(Driver driver, UUID tripId) {
        var trip = tripProvider.get(tripId).orElseThrow(() -> new EntityNotFoundException(Trip.class, tripId));
        verificationService.checkDriverToTripRelation(driverProvider.get(trip.getDriverId()).orElse(null), driver);
        var checkins = checkinProvider.findAllByTripId(tripId)
                .stream().map(checkinMapper::toDto)
                .sorted(Comparator.comparing(CheckinDTO::getTime))
                .toList();
        verificationService.checkCheckinsExistence(checkins, trip.getDigitId());
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
        if(driverLocationSearchDTO.isForPlanning()){
            driverDTO = getAllDriversForPlanning(driverLocationSearchDTO.getDeadline(), contractorId, driverLocationSearchDTO.getAutoparkId());
        } else {
            if (driverLocationSearchDTO.isFullSearch()) {
                driverDTO = getAllDriversOnlineByContractor(contractorId,  driverLocationSearchDTO.getAutoparkId(), driverLocationSearchDTO.getSize(), driverLocationSearchDTO.getPage());
            } else {
                driverDTO = getDriversAround(contractorId, driverLocationSearchDTO.getAutoparkId(), driverLocationSearchDTO.getLatitude(),
                        driverLocationSearchDTO.getLongitude(), minRadius).stream().map(driverMapper::toDriverShiftDto).collect(Collectors.toList()); // NOSONAR
            }
        }
        setShifts(driverDTO);
        var iterator = driverDTO.iterator();
        while (iterator.hasNext() && !driverLocationSearchDTO.isForPlanning()) {
            var dto = iterator.next();
            if (driverLocationSearchDTO.getDeadline() != null
                    && dto.getCurrentShift().getEndDate().isBefore(driverLocationSearchDTO.getDeadline())
                    && (!driverLocationSearchDTO.isFullSearch()||(driverLocationSearchDTO.isFullSearch()
                    && driverLocationSearchDTO.isEnableShiftFilter()))) {
                iterator.remove();
                continue;
            }
            if(LocalDateTime.now(ZoneOffset.UTC).isAfter(dto.getCurrentShift().getEndDate())){
                var driver = driverProvider.get(dto.getCurrentShift().getDriverId()).orElseThrow(() -> new EntityNotFoundException(Driver.class, dto.getCurrentShift().getDriverId()));
                if(driver.getActiveTripId()==null&&!driver.isServing()) {
                    iterator.remove();
                    continue;
                }
            }
            if (dto.getLatitude()!=null&&dto.getLongitude()!=null) {
                dto.setDistanceInKilometer(calculateDistanceInKilometer(driverLocationSearchDTO.getLatitude(),
                        driverLocationSearchDTO.getLongitude(), dto.getLatitude(), dto.getLongitude()));
            } else {
                log.info("Расстояние от местоположения водителя "+dto.getHumanReadableId()+" до точки выполнения заказа не будет рассчитано из-за отсутствия данных по координатам водителя");
            }
        }
        var driversWithoutLocationData = driverDTO.stream().filter(driver -> driver.getDistanceInKilometer() == null).toList(); // NOSONAR
        if(!driversWithoutLocationData.isEmpty()){
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
                getDriversAround(trip.getContractorId(), trip.getAutoparkId(), requestLatitude, requestLongitude, radius).stream()
                        .map(driverMapper::toDriverShiftDto)
                        .collect(Collectors.toList()); // NOSONAR
        log.trace("assignDriver: for trip " + trip.getId() + " found near drivers " + driverSortedList.size());
        if (!driverSortedList.isEmpty()) {
            setShifts(driverSortedList);
            var iterator = driverSortedList.iterator();
            while(iterator.hasNext()){
                var dto = iterator.next();
                if(dto.getCurrentShift().getEndDate().isBefore(trip.getStartTime().withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime())){
                    iterator.remove();
                    continue;
                }
                if(LocalDateTime.now(ZoneOffset.UTC).isAfter(dto.getCurrentShift().getEndDate())){
                    var driver = driverProvider.get(dto.getCurrentShift().getDriverId()).orElseThrow(() -> new EntityNotFoundException(Driver.class, dto.getCurrentShift().getDriverId()));
                    if(driver.getActiveTripId()==null&&!driver.isServing()) {
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
                    driver.setActiveTripId(trip.getId());
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

        if (driverBusynessRequest.driverIds() != null && !driverBusynessRequest.driverIds().isEmpty()) {
            var rawData = tripProvider.findAllDriverBusyness(driverBusynessRequest, contractorId, contractorDigitId);
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

        return new DriverBusynessDTO(busynessData);
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
        LocalDateTime minDateTime = LocalDateTime.now(ZoneOffset.UTC).minusHours(24);
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
                trip.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT);
                trip.setDriverId(null);
                trip.setVehicleId(null);
                updateTripService.updateTrip(null, trip, trip.getStatus());
                tripSender.send(trip, false, ChannelType.BOTH);
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

    private List<DriverShiftDTO> getAllDriversOnlineByContractor(UUID contractorId,  UUID autoparkId, int size, int page) {
        var pageRequest = PageRequest.of(page, size);
        var drivers = driverProvider.findAllByContractorIdAndOnline(contractorId, autoparkId, true, pageRequest);
        return drivers.stream().map(driverMapper::toDriverShiftDto).collect(Collectors.toList()); //NOSONAR
    }

    private List<Driver> getDriversAround(UUID contractorId, UUID autoparkId, Double requestLatitude, Double requestLongitude, double radius) {
        log.trace("getDriversAround: radius = " + minRadius + ", timeSecs = " + timeSecs + ", finishSearchMin = " + finishSearchMin);
        if ((requestLatitude == null) || (requestLongitude == null)) {
            return Collections.emptyList();
        }
        List<Driver> list = driverProvider.findAllByActiveAndOnlineAndContractorIdAndAutoparkId(true, true, contractorId, autoparkId);

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
            var now = OffsetDateTime.of(LocalDateTime.now(ZoneOffset.UTC), ZoneOffset.UTC);
            stopList = tripList.stream().filter(e -> {
                long minutes = ChronoUnit.MINUTES.between(now, e.getStartTime());
                return (minutes > 0) && (minutes < reservedTimeMin);
            }).collect(Collectors.toList());
            if (!stopList.isEmpty()) {
                for (Trip curTrip : stopList) {
                    log.trace("defineInRadiusDriver: driver declined " + driver.getId()
                            + ": next trip too soon:"
                            + " tripId = " + curTrip.getId()
                            + ", reservedTimeMin =  " + reservedTimeMin
                            + ", now " + now
                            + ", startTime = " + curTrip.getStartTime());
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

    private List<DriverShiftDTO> getAllDriversForPlanning(LocalDateTime tripStartTime, UUID contractorId, UUID autoparkId) {
        var drivers = driverProvider.getAllDriversByShiftDateIn(tripStartTime, contractorId, autoparkId);
        return drivers.stream().map(driverMapper::toDriverShiftDto).collect(Collectors.toList()); //NOSONAR
    }
}
