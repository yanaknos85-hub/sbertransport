package ru.sber.transport.trip.web.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.trip.business.dto.*;
import ru.sber.transport.trip.business.dto.v2.CheckinDtoV2;
import ru.sber.transport.trip.business.dto.v2.CheckinResponseDtoV2;
import ru.sber.transport.trip.business.dto.v2.DriverLocationDTO;
import ru.sber.transport.trip.business.model.*;
import ru.sber.transport.trip.business.providers.TripProvider;
import ru.sber.transport.trip.exceptions.FieldException;
import ru.sber.transport.trip.exceptions.FieldsException;
import ru.sber.transport.trip.messaging.ChannelType;
import ru.sber.transport.trip.messaging.providers.*;
import ru.sber.transport.trip.messaging.senders.dispatcher.DriverSender;
import ru.sber.transport.trip.messaging.senders.dispatcher.ShiftSender;
import ru.sber.transport.trip.messaging.senders.dispatcher.TripSender;
import ru.sber.transport.trip.providers.checkin.CheckinProvider;
import ru.sber.transport.trip.providers.checkin.mapper.CheckinMapper;
import ru.sber.transport.trip.providers.history.trip.TripHistoryProvider;
import ru.sber.transport.trip.web.exceptions.ConflictException;
import ru.sber.transport.trip.web.exceptions.DataConflictException;
import ru.sber.transport.trip.business.dto.TripAssignStatisticDto;
import ru.sber.transport.trip.web.service.*;

import java.io.Serializable;
import java.time.*;
import java.util.*;

import static ru.sber.transport.trip.business.model.TripStatus.ORDER_FINISHED;

//TODO: Полностью зарефакторить класс
@Slf4j
@RequiredArgsConstructor
@Component
@Transactional
public class TripServiceImpl implements TripService {

    private final TripProvider tripProvider;

    private final DriverProvider driverProvider;

    private final DriverSender driverSender;

    private final ContractorProvider contractorProvider;

    private final DispatcherProvider dispatcherProvider;

    private final ShiftProvider shiftProvider;

    private final ShiftSender shiftSender;

    private final TripHistoryProvider tripHistoryProvider;

    private final CheckinProvider checkinProvider;

    private final UpdateTripService updateTripService;

    private final VerificationService verificationService;

    private final DispatcherStatusChangingService dispatcherStatusChangingService;

    private final List<TripSender> tripSenders;

    private final CheckinMapper checkinMapper;

    private final AuthCheckService authCheckService;

    private final VehicleProvider vehicleProvider;

    @Value("${conflict.changing-state-of-finished-trip.ignore}")
    private boolean ignoreConflictChangingStateOfFinishedTrip;

    private static final String EXPECTED_VEHICLE_ID = "expectedVehicleId";

    private static final String PLANNING_SHIFT_ID = "planningShiftId";

    private static final String DRIVER_ID_FIELD = "driverId";

    private static final String STATUS_FIELD = "status";

    private static final String CHANGED_BY_DRIVER_FIELD = "changedByDriver";

    private static final String LATITUDE_FIELD = "latitude";

    private static final String LONGITUDE_FIELD = "longitude";

    private static final String AZIMUTH = "azimuth";

    private static final String TIMEZONE_FIELD = "timezone";

    private static final String CHECKIN_TYPE_FIELD = "type";

    private static final String DATE_TIME_FIELD = "dateTime";

    private static final String FACT_DISTANCE_FIELD = "factDistance";

    private static final String DRIVER_WAITING_TIME_FIELD = "driverWaitingTime";

    private static final String FACT_COST_FIELD = "factCost";

    private static final String DISPATCHER_TAKE_TO_WORK_FIELD = "dispatcherTakeToWork";

