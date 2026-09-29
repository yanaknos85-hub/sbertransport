package ru.sber.transport.trip.providers.trips;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jooq.*;
import org.jooq.Record;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import ru.sber.transport.trip.business.dto.*;
import ru.sber.transport.trip.business.model.Trip;
import ru.sber.transport.trip.business.model.TripStatus;
import ru.sber.transport.trip.business.providers.TripProvider;
import ru.sber.transport.trip.database.trips.Keys;
import ru.sber.transport.trip.database.trips.Tables;
import ru.sber.transport.trip.database.trips.tables.records.TripsRecord;
import ru.sber.transport.trip.messaging.providers.ContractorProvider;
import ru.sber.transport.trip.providers.trips.mapping.TripMapper;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Repository
class TripProviderImpl implements TripProvider {

    private final DSLContext dslContext;

    private final TripMapper tripMapper;

    private final ContractorProvider contractorProvider;

    private final ObjectMapper objectMapper;

    @Override
    public Optional<Trip> get(UUID tripId) {
        return dslContext.selectFrom(Tables.TRIPS_)
                .where(Tables.TRIPS_.ID.eq(tripId))
                .fetchOptional().map(tripMapper::toModel);
    }

    @Override
    public int save(Trip trip) {
        if (trip.getDigitId() == null) {
            trip.setDigitId(contractorProvider.nextDigit(trip.getContractorId()));
        }
        var model = tripMapper.toEntity(trip);
        return dslContext.insertInto(Tables.TRIPS_).set(model)
                .onConflict(Keys.PK_TRIPS.getFields()).doUpdate().set(model)
                .execute();
    }

    @Override
    public int countByContractorIdAndStatusIn(UUID contractorId, List<TripStatus> statuses, UUID autoparkId) {
        var stringStatuses = statuses.stream().map(Enum::name).toList();

        var condition = Tables.TRIPS_.CONTRACTOR_ID.eq(contractorId)
                .and(Tables.TRIPS_.STATUS.in(stringStatuses));

        if (autoparkId != null) {
            condition = condition.and(Tables.TRIPS_.AUTOPARK_ID.eq(autoparkId));
        }

        return dslContext.fetchCount(Tables.TRIPS_, condition);
    }

    @Override
    public Optional<Trip> findByContractorIdAndId(UUID contractorId, UUID tripId) {
        return dslContext.selectFrom(Tables.TRIPS_)
                .where(Tables.TRIPS_.ID.eq(tripId))
                .and(Tables.TRIPS_.CONTRACTOR_ID.eq(contractorId))
                .fetchOptional().map(tripMapper::toModel);
    }