    // Сопоставление статуса (ключа) и возможных переходов в этот статус из какого-либо (значения)
    private final Map<TripStatus, List<TripStatus>> possibleStatusesChanges = Map.of(
            TripStatus.DRIVER_ASSIGNED, List.of(),
            TripStatus.DRIVER_ON_THE_WAY, List.of(TripStatus.DRIVER_ASSIGNED),
            TripStatus.DRIVER_ARRIVED, List.of(TripStatus.DRIVER_ON_THE_WAY),
            TripStatus.TRIP_IN_PROGRESS, List.of(TripStatus.DRIVER_ARRIVED, TripStatus.INTERMEDIATE_WAYPOINT_ARRIVED),
            TripStatus.INTERMEDIATE_WAYPOINT_ARRIVED, List.of(TripStatus.TRIP_IN_PROGRESS),
            TripStatus.ORDER_FINISHED, List.of(TripStatus.TRIP_IN_PROGRESS),
            TripStatus.ORDER_CANCELLED_BY_CLIENT, List.of(),
            TripStatus.ORDER_CANCELLED_BY_DRIVER, List.of(),
            TripStatus.ORDER_EXPIRED, List.of());

    @Override
    public Optional<Trip> get(UUID tripId) {
        return tripProvider.get(tripId);
    }

    @Override
    public void update(UUID contractorId, UUID tripId, Map<String, Serializable> patchData, JwtAuthenticationToken authentication) throws FieldsException {
        var userId = UUID.fromString(authentication.getToken().getId());
        var trip = tripProvider.findByContractorIdAndId(contractorId, tripId).orElseThrow(() -> new EntityNotFoundException(Trip.class, tripId));
        var channels = new ArrayList<>(List.of(ChannelType.WEB_SOCKET));
        if (patchData.containsKey(CHANGED_BY_DRIVER_FIELD) && Boolean.valueOf(String.valueOf(patchData.get(CHANGED_BY_DRIVER_FIELD))).equals(true)) {
            log.info("Изменение поездки TR-%04d-%08d водителем: login = %s; id = %s;".formatted(contractorProvider.getContractorDigitId(contractorId), trip.getDigitId(), authentication.getToken().getSubject(), userId));
            updateByDriver(userId, trip, patchData);
        } else {
            log.info("Изменение поездки TR-%04d-%08d диспетчером: login = %s; id = %s;".formatted(contractorProvider.getContractorDigitId(contractorId), trip.getDigitId(), authentication.getToken().getSubject(), userId));
            var dispatcher = authCheckService.dispatcherAuthCheck(contractorId, authentication);
            var historyItems = new ArrayList<TripHistoryItem>();
            try {
                var exceptions = new LinkedList<FieldException>();
                if (patchData.containsKey(DRIVER_ID_FIELD) && patchData.containsKey(STATUS_FIELD)) {
                    patchData.remove(STATUS_FIELD);
                    log.warn("При назначении водителя на поездку статус поездки сменится автоматически, значение поля " + STATUS_FIELD + " не будет учтено");
                }
                if (patchData.containsKey(PLANNING_SHIFT_ID) && patchData.size() > 1) {
                    patchData.entrySet().removeIf(entry -> !entry.getKey().equals(PLANNING_SHIFT_ID));
                    log.warn("При планировании водителя на поездку больше никаких изменений сделать нельзя");
                }
                verificationService.checkDriverAssigningCorrectness(patchData);
                for (var entry : patchData.entrySet()) {
                    var exception = switch (entry.getKey()) {
                        case DRIVER_ID_FIELD ->
                                updateDriver(contractorId, trip, entry.getValue(), historyItems,
                                        getIdDispatcher(dispatcher, userId),
                                        dispatcher == null ? Actor.ADMIN : Actor.DISPATCHER);
                        case STATUS_FIELD -> updateStatus(trip, entry.getValue(), historyItems,
                                getIdDispatcher(dispatcher, userId),
                                setActorByDispatcher(dispatcher));
                        case FACT_DISTANCE_FIELD ->
                                updateFactDistance(trip, entry.getValue(), historyItems,
                                        getIdDispatcher(dispatcher, userId),
                                        setActorByDispatcher(dispatcher));
                        case DRIVER_WAITING_TIME_FIELD ->
                                updateDriverWaitingTime(trip, entry.getValue(), historyItems,
                                        getIdDispatcher(dispatcher, userId),
                                        setActorByDispatcher(dispatcher));
                        case PLANNING_SHIFT_ID ->
                                tripPlanning(trip, entry.getValue(), historyItems,
                                        getIdDispatcher(dispatcher, userId),
                                        setActorByDispatcher(dispatcher));
                        case EXPECTED_VEHICLE_ID ->
                                expectedVehicleChanging(trip, entry.getValue(), historyItems,
                                        getIdDispatcher(dispatcher, userId),
                                        setActorByDispatcher(dispatcher));
                        case FACT_COST_FIELD ->
                                factCost(trip, entry.getValue(), historyItems,
                                        getIdDispatcher(dispatcher, userId),
                                        setActorByDispatcher(dispatcher));
                        case DISPATCHER_TAKE_TO_WORK_FIELD ->
                            createDispatcherTakeToWorkEvent(trip, dispatcher, historyItems, entry.getValue());
                        default -> notSupported(entry);
                    };
                    if (exception != null) {
                        exceptions.add(exception);
                    }
                }
                if (!exceptions.isEmpty()) {
                    throw new FieldsException(exceptions);
                }
            } finally {
                trip.setReportCreated(false);
                trip.setDispatcherId(dispatcher == null ? null : dispatcher.getId());
                var savedTrip = updateTripService.updateTrip(driverProvider.get(trip.getDriverId()).orElse(null), trip, trip.getStatus());
                if (!Objects.equals(savedTrip.getDriverId(), trip.getDriverId()) || Objects.equals(savedTrip.getDispatcherId(), trip.getDispatcherId())) {
                    channels.add(ChannelType.KAFKA);
                }
                savedTrip.setDriverId(trip.getDriverId());
                savedTrip.setDispatcherId(trip.getDispatcherId());
                tripSenders.forEach(sender -> sender.send(savedTrip, false, channels.toArray(ChannelType[]::new)));
                historyItems.stream()
                        .sorted(Comparator.comparing(TripHistoryItem::getChangeTime))
                        .forEach(tripHistoryItem -> {
                            tripHistoryItem.setNewDispatcherId(dispatcher == null ? null : dispatcher.getId());
                            tripHistoryProvider.save(tripHistoryItem);
                        });
            }
        }
    }

    @Override
    public TripAssignStatisticDto getAssignStatistic(UUID contractorId, UUID autoparkId) {
        var assignCountStatuses = List.of(TripStatus.DRIVER_ASSIGNED, TripStatus.DRIVER_ON_THE_WAY,
                TripStatus.DRIVER_ARRIVED, TripStatus.TRIP_IN_PROGRESS, TripStatus.INTERMEDIATE_WAYPOINT_ARRIVED);
        var noAssignCountStatuses = List.of(TripStatus.SENT_TO_CONTRACTOR, TripStatus.WAITING_FOR_ASSIGNMENT);

        var assignCount = tripProvider.countByContractorIdAndStatusIn(contractorId, assignCountStatuses, autoparkId);
        var notAssignCount = tripProvider.countByContractorIdAndStatusIn(contractorId, noAssignCountStatuses, autoparkId);
        var totalCount = assignCount + notAssignCount;

        return new TripAssignStatisticDto(totalCount, assignCount, notAssignCount);
    }

    @Override
    public Iterable<Trip> getAll(UUID contractorId, RequestSearchDto searchData, List<TripStatus> statuses, UUID authenticatedUserId) {
        return tripProvider.findAll(contractorId, searchData, statuses);
    }

    @Override
    public CheckinResponseDtoV2 getTripCheckins(UUID contractorId, UUID tripId) {
        var trip = tripProvider.get(tripId).orElseThrow(() -> new EntityNotFoundException(Trip.class, tripId));
        var checkins = checkinProvider.findAllByTripId(tripId)
                .stream().map(checkinMapper::toDtoV2)
                .sorted(Comparator.comparing(CheckinDtoV2::getTime))
                .toList();
        var checkinResponse = new CheckinResponseDtoV2();
        if (!checkins.isEmpty()) {
            checkinResponse.setChekins(checkins);
            var duration = Duration.between(checkins.get(0).getTime(), checkins.get(checkins.size() - 1).getTime());
            checkinResponse.setTripDuration(
                    new TripDurationDTO(
                            duration.toDaysPart(),
                            duration.toHoursPart(),
                            duration.toMinutesPart(),
                            duration.toSecondsPart())
            );
        } else {
            checkinResponse.setChekins(Collections.emptyList());
            checkinResponse.setTripDuration(null);
        }
        checkinResponse.setComplete(trip.getStatus().isTerminal());
        var driver = driverProvider.get(trip.getDriverId()).orElse(null);
        if (driver != null) {
            var driverLocation = new DriverLocationDTO(driver.getLongitude(), driver.getLatitude());
            checkinResponse.setDriverLocation(driverLocation);
        }
        return checkinResponse;
    }