    @Override
    public Optional<Trip> findByContractorIdAndHumanReadableId(UUID contractorId, String humanReadableId) {
        var select = dslContext.selectFrom(Tables.TRIPS_)
                .where(Tables.TRIPS_.CONTRACTOR_ID.eq(contractorId));
        select = appendHumanReadableId(humanReadableId, select);
        return select.fetchOptional().map(tripMapper::toModel);
    }
    @Override
    public List<Trip> findAllByDriverAndStatusIn(UUID driverId, List<TripStatus> tripStatuses) {
        var stringStatuses = tripStatuses.stream().map(Enum::name).collect(Collectors.toList());
        return dslContext.selectFrom(Tables.TRIPS_)
                        .where(Tables.TRIPS_.DRIVER_ID.eq(driverId))
                        .and(Tables.TRIPS_.STATUS.in(stringStatuses))
                        .fetchInto(TripsRecord.class)
                .stream().map(tripMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<Trip> findAll(UUID contractorId, RequestSearchDto searchData, List<TripStatus> statuses) {
        var stringStatuses = statuses.stream().map(Enum::name).collect(Collectors.toList());
        var select = dslContext.selectFrom(Tables.TRIPS_).where(Tables.TRIPS_.CONTRACTOR_ID.eq(contractorId))
                .and(Tables.TRIPS_.STATUS.in(stringStatuses));
        return enrichRequestBySearchData(select, searchData);
    }

    @Override
    public Iterable<Trip> findAllByContractorIdAndDispatcherIdAndStatusIn(UUID contractorId, UUID dispatcherId, List<TripStatus> statuses, RequestSearchDto searchData) {
        var stringStatuses = statuses.stream().map(Enum::name).collect(Collectors.toList());
        var request = dslContext.selectFrom(Tables.TRIPS_).where(Tables.TRIPS_.CONTRACTOR_ID.eq(contractorId))
                .and(Tables.TRIPS_.DISPATCHER_ID.eq(dispatcherId))
                .and(Tables.TRIPS_.STATUS.in(stringStatuses));
        return enrichRequestBySearchData(request, searchData);
    }

    @Override
    public List<Trip> findAllByDriverIdAndStartTimeAfterAndStatusInOrderByStartTimeDesc(UUID driverId, LocalDateTime startDateTime, List<TripStatus> tripStatuses) {
        var stringStatuses = tripStatuses.stream().map(Enum::name).collect(Collectors.toList());
        return dslContext.selectFrom(Tables.TRIPS_)
                        .where(Tables.TRIPS_.DRIVER_ID.eq(driverId))
                        .and(Tables.TRIPS_.EXPECTED_START_TIME.greaterThan(OffsetDateTime.of(startDateTime, ZoneOffset.UTC)))
                        .and(Tables.TRIPS_.STATUS.in(stringStatuses))
                        .orderBy(Tables.TRIPS_.EXPECTED_START_TIME.desc())
                        .fetchInto(TripsRecord.class)
                .stream().map(tripMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Trip> findTripsForAssigningDriver() {
        return dslContext.select(Arrays.stream(Tables.TRIPS_.fields()).toList())
                .from(Tables.TRIPS_).innerJoin(Tables.CONTRACTORS)
                .on(Tables.TRIPS_.CONTRACTOR_ID.eq(Tables.CONTRACTORS.ID))
                .where(Tables.TRIPS_.STATUS.eq(TripStatus.WAITING_FOR_ASSIGNMENT.name()))
                .and(Tables.TRIPS_.DRIVER_ID.isNull())
                .and(Tables.TRIPS_.EXPECTED_START_TIME.greaterOrEqual(OffsetDateTime.of(LocalDate.now(ZoneOffset.UTC).atStartOfDay().minusDays(2), ZoneOffset.UTC)))
                .and(Tables.TRIPS_.PLANNED_SHIFT_ID.isNull())
                .and(Tables.CONTRACTORS.AUTOASSIGN.isTrue())
                .orderBy(Tables.TRIPS_.EXPECTED_START_TIME.asc())
                .fetchInto(TripsRecord.class)
                .stream().map(tripMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Trip> findAllByContractorIdOrderByDigitId(UUID contractorId, TripsExportFiltersDTO filters) {
        var select = dslContext.selectFrom(Tables.TRIPS_)
                .where(Tables.TRIPS_.CONTRACTOR_ID.eq(contractorId));
        if(filters!=null){
            if(filters.getDuration()!=null) {
                select = select.and(Tables.TRIPS_.EXPECTED_START_TIME.between(filters.getDuration().getStart(),
                        filters.getDuration().getEnd()));
            }
            if(filters.getStatuses()!=null) {
                select = select.and(Tables.TRIPS_.STATUS.in(filters.getStatuses().stream().map(TripStatus::toString)
                        .collect(Collectors.toList())));
            }
        }
        return select.orderBy(Tables.TRIPS_.DIGIT_ID)
                    .fetchInto(TripsRecord.class)
                    .stream().map(tripMapper::toModel)
                    .collect(Collectors.toList());
    }

    @Override
    public List<Trip> findAllByDriverId(UUID driverId) {
        return dslContext.selectFrom(Tables.TRIPS_)
                .where(Tables.TRIPS_.DRIVER_ID.eq(driverId))
                .fetchInto(TripsRecord.class)
                .stream().map(tripMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<Trip> findAllByDriverAndStatusIn(UUID driverId, List<TripStatus> tripStatuses, RequestSearchDto searchDto) {
        var pageable = PageRequest.of(searchDto.getPage(), searchDto.getSize());
        var sort = searchDto.getField().getSortField().sort(SortOrder.valueOf(searchDto.getDirection().name()));
        var stringStatuses = tripStatuses.stream().map(Enum::name).collect(Collectors.toList());
        var func = (Function<SelectWhereStep<?>, SelectConditionStep<?>>) from -> from
                .where(Tables.TRIPS_.DRIVER_ID.eq(driverId))
                .and(Tables.TRIPS_.STATUS.in(stringStatuses));
        var select =  func.apply(dslContext.selectFrom(Tables.TRIPS_));
        var list = select
                .orderBy(sort)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchInto(TripsRecord.class);
        var total = func.apply(dslContext.selectCount().from(Tables.TRIPS_))
                .fetchInto(Integer.class).get(0);
        return new PageImpl<>(
                list.stream().map(tripMapper::toModel).collect(Collectors.toList()),
                pageable,
                total
        );
    }

    @Override
    public List<DriverBusynessDTO.BusynessData> findAllDriverBusyness(DriverBusynessRequest driverBusynessRequest, UUID contractorId, Long contractorDigitId) {
        return getDriverBusinessRequestSignature()
                .from(Tables.TRIPS_).innerJoin(Tables.DRIVER)
                .on(Tables.DRIVER.ID.eq(Tables.TRIPS_.DRIVER_ID))
                .leftJoin(Tables.VEHICLE)
                .on(Tables.VEHICLE.ID.eq(Tables.TRIPS_.VEHICLE_ID))
                .where(Tables.TRIPS_.EXPECTED_START_TIME.between(driverBusynessRequest.startTime(), driverBusynessRequest.endTime())
                        .or(Tables.TRIPS_.EXPECTED_END_TIME.between(driverBusynessRequest.startTime(), driverBusynessRequest.endTime())))
                .and(Tables.TRIPS_.CONTRACTOR_ID.eq(contractorId))
                .and(Tables.TRIPS_.DRIVER_ID.in(driverBusynessRequest.driverIds()))
                .fetchInto(DriverBusynessRawResponse.class)
                .parallelStream().map(driverBusynessRawResponse -> tripMapper.toDto(driverBusynessRawResponse, contractorDigitId, false))
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleBusynessDTO> findAllVehicleBusyness(VehicleBusynessRequest vehicleBusynessRequest, UUID contractorId, Long contractorDigitId) {
        return getVehicleBusinessRequestSignature()
                .from(Tables.TRIPS_)
                .leftJoin(Tables.VEHICLE)
                .on(Tables.VEHICLE.ID.eq(Tables.TRIPS_.VEHICLE_ID))
                .where(Tables.TRIPS_.EXPECTED_START_TIME.between(vehicleBusynessRequest.startTime(), vehicleBusynessRequest.endTime())
                        .or(Tables.TRIPS_.EXPECTED_END_TIME.between(vehicleBusynessRequest.startTime(), vehicleBusynessRequest.endTime())))
                .and(Tables.TRIPS_.CONTRACTOR_ID.eq(contractorId))
                .and(Tables.TRIPS_.VEHICLE_ID.in(vehicleBusynessRequest.vehicleIds()))
                .fetchInto(VehicleBusynessRawResponse.class)
                .parallelStream().map(vehicleBusynessRawResponse -> tripMapper.toDto(vehicleBusynessRawResponse, contractorDigitId, false, false))
                .collect(Collectors.toList());
    }

    @Override
    public List<DriverBusynessDTO.BusynessData> findAllPlanningDriverBusyness(DriverBusynessRequest driverBusynessRequest, UUID contractorId, Long contractorDigitId) {
        return getDriverBusinessRequestSignature()
                .from(Tables.TRIPS_).innerJoin(Tables.SHIFT)
                .on(Tables.TRIPS_.PLANNED_SHIFT_ID.eq(Tables.SHIFT.ID))
                .innerJoin(Tables.DRIVER)
                .on(Tables.DRIVER.ID.eq(Tables.SHIFT.DRIVER_ID))
                .leftJoin(Tables.VEHICLE)
                .on(Tables.VEHICLE.ID.eq(Tables.SHIFT.VEHICLE_ID))
                .where(Tables.SHIFT.DRIVER_ID.in(driverBusynessRequest.driverIds()))
                .and(Tables.TRIPS_.EXPECTED_START_TIME.between(driverBusynessRequest.startTime(), driverBusynessRequest.endTime())
                        .or(Tables.TRIPS_.EXPECTED_END_TIME.between(driverBusynessRequest.startTime(), driverBusynessRequest.endTime())))
                .and(Tables.TRIPS_.CONTRACTOR_ID.eq(contractorId))
                .fetchInto(DriverBusynessRawResponse.class)
                .parallelStream().map(driverBusynessRawResponse -> tripMapper.toDto(driverBusynessRawResponse, contractorDigitId, true))
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleBusynessDTO> findAllPlanningVehicleBusyness(VehicleBusynessRequest vehicleBusynessRequest, UUID contractorId, Long contractorDigitId) {
        return getVehicleBusinessRequestSignature()
                .from(Tables.TRIPS_).innerJoin(Tables.SHIFT)
                .on(Tables.TRIPS_.PLANNED_SHIFT_ID.eq(Tables.SHIFT.ID))
                .leftJoin(Tables.VEHICLE)
                .on(Tables.VEHICLE.ID.eq(Tables.SHIFT.VEHICLE_ID))
                .where(Tables.SHIFT.VEHICLE_ID.in(vehicleBusynessRequest.vehicleIds()))
                .and(Tables.TRIPS_.EXPECTED_START_TIME.between(vehicleBusynessRequest.startTime(), vehicleBusynessRequest.endTime())
                        .or(Tables.TRIPS_.EXPECTED_END_TIME.between(vehicleBusynessRequest.startTime(), vehicleBusynessRequest.endTime())))
                .and(Tables.TRIPS_.CONTRACTOR_ID.eq(contractorId))
                .fetchInto(VehicleBusynessRawResponse.class)
                .parallelStream().map(vehicleBusynessRawResponse -> tripMapper.toDto(vehicleBusynessRawResponse, contractorDigitId, true, false))
                .collect(Collectors.toList());
    }

    @Override
    public List<DriverBusynessDTO.TripData> findAllOrderedVehicles(DriverBusynessRequest driverBusynessRequest, UUID contractorId, Long contractorDigitId) {
        return getDriverBusinessOrdersSignature()
                .from(Tables.TRIPS_)
                .leftJoin(Tables.VEHICLE)
                .on(Tables.VEHICLE.ID.eq(Tables.TRIPS_.EXPECTED_VEHICLE_ID))
                .where(Tables.TRIPS_.EXPECTED_START_TIME.between(driverBusynessRequest.startTime(), driverBusynessRequest.endTime())
                        .or(Tables.TRIPS_.EXPECTED_END_TIME.between(driverBusynessRequest.startTime(), driverBusynessRequest.endTime())))
                .and(Tables.TRIPS_.PLANNED_SHIFT_ID.isNull())
                .and(Tables.TRIPS_.DRIVER_ID.isNull())
                .and(Tables.TRIPS_.VEHICLE_ID.isNull())
                .and(Tables.TRIPS_.EXPECTED_VEHICLE_ID.isNotNull())
                .and(Tables.TRIPS_.STATUS.eq(TripStatus.WAITING_FOR_ASSIGNMENT.name()))
                .and(Tables.TRIPS_.CONTRACTOR_ID.eq(contractorId))
                .fetchInto(DriverBusynessRawResponse.TripData.class)
                .parallelStream().map(driverBusynessRawResponse ->
                        tripMapper.fillTrips(driverBusynessRawResponse, contractorDigitId, true).get(0))
                .toList();
    }

    @Override
    public List<VehicleBusynessDTO> findAllOrderedVehicles(VehicleBusynessRequest vehicleBusynessRequest, UUID contractorId, Long contractorDigitId) {
        return getVehicleBusinessRequestSignature()
                .from(Tables.TRIPS_)
                .leftJoin(Tables.VEHICLE)
                .on(Tables.VEHICLE.ID.eq(Tables.TRIPS_.EXPECTED_VEHICLE_ID))
                .where(Tables.TRIPS_.EXPECTED_START_TIME.between(vehicleBusynessRequest.startTime(), vehicleBusynessRequest.endTime())
                        .or(Tables.TRIPS_.EXPECTED_END_TIME.between(vehicleBusynessRequest.startTime(), vehicleBusynessRequest.endTime())))
                .and(Tables.TRIPS_.PLANNED_SHIFT_ID.isNull())
                .and(Tables.TRIPS_.DRIVER_ID.isNull())
                .and(Tables.TRIPS_.VEHICLE_ID.isNull())
                .and(Tables.TRIPS_.EXPECTED_VEHICLE_ID.isNotNull())
                .and(Tables.TRIPS_.STATUS.eq(TripStatus.WAITING_FOR_ASSIGNMENT.name()))
                .and(Tables.TRIPS_.CONTRACTOR_ID.eq(contractorId))
                .fetchInto(VehicleBusynessRawResponse.class)
                .parallelStream().map(vehicleBusynessRawResponse ->
                        tripMapper.toDto(vehicleBusynessRawResponse, contractorDigitId, false, true))
                .toList();
    }

    @Override
    public List<Trip> findAllByPlannedShiftId(UUID plannedShiftId) {
        return dslContext.selectFrom(Tables.TRIPS_)
                .where(Tables.TRIPS_.PLANNED_SHIFT_ID.eq(plannedShiftId))
                .fetchInto(TripsRecord.class)
                .stream().map(tripMapper::toModel).collect(Collectors.toList()); //NOSONAR
    }

    @Override
    public List<TripsRecord> findAllByReportCreatedFalseAndTerminalStatusesAndLimit(int limit) {
        return dslContext.selectFrom(Tables.TRIPS_)
                .where(Tables.TRIPS_.REPORT_CREATED.isFalse())
                .and(Tables.TRIPS_.STATUS.in(TripStatus.getStringTerminalStatuses()))
                .orderBy(Tables.TRIPS_.FACT_START_TIME.asc())
                .limit(limit)
                .fetchInto(TripsRecord.class);
    }

    @Override
    public void setReportCreatedIsTrueByIds(List<UUID> ids) {
        dslContext.update(Tables.TRIPS_)
                .set(Tables.TRIPS_.REPORT_CREATED, true)
                .where(Tables.TRIPS_.ID.in(ids))
                .execute();
    }

    @Override
    public void updateFactData(Trip trip) {
        dslContext.update(Tables.TRIPS_)
                .set(Tables.TRIPS_.FACT_DISTANCE, trip.getFactDistance())
                .set(Tables.TRIPS_.DRIVER_WAITING_TIME, trip.getDriverWaitingTime() != null ? trip.getDriverWaitingTime().toMillis() : null)
                .where(Tables.TRIPS_.ID.eq(trip.getId()))
                .execute();
    }

    private Iterable<Trip> enrichRequestBySearchData(SelectConditionStep<TripsRecord> request, RequestSearchDto searchData){
        var requestAppended = appendFilters(searchData, request);

        var pageNumber = searchData.getPage();
        var pageSize = searchData.getSize();
        var page = PageRequest.of(pageNumber, pageSize);
        var sort = searchData.getField().getSortField().sort(SortOrder.valueOf(searchData.getDirection().name()));
        var total = dslContext.fetchCount(requestAppended);
        var list = requestAppended
                .orderBy(sort)
                .offset(page.getOffset())
                .limit(page.getPageSize())
                .fetchInto(TripsRecord.class);
        return new PageImpl<>(
                list.stream().map(tripMapper::toModel).collect(Collectors.toList()),
                page,
                total
        );
    }

    private <T extends Record> SelectConditionStep<T> appendHumanReadableId(Serializable value, SelectConditionStep<T> select) {
        var stringArray = value.toString().split("-");
        if(stringArray.length>0) {
            var stringDigitId = stringArray[stringArray.length - 1];
            BigInteger digitId;
            try {
                digitId = BigInteger.valueOf(Long.parseLong(stringDigitId));
            } catch (NumberFormatException numberFormatException) {
                return select;
            }
            return select.and(Tables.TRIPS_.DIGIT_ID.eq(digitId));
        }
        return select;
    }

    private <T extends Record> SelectConditionStep<T> appendRequestHumanReadableId(Serializable value, SelectConditionStep<T> select) {
        return select.and((Tables.TRIPS_.REQUESTS.cast(String.class).likeRegex("\"humanReadableId\"\\s{0,}:\\s{0,}\"[-\\w]*(%s)[-\\w]*\"".formatted(value))).or(Tables.TRIPS_.EXTERNAL_HUMAN_READABLE_ID.like("%"+value+"%")));
    }

    private <T extends Record> SelectConditionStep<T> appendDesireDateStart(Serializable value, SelectConditionStep<T> select) {
        return select.and(Tables.TRIPS_.EXPECTED_START_TIME.greaterOrEqual(ZonedDateTime.parse(String.valueOf(value)).toOffsetDateTime()));
    }

    private <T extends Record> SelectConditionStep<T> appendDesireDateEnd(Serializable value, SelectConditionStep<T> select) {
        return select.and(Tables.TRIPS_.EXPECTED_START_TIME.lessOrEqual(ZonedDateTime.parse(String.valueOf(value)).toOffsetDateTime()));
    }

    private <T extends Record> SelectConditionStep<T> appendDriverIds(Serializable value, SelectConditionStep<T> select){
        return select.and(Tables.TRIPS_.DRIVER_ID.in(objectMapper.convertValue(value, new TypeReference<List<UUID>>(){})));
    }

    private <T extends Record> SelectConditionStep<T> appendExpectedTime(Serializable value, SelectConditionStep<T> select){
        return select.and(Tables.TRIPS_.EXPECTED_TIME.greaterOrEqual(Duration.ofHours(Long.parseLong(value.toString())).toSeconds()));
    }

    private <T extends Record> SelectConditionStep<T> appendAutoparkId(Serializable value, SelectConditionStep<T> select){
        return select.and(Tables.TRIPS_.AUTOPARK_ID.eq(UUID.fromString(String.valueOf(value))));
    }

    private <T extends Record> SelectConditionStep<T> appendTaxiClass(Serializable value, SelectConditionStep<T> select){
        return select.and(Tables.TRIPS_.TAXI_CLASS.eq(String.valueOf(value)));
    }

    private <T extends Record> SelectConditionStep<T> appendFilters(RequestSearchDto searchData, SelectConditionStep<T> select) {
        var filter = searchData.getFilter();
        for (var entry : filter.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            if (value != null) {
                switch (key) {
                    case HUMAN_READABLE_ID -> select = appendHumanReadableId(value, select);
                    case REQUEST_HUMAN_READABLE_ID -> select = appendRequestHumanReadableId(value, select);
                    case DESIRE_DATE_START -> select = appendDesireDateStart(value, select);
                    case DESIRE_DATE_END -> select = appendDesireDateEnd(value, select);
                    case DRIVER_IDS -> select = appendDriverIds(value, select);
                    case EXPECTED_TIME -> select = appendExpectedTime(value, select);
                    case AUTOPARK_ID -> select = appendAutoparkId(value, select);
                    case TAXI_CLASS -> select = appendTaxiClass(value, select);
                }
            }
        }
        return select;
    }

    private @NotNull SelectSelectStep<Record16<UUID, String, UUID, String, BigInteger, JSON, JSON, OffsetDateTime, OffsetDateTime, OffsetDateTime, OffsetDateTime, String, UUID, String, String, String>> getDriverBusinessRequestSignature(){
        return dslContext.select(Tables.DRIVER.ID.as("driverData.id"),
                Tables.DRIVER.HUMAN_READABLE_ID.as("driverData.humanReadableId"),
                Tables.TRIPS_.ID.as("tripData.id"),
                Tables.TRIPS_.STATUS.as("tripData.status"),
                Tables.TRIPS_.DIGIT_ID.as("tripData.digitId"),
                Tables.TRIPS_.REQUESTS.as("tripData.requests"),
                Tables.TRIPS_.WAYPOINTS.as("tripData.waypoints"),
                Tables.TRIPS_.EXPECTED_START_TIME.as("tripData.expectedStartTime"),
                Tables.TRIPS_.EXPECTED_END_TIME.as("tripData.expectedEndTime"),
                Tables.TRIPS_.FACT_START_TIME.as("tripData.factStartTime"),
                Tables.TRIPS_.FACT_END_TIME.as("tripData.factEndTime"),
                Tables.TRIPS_.TIME_ZONE.as("tripData.timeZone"),
                Tables.VEHICLE.ID.as("tripData.vehicleData.id"),
                Tables.VEHICLE.STATE_NUMBER.as("tripData.vehicleData.stateNumber"),
                Tables.VEHICLE.MODEL.as("tripData.vehicleData.model"),
                Tables.VEHICLE.BRAND.as("tripData.vehicleData.brand"));
    }

    private @NotNull SelectSelectStep<Record11<UUID, UUID, String, BigInteger, JSON, JSON, OffsetDateTime, OffsetDateTime, OffsetDateTime, OffsetDateTime, String>> getVehicleBusinessRequestSignature(){
        return dslContext.select(Tables.VEHICLE.ID.as("id"),
                Tables.TRIPS_.ID.as("tripData.id"),
                Tables.TRIPS_.STATUS.as("tripData.status"),
                Tables.TRIPS_.DIGIT_ID.as("tripData.digitId"),
                Tables.TRIPS_.REQUESTS.as("tripData.requests"),
                Tables.TRIPS_.WAYPOINTS.as("tripData.waypoints"),
                Tables.TRIPS_.EXPECTED_START_TIME.as("tripData.expectedStartTime"),
                Tables.TRIPS_.EXPECTED_END_TIME.as("tripData.expectedEndTime"),
                Tables.TRIPS_.FACT_START_TIME.as("tripData.factStartTime"),
                Tables.TRIPS_.FACT_END_TIME.as("tripData.factEndTime"),
                Tables.TRIPS_.TIME_ZONE.as("tripData.timeZone"));
    }

    private @NotNull SelectSelectStep<Record14<UUID, String, BigInteger, JSON, JSON, OffsetDateTime, OffsetDateTime, OffsetDateTime, OffsetDateTime, String, UUID, String, String, String>> getDriverBusinessOrdersSignature(){
        return dslContext.select(
                Tables.TRIPS_.ID.as("id"),
                Tables.TRIPS_.STATUS.as("status"),
                Tables.TRIPS_.DIGIT_ID.as("digitId"),
                Tables.TRIPS_.REQUESTS.as("requests"),
                Tables.TRIPS_.WAYPOINTS.as("waypoints"),
                Tables.TRIPS_.EXPECTED_START_TIME.as("expectedStartTime"),
                Tables.TRIPS_.EXPECTED_END_TIME.as("expectedEndTime"),
                Tables.TRIPS_.FACT_START_TIME.as("factStartTime"),
                Tables.TRIPS_.FACT_END_TIME.as("factEndTime"),
                Tables.TRIPS_.TIME_ZONE.as("timeZone"),
                Tables.VEHICLE.ID.as("vehicleData.id"),
                Tables.VEHICLE.STATE_NUMBER.as("vehicleData.stateNumber"),
                Tables.VEHICLE.MODEL.as("vehicleData.model"),
                Tables.VEHICLE.BRAND.as("vehicleData.brand"));
    }
}