    @Override
    public void processPlannedTrips(Shift shift) {
        var trips = tripProvider.findAllByPlannedShiftId(shift.getId());
        trips.forEach(trip -> {
            var tripHistoryItem = getNewTripHistoryItem(trip, shift.getDriverId());
            trip.setStatus(TripStatus.DRIVER_ASSIGNED);
            trip.setDriverId(shift.getDriverId());
            trip.setVehicleId(shift.getVehicleId());
            trip.setPlannedShiftId(null);
            updateTripService.updateTrip(driverProvider.get(trip.getDriverId()).orElse(null), trip, trip.getStatus());
            tripSenders.forEach(sender -> sender.send(trip, false, ChannelType.BOTH));
            tripHistoryProvider.save(tripHistoryItem);
        });
    }

    private FieldException updateStatus(Trip trip, Serializable value, ArrayList<TripHistoryItem> historyItems, UUID dispatcherId, Actor actor) {
        TripStatus status;
        var historyItem = getNewTripHistoryItem(trip, actor, dispatcherId);
        historyItem.setAction(ActionType.STATUS_CHANGING);
        try {
            status = TripStatus.valueOf(String.valueOf(value));
        } catch (IllegalArgumentException e) {
            return new FieldException(STATUS_FIELD, value, TripStatus.class);
        }
        verificationService.checkImpossibleStatus(List.of(TripStatus.WAITING_FOR_ASSIGNMENT, TripStatus.SENT_TO_CONTRACTOR), status);
        verificationService.checkEndStatus(trip, status);
        trip = dispatcherStatusChangingService.processStatusChangingByDispatcher(trip, status);
        historyItem.setNewDriverId(trip.getDriverId());
        historyItem.setNewStatus(trip.getStatus());
        historyItems.add(historyItem);
        log.info("Поездке TR-%04d-%08d присвоен статус %s".formatted(contractorProvider.getContractorDigitId(trip.getContractorId()), trip.getDigitId(), trip.getStatus()));
        return null;
    }

    private FieldException notSupported(Map.Entry<String, Serializable> entry) {
        log.info("Updating field %s is not supported".formatted(entry.getKey()));
        return null;
    }

    private FieldException updateDriver(UUID contractorId, Trip trip, Serializable value, ArrayList<TripHistoryItem> historyItems, UUID dispatcherId, Actor actor) {
        verificationService.checkDriverSwitchIsAvailable(trip);
        UUID driverId;
        try {
            var historyItem = getNewTripHistoryItem(trip, actor, dispatcherId);
            historyItem.setAction(ActionType.DRIVER_CHANGING);
            driverId = UUID.fromString(String.valueOf(value));
            var driver = driverProvider.findByContractorIdAndId(contractorId, driverId).orElseThrow(() -> new EntityNotFoundException(Driver.class, driverId));
            if (!driver.isOnline()) {
                throw new DataConflictException(Driver.class, DataConflictException.ConflictType.DRIVER_MUST_BE_ONLINE, false, "online", true);
            }
            if (trip.getAutoparkId() != null && driver.getAutoparkId() != null && !trip.getAutoparkId().equals(driver.getAutoparkId())) {
                throw new ConflictException("Поездка и водитель относятся к разным филиалам автопарка!");
            }
            var shift = shiftProvider.get(driver.getShiftId()).orElseThrow(() -> new EntityNotFoundException(Shift.class, driver.getShiftId()));
            verificationService.checkShiftEndDateToTripStartTimeRelation(shift, trip);
            if (trip.getDriverId() != null && !driver.getId().equals(trip.getDriverId())) {
                var pastDriver = driverProvider.get(trip.getDriverId()).orElseThrow(() -> new EntityNotFoundException(Driver.class, trip.getDriverId()));
                pastDriver.setServing(false);
                var before = pastDriver.getActiveTripId();
                pastDriver.setActiveTripId(null);
                log.debug("ActiveTripId changing! Driver = " + driver.getId() + "; Before = " + before + "; After = " + driver.getActiveTripId() + "; " + new Throwable().getStackTrace()[0].getFileName() + " " + new Throwable().getStackTrace()[0].getLineNumber());
                driverProvider.save(pastDriver);
                driverSender.send(pastDriver);
            }
            trip.setDriverId(driver.getId());
            trip.setVehicleId(shift.getVehicleId());
            trip.setStatus(TripStatus.DRIVER_ASSIGNED);
            trip.setPlannedShiftId(null);
            driverProvider.save(driver);
            driverSender.send(driver);
            historyItem.setNewDriverId(trip.getDriverId());
            historyItem.setNewStatus(trip.getStatus());
            historyItems.add(historyItem);
            log.info("Водитель %s %s %s назначен на поездку TR-%04d-%08d".formatted(driver.getLastName(), driver.getFirstName(), driver.getPatronymic(), contractorProvider.getContractorDigitId(trip.getContractorId()), trip.getDigitId()));
            return null;
        } catch (IllegalArgumentException e) {
            return new FieldException(DRIVER_ID_FIELD, value, UUID.class);
        }
    }

    private void updateByDriver(UUID userId, Trip trip, Map<String, Serializable> patchData) {
        var driver = driverProvider.get(userId)
                .orElseGet(() -> driverProvider.getByOauthId(userId)
                        .orElseThrow(() -> new EntityNotFoundException(Driver.class, userId)));
        verificationService.checkIsDriverOnline(driver);
        var shift = shiftProvider.get(driver.getShiftId()).orElseThrow(() -> new EntityNotFoundException(Shift.class, driver.getShiftId()));
        var status = TripStatus.valueOf(String.valueOf(patchData.get(STATUS_FIELD)));
        var historyItem = getNewTripHistoryItem(trip, Actor.DRIVER, null);
        //TODO: Выпилить данный фунционал после устранения проблем на МП Водителя с зависанием старых поездок
        if (ignoreConflictChangingStateOfFinishedTrip) {
            try {
                verificationService.checkEndStatus(trip, status);
            } catch (ConflictException conflictException) {
                log.error("Нельзя изменить статус поездки на " + status + ", так как поездка находится в конечном статусе");
                historyItem.setNewDispatcherId(trip.getDispatcherId());
                historyItem.setAction(ActionType.INVALID_STATUS_CHANGING_ATTEMPT);
                historyItem.setNewDriverId(trip.getDriverId());
                historyItem.setNewStatus(trip.getStatus());
                tripHistoryProvider.save(historyItem);
                return;
            }
        } else {
            verificationService.checkEndStatus(trip, status);
        }
        //END
        dispatcherStatusChangingService.validateUpdate(trip, driver);
        historyItem.setNewDispatcherId(trip.getDispatcherId());
        historyItem.setAction(ActionType.STATUS_CHANGING);

        verificationService.checkPossibleStatus(possibleStatusesChanges.get(status), trip.getStatus());

        if (TripStatus.DRIVER_ARRIVED.equals(status)) {
            setDriverArrived(driver, trip);
        } else if (TripStatus.TRIP_IN_PROGRESS.equals(status)) {
            setServingRequest(driver, true, trip);
        } else if (ORDER_FINISHED.equals(status)) {
            doFinish(trip, driver, shift);
        } else {
            otherUpdate(trip, driver, shift, status);
        }
        historyItem.setNewDriverId(trip.getDriverId());
        historyItem.setNewStatus(trip.getStatus());
        tripHistoryProvider.save(historyItem);
        processCheckin(trip, patchData, driver, status);
    }

    private void setServingRequest(Driver driver, boolean state, Trip trip) {
        driver.setServing(state);
        if (!state) {
            trip.setFactEndTime(OffsetDateTime.now(ZoneOffset.UTC));
            trip.setStatus(ORDER_FINISHED);
            var before = driver.getActiveTripId();
            driver.setActiveTripId(null);
            log.debug("ActiveTripId changing! Driver = " + driver.getId() + "; Before = " + before + "; After = " + driver.getActiveTripId() + "; " + new Throwable().getStackTrace()[0].getFileName() + " " + new Throwable().getStackTrace()[0].getLineNumber());
        } else {
            if(trip.getFactStartTime() == null){
                trip.setFactStartTime(OffsetDateTime.now(ZoneOffset.UTC));
            }
            trip.setStatus(TripStatus.TRIP_IN_PROGRESS);
        }
        driverProvider.save(driver);
        var savedDriver = driverProvider.get(driver.getId()).orElse(null);
        var savedTrip = updateTripService.updateTrip(savedDriver, trip, trip.getStatus());
        tripSenders.forEach(sender -> sender.send(savedTrip, false, ChannelType.BOTH));
    }

    private void setDriverArrived(Driver driver, Trip trip) {
        trip.setArrivedDate(OffsetDateTime.now(ZoneOffset.UTC));
        var savedTrip = updateTripService.updateTrip(driver, trip, TripStatus.DRIVER_ARRIVED);
        tripSenders.forEach(sender -> sender.send(savedTrip, false, ChannelType.BOTH));
    }

    private void doFinish(Trip trip, Driver driver, Shift shift) {
        setServingRequest(driver, false, trip);
        if (LocalDateTime.now(ZoneOffset.UTC).isAfter(shift.getEndDate())) {
            shift.setActive(false);
            shiftProvider.save(shift);
            shiftSender.send(shift);
            driver.setOnline(false);
            driver.setShiftId(null);
            driverProvider.save(driver);
        }
    }

    private void otherUpdate(Trip trip, Driver driver, Shift shift, TripStatus status) {
        if (TripStatus.ORDER_CANCELLED_BY_CLIENT.equals(status) ||
                TripStatus.ORDER_CANCELLED_BY_DRIVER.equals(status) ||
                TripStatus.ORDER_EXPIRED.equals(status)) {
            driver.setServing(false);
            var before = driver.getActiveTripId();
            driver.setActiveTripId(null);
            log.debug("ActiveTripId changing! Driver = " + driver.getId() + "; Before = " + before + "; After = " + driver.getActiveTripId() + "; " + new Throwable().getStackTrace()[0].getFileName() + " " + new Throwable().getStackTrace()[0].getLineNumber());
            if (LocalDateTime.now(ZoneOffset.UTC).isAfter(shift.getEndDate())) {
                shift.setActive(false);
                shiftProvider.save(shift);
                shiftSender.send(shift);
                driver.setOnline(false);
                driver.setShiftId(null);
            }
            driverProvider.save(driver);
        }
        var savedTrip = updateTripService.updateTrip(driver, trip, status);
        if (TripStatus.INTERMEDIATE_WAYPOINT_ARRIVED == savedTrip.getStatus()) {
            tripSenders.forEach(sender -> sender.send(savedTrip, false, ChannelType.WEB_SOCKET));
        } else {
            tripSenders.forEach(sender -> sender.send(savedTrip, false, ChannelType.BOTH));
        }
    }

    private void processCheckin(Trip trip, Map<String, Serializable> patchData, Driver driver, TripStatus status) {
        var checkin = new Checkin();
        checkin.setId(UUID.randomUUID());
        checkin.setLatitude(Double.parseDouble(String.valueOf(patchData.get(LATITUDE_FIELD))));
        checkin.setLongitude(Double.parseDouble(String.valueOf(patchData.get(LONGITUDE_FIELD))));
        checkin.setStatus(status);
        checkin.setType(CheckinType.valueOf(String.valueOf(patchData.get(CHECKIN_TYPE_FIELD))));
        checkin.setTripId(trip.getId());
        if (patchData.containsKey(DATE_TIME_FIELD)) {
            checkin.setTime(ZonedDateTime.parse(String.valueOf(patchData.get(DATE_TIME_FIELD))));
        } else {
            checkin.setTime(ZonedDateTime.now(ZoneOffset.UTC));
        }
        checkin.setTimeZone(String.valueOf(patchData.get(TIMEZONE_FIELD)));
        checkinProvider.save(checkin);
        var azimuthObject = patchData.get(AZIMUTH);
        var azimuth = azimuthObject == null ? 0 : Double.parseDouble(String.valueOf(azimuthObject));
        var savedCheckin = checkinProvider.get(checkin.getId());
        savedCheckin.ifPresent(value -> lastPointInfo(new GeoWaypointDTO(value.getLatitude(), value.getLongitude(), value.getTimeZone(), null, azimuth), driver));
    }

    private void lastPointInfo(GeoWaypointDTO geoWaypointDTO, Driver driver) {
        driver.setTimeZone(geoWaypointDTO.getTimeZone());
        driver.setPointTime(ZonedDateTime.now(ZoneOffset.UTC));
        driver.setLatitude(geoWaypointDTO.getLatitude());
        driver.setLongitude(geoWaypointDTO.getLongitude());
        driverProvider.save(driver);
        driverSender.send(driver);
    }

    private FieldException updateFactDistance(Trip trip, Serializable entry, ArrayList<TripHistoryItem> historyItems, UUID dispatcherId, Actor actor) {
        try {
            var historyItem = getNewTripHistoryItem(trip, actor, dispatcherId);
            historyItem.setNewDriverId(trip.getDriverId());
            historyItem.setNewStatus(trip.getStatus());
            historyItem.setAction(ActionType.FACT_DATA_CHANGING);
            historyItems.add(historyItem);
            var factDistance = Double.parseDouble(String.valueOf(entry));
            trip.setFactDistance(factDistance);
        } catch (IllegalArgumentException e) {
            return new FieldException(FACT_DISTANCE_FIELD, entry, UUID.class);
        }
        return null;
    }

    private FieldException updateDriverWaitingTime(Trip trip, Serializable entry, ArrayList<TripHistoryItem> historyItems, UUID dispatcherId, Actor actor) {
        try {
            var historyItem = getNewTripHistoryItem(trip, actor, dispatcherId);
            historyItem.setNewDriverId(trip.getDriverId());
            historyItem.setNewStatus(trip.getStatus());
            historyItem.setAction(ActionType.FACT_DATA_CHANGING);
            historyItems.add(historyItem);
            var driverWaitingTime = Duration.ofMillis(Long.parseLong(String.valueOf(entry)));
            trip.setDriverWaitingTime(driverWaitingTime);
        } catch (IllegalArgumentException e) {
            return new FieldException(DRIVER_WAITING_TIME_FIELD, entry, UUID.class);
        }
        return null;
    }

    private FieldException tripPlanning(Trip trip, Serializable entry, ArrayList<TripHistoryItem> historyItems, UUID dispatcherId, Actor actor) {
        try {
            verificationService.checkPlanningIsAvailable(trip.getStatus());
            var shift = shiftProvider.get(UUID.fromString(String.valueOf(entry)))
                    .orElseThrow(() -> new EntityNotFoundException(Shift.class, UUID.fromString(String.valueOf(entry))));
            verificationService.checkShiftIsDeleted(shift, ShiftOperationType.TRIP_PLANNING);
            verificationService.checkShiftEndDateToTripStartTimeRelation(shift, trip);
            var historyItem = getNewTripHistoryItem(trip, actor, dispatcherId);
            historyItem.setNewStatus(trip.getStatus());
            historyItem.setNewPlannedShiftId(shift.getId());
            historyItem.setAction(ActionType.TRIP_PLANNING);
            historyItems.add(historyItem);
            trip.setPlannedShiftId(shift.getId());
        } catch (IllegalArgumentException e) {
            return new FieldException(PLANNING_SHIFT_ID, entry, UUID.class);
        }
        return null;
    }

    private FieldException expectedVehicleChanging(Trip trip, Serializable entry, ArrayList<TripHistoryItem> historyItems, UUID dispatcherId, Actor actor) {
        try {
            var vehicle = vehicleProvider.getByIdAndContractorId(UUID.fromString(String.valueOf(entry)), trip.getContractorId())
                    .orElseThrow(() -> new EntityNotFoundException(Vehicle.class, UUID.fromString(String.valueOf(entry))));
            var historyItem = getNewTripHistoryItem(trip, actor, dispatcherId);
            historyItem.setNewStatus(trip.getStatus());
            historyItem.setNewExpectedVehicleId(vehicle.getId());
            historyItem.setAction(ActionType.EXPECTED_VEHICLE_CHANGING);
            historyItems.add(historyItem);
            trip.setExpectedVehicleId(vehicle.getId());
        } catch (IllegalArgumentException e) {
            return new FieldException(PLANNING_SHIFT_ID, entry, UUID.class);
        }
        return null;
    }

    private TripHistoryItem getNewTripHistoryItem(Trip trip, Actor actor, UUID dispatcherId) {
        var historyItem = new TripHistoryItem();
        historyItem.setChangeTime(LocalDateTime.now(ZoneOffset.UTC));
        historyItem.setTripId(trip.getId());
        historyItem.setOldDispatcherId(trip.getDispatcherId());
        historyItem.setOldDriverId(trip.getDriverId());
        historyItem.setOldStatus(trip.getStatus());
        historyItem.setOldPlannedShiftId(trip.getPlannedShiftId());
        switch (actor){
            case ADMIN -> {
                historyItem.setActorId(dispatcherId);
                historyItem.setActorType(Actor.ADMIN);
            }
            case DISPATCHER -> {
                historyItem.setActorId(dispatcherId);
                historyItem.setActorType(Actor.DISPATCHER);
            }
            case DRIVER -> {
                historyItem.setActorId(trip.getDriverId());
                historyItem.setActorType(Actor.DRIVER);
            }
            default -> {
                log.debug("Unexpected actor type, history item will not have actor info");
            }
        }
        return historyItem;
    }

    private TripHistoryItem getNewTripHistoryItem(Trip trip, UUID driverId) {
        var historyItem = new TripHistoryItem();
        historyItem.setChangeTime(LocalDateTime.now(ZoneOffset.UTC));
        historyItem.setTripId(trip.getId());
        historyItem.setOldDispatcherId(trip.getDispatcherId());
        historyItem.setNewDispatcherId(trip.getDispatcherId());
        historyItem.setOldDriverId(trip.getDriverId());
        historyItem.setNewDriverId(driverId);
        historyItem.setOldStatus(trip.getStatus());
        historyItem.setNewStatus(TripStatus.DRIVER_ASSIGNED);
        historyItem.setOldPlannedShiftId(trip.getPlannedShiftId());
        historyItem.setNewPlannedShiftId(null);
        historyItem.setActorId(null);
        historyItem.setAction(ActionType.DRIVER_CHANGING);
        historyItem.setActorType(Actor.SYSTEM);
        return historyItem;
    }

    private FieldException factCost(Trip trip, Serializable entry, ArrayList<TripHistoryItem> historyItems, UUID dispatcherId, Actor actor) {
        try {
            var historyItem = getNewTripHistoryItem(trip, actor, dispatcherId);
            historyItem.setNewDriverId(trip.getDriverId());
            historyItem.setNewStatus(trip.getStatus());
            historyItem.setAction(ActionType.FACT_DATA_CHANGING);
            historyItems.add(historyItem);
            var factCost = Long.parseLong(String.valueOf(entry));
            trip.setFactCost(factCost);
        } catch (IllegalArgumentException e) {
            return new FieldException(FACT_COST_FIELD, entry, UUID.class);
        }
        return null;
    }

    private UUID getIdDispatcher(Dispatcher dispatcher, UUID userId ){
        return Objects.isNull(dispatcher) ? userId : dispatcher.getId();
    }

    private Actor setActorByDispatcher(Dispatcher dispatcher) {
        return Objects.isNull(dispatcher) ? Actor.ADMIN : Actor.DISPATCHER;
    }

    private FieldException createDispatcherTakeToWorkEvent(Trip trip, Dispatcher dispatcher, ArrayList<TripHistoryItem> historyItems, Serializable entry){
        try {
            if (Boolean.parseBoolean(String.valueOf(entry)) && trip.getDispatcherId() == null && dispatcher != null) {
                var historyItem = new TripHistoryItem();
                historyItem.setChangeTime(LocalDateTime.now(ZoneOffset.UTC));
                historyItem.setTripId(trip.getId());
                historyItem.setOldDispatcherId(trip.getDispatcherId());
                historyItem.setNewDispatcherId(dispatcher.getId());
                historyItem.setOldDriverId(trip.getDriverId());
                historyItem.setNewDriverId(trip.getDriverId());
                historyItem.setOldStatus(trip.getStatus());
                historyItem.setNewStatus(trip.getStatus());
                historyItem.setOldPlannedShiftId(trip.getPlannedShiftId());
                historyItem.setNewPlannedShiftId(trip.getPlannedShiftId());
                historyItem.setActorId(dispatcher.getId());
                historyItem.setAction(ActionType.DISPATCHER_TAKE_TO_WORK);
                historyItem.setActorType(Actor.DISPATCHER);
                historyItems.add(historyItem);
            }
        } catch (IllegalArgumentException e) {
            return new FieldException(DISPATCHER_TAKE_TO_WORK_FIELD, entry, UUID.class);
        }
        return null;
    }
}
