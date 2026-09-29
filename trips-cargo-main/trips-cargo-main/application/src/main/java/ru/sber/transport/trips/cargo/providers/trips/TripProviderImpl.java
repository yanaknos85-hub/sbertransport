package ru.sber.transport.trips.cargo.providers.trips;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import ru.sber.transport.trips.cargo.business.dto.*;
import ru.sber.transport.trips.cargo.business.model.Trip;
import ru.sber.transport.trips.cargo.business.model.TripStatus;
import ru.sber.transport.trips.cargo.business.providers.TripProvider;
import ru.sber.transport.trips.cargo.messaging.providers.ContractorProvider;
import ru.sber.transport.trips.cargo.providers.trips.mapping.TripMapper;
import ru.sber.transport.trips_cargo.database.trips_cargo.Keys;
import ru.sber.transport.trips_cargo.database.trips_cargo.Tables;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.records.TripsRecord;

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
        return dslContext.selectFrom(Tables.TRIPS)
                .where(Tables.TRIPS.ID.eq(tripId))
                .fetchOptional().map(tripMapper::toModel);
    }

    @Override
    public Optional<Trip> findByContractorIdAndId(UUID contractorId, UUID tripId) {
        return dslContext.selectFrom(Tables.TRIPS)
                .where(Tables.TRIPS.ID.eq(tripId))
                .and(Tables.TRIPS.CONTRACTOR_ID.eq(contractorId))
                .fetchOptional().map(tripMapper::toModel);
    }

    @Override
    public Optional<Trip> findByContractorIdAndHumanReadableId(UUID contractorId, String humanReadableId) {
        var select = dslContext.selectFrom(Tables.TRIPS)
                .where(Tables.TRIPS.CONTRACTOR_ID.eq(contractorId));
        select = appendHumanReadableId(humanReadableId, select);
        return select.fetchOptional().map(tripMapper::toModel);
    }

    @Override
    public int save(Trip trip) {
        if (trip.getDigitId() == null) {
            trip.setDigitId(contractorProvider.nextDigit(trip.getContractorId()));
        }
        var model = tripMapper.toEntity(trip);
        return dslContext.insertInto(Tables.TRIPS).set(model)
                .onConflict(Keys.PK_TRIPS_CARGO.getFields()).doUpdate().set(model)
                .execute();
    }

    @Override
    public int countByContractorIdAndStatusIn(UUID contractorId, UUID autoparkId, List<TripStatus> statuses) {
        var stringStatuses = statuses.stream().map(Enum::name).toList();

        Condition autoparkCondition = DSL.noCondition();
        if (autoparkId != null) {
            autoparkCondition = autoparkCondition.and(Tables.TRIPS.AUTOPARK_ID.eq(autoparkId));
        }

        var condition = Tables.TRIPS.CONTRACTOR_ID.eq(contractorId)
                .and(Tables.TRIPS.STATUS.in(stringStatuses))
                .and(autoparkCondition);

        return dslContext.fetchCount(Tables.TRIPS, condition);
    }

    @Override
    public List<Trip> findAllByDriverAndStatusIn(UUID driverId, List<TripStatus> tripStatuses) {
        var stringStatuses = tripStatuses.stream().map(Enum::name).collect(Collectors.toList());
        return dslContext.selectFrom(Tables.TRIPS)
                .where(Tables.TRIPS.DRIVER_ID.eq(driverId))
                .and(Tables.TRIPS.STATUS.in(stringStatuses))
                .fetchInto(TripsRecord.class).stream().map(tripMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<Trip> findAll(UUID contractorId, RequestSearchDto searchData, List<TripStatus> statuses, Pageable pageable) {
        var stringStatuses = statuses.stream().map(Enum::name).collect(Collectors.toList());
        var select = dslContext.selectFrom(Tables.TRIPS).where(Tables.TRIPS.CONTRACTOR_ID.eq(contractorId))
                .and(Tables.TRIPS.STATUS.in(stringStatuses));

        select = appendFilters(searchData, select);

        var total = dslContext.fetchCount(select);
        var sort = searchData.getField().getSortField().sort(SortOrder.valueOf(searchData.getDirection().name()));
        var list = select
                .orderBy(sort)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchInto(TripsRecord.class);
        return new PageImpl<>(
                list.stream().map(tripMapper::toModel).collect(Collectors.toList()),
                pageable,
                total
        );
    }

    @Override
    public Iterable<Trip> findAllByContractorIdAndDispatcherIdAndStatusIn(UUID contractorId, UUID dispatcherId, List<TripStatus> statuses, Pageable pageable, RequestSearchDto searchData) {
        var stringStatuses = statuses.stream().map(Enum::name).collect(Collectors.toList());
        var request = dslContext.selectFrom(Tables.TRIPS).where(Tables.TRIPS.CONTRACTOR_ID.eq(contractorId))
                .and(Tables.TRIPS.DISPATCHER_ID.eq(dispatcherId))
                .and(Tables.TRIPS.STATUS.in(stringStatuses));

        request = appendFilters(searchData, request);

        var total = dslContext.fetchCount(request);
        var list = request
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchInto(TripsRecord.class);
        return new PageImpl<>(
                list.stream().map(tripMapper::toModel).collect(Collectors.toList()),
                pageable,
                total
        );
    }

    @Override
    public List<Trip> findAllByDriverIdAndStartTimeAfterAndStatusInOrderByStartTimeDesc(UUID driverId, LocalDateTime startDateTime, List<TripStatus> tripStatuses) {
        var stringStatuses = tripStatuses.stream().map(Enum::name).collect(Collectors.toList());
        return dslContext.selectFrom(Tables.TRIPS)
                .where(Tables.TRIPS.DRIVER_ID.eq(driverId))
                .and(Tables.TRIPS.START_TIME.greaterThan(OffsetDateTime.of(startDateTime, ZoneOffset.UTC)))
                .and(Tables.TRIPS.STATUS.in(stringStatuses))
                .orderBy(Tables.TRIPS.START_TIME.desc())
                .fetchInto(TripsRecord.class)
                .stream().map(tripMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Trip> findTripsForAssigningDriver() {
        return dslContext.select(Arrays.stream(Tables.TRIPS.fields()).toList())
                .from(Tables.TRIPS).innerJoin(Tables.CONTRACTORS)
                .on(Tables.TRIPS.CONTRACTOR_ID.eq(Tables.CONTRACTORS.ID))
                .where(Tables.TRIPS.STATUS.eq(TripStatus.WAITING_FOR_ASSIGNMENT.name()))
                .and(Tables.TRIPS.DRIVER_ID.isNull())
                .and(Tables.TRIPS.START_TIME.greaterOrEqual(OffsetDateTime.of(LocalDate.now(ZoneOffset.UTC).atStartOfDay().minusDays(2), ZoneOffset.UTC)))
                .and(Tables.TRIPS.PLANNED_SHIFT_ID.isNull())
                .and(Tables.CONTRACTORS.AUTOASSIGN.isTrue())
                .orderBy(Tables.TRIPS.START_TIME.asc())
                .fetchInto(TripsRecord.class)
                .stream().map(tripMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Trip> findAllByContractorIdOrderByDigitId(UUID contractorId, TripsExportFiltersDTO filters) {
        var select = dslContext.selectFrom(Tables.TRIPS)
                .where(Tables.TRIPS.CONTRACTOR_ID.eq(contractorId));
        if (filters != null) {
            if (filters.getDuration() != null) {
                select = select.and(Tables.TRIPS.START_TIME.between(filters.getDuration().getStart(),
                        filters.getDuration().getEnd()));
            }
            if (filters.getStatuses() != null) {
                select = select.and(Tables.TRIPS.STATUS.in(filters.getStatuses().stream().map(TripStatus::toString)
                        .collect(Collectors.toList())));
            }
        }
        return select.orderBy(Tables.TRIPS.DIGIT_ID)
                .fetchInto(TripsRecord.class)
                .stream().map(tripMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Trip> findAllByDriverId(UUID driverId) {
        return dslContext.selectFrom(Tables.TRIPS)
                .where(Tables.TRIPS.DRIVER_ID.eq(driverId))
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
                .where(Tables.TRIPS.DRIVER_ID.eq(driverId))
                .and(Tables.TRIPS.STATUS.in(stringStatuses));
        var select = func.apply(dslContext.selectFrom(Tables.TRIPS));
        var list = select
                .orderBy(sort)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchInto(TripsRecord.class);
        var total = func.apply(dslContext.selectCount().from(Tables.TRIPS))
                .fetchInto(Integer.class).get(0);
        return new PageImpl<>(
                list.stream().map(tripMapper::toModel).collect(Collectors.toList()),
                pageable,
                total
        );
    }

    @Override
    public List<DriverBusynessDTO.BusynessData> findAllDriverBusyness(DriverBusynessRequest driverBusynessRequest, UUID contractorId, Long contractorDigitId) {
        return dslContext.select(Tables.DRIVER.ID.as("driverData.id"),
                        Tables.DRIVER.HUMAN_READABLE_ID.as("driverData.humanReadableId"),
                        Tables.TRIPS.ID.as("tripData.id"),
                        Tables.TRIPS.STATUS.as("tripData.status"),
                        Tables.TRIPS.DIGIT_ID.as("tripData.digitId"),
                        Tables.TRIPS.REQUESTS.as("tripData.requests"),
                        Tables.TRIPS.START_TIME.as("tripData.startTime"),
                        Tables.TRIPS.END_TIME.as("tripData.endTime"),
                        Tables.VEHICLE.ID.as("tripData.vehicleData.id"),
                        Tables.VEHICLE.STATE_NUMBER.as("tripData.vehicleData.stateNumber"),
                        Tables.VEHICLE.MODEL.as("tripData.vehicleData.model"),
                        Tables.VEHICLE.BRAND.as("tripData.vehicleData.brand"))
                .from(Tables.TRIPS).innerJoin(Tables.DRIVER)
                .on(Tables.DRIVER.ID.eq(Tables.TRIPS.DRIVER_ID))
                .leftJoin(Tables.VEHICLE)
                .on(Tables.VEHICLE.ID.eq(Tables.TRIPS.VEHICLE_ID))
                .where(Tables.TRIPS.START_TIME.between(driverBusynessRequest.startTime(), driverBusynessRequest.endTime())
                        .or(Tables.TRIPS.END_TIME.between(driverBusynessRequest.startTime(), driverBusynessRequest.endTime())))
                .and(Tables.TRIPS.CONTRACTOR_ID.eq(contractorId))
                .and(Tables.TRIPS.DRIVER_ID.in(driverBusynessRequest.driverIds()))
                .fetchInto(DriverBusynessRawResponse.class)
                .parallelStream().map(driverBusynessRawResponse -> tripMapper.toDto(driverBusynessRawResponse, contractorDigitId))
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleBusynessDTO> findAllVehicleBusyness(VehicleBusynessRequest vehicleBusynessRequest, UUID contractorId, Long contractorDigitId) {
        return dslContext.select(Tables.VEHICLE.ID.as("id"),
                        Tables.TRIPS.ID.as("tripData.id"),
                        Tables.TRIPS.STATUS.as("tripData.status"),
                        Tables.TRIPS.DIGIT_ID.as("tripData.digitId"),
                        Tables.TRIPS.REQUESTS.as("tripData.requests"),
                        Tables.TRIPS.WAYPOINTS.as("tripData.waypoints"),
                        Tables.TRIPS.START_TIME.as("tripData.expectedStartTime"),
                        Tables.TRIPS.END_TIME.as("tripData.expectedEndTime"),
                        Tables.TRIPS.TIME_ZONE.as("tripData.timeZone"))
                .from(Tables.TRIPS)
                .leftJoin(Tables.VEHICLE)
                .on(Tables.VEHICLE.ID.eq(Tables.TRIPS.VEHICLE_ID))
                .where(Tables.TRIPS.START_TIME.between(vehicleBusynessRequest.startTime(), vehicleBusynessRequest.endTime())
                        .or(Tables.TRIPS.END_TIME.between(vehicleBusynessRequest.startTime(), vehicleBusynessRequest.endTime())))
                .and(Tables.TRIPS.CONTRACTOR_ID.eq(contractorId))
                .and(Tables.TRIPS.VEHICLE_ID.in(vehicleBusynessRequest.vehicleIds()))
                .fetchInto(VehicleBusynessRawResponse.class)
                .parallelStream().map(vehicleBusynessRawResponse -> tripMapper.toDto(vehicleBusynessRawResponse, contractorDigitId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Trip> findAllByPlannedShiftId(UUID plannedShiftId) {
        return dslContext.selectFrom(Tables.TRIPS)
                .where(Tables.TRIPS.PLANNED_SHIFT_ID.eq(plannedShiftId))
                .fetchInto(TripsRecord.class)
                .stream().map(tripMapper::toModel).collect(Collectors.toList()); //NOSONAR
    }

    @Override
    public Optional<Trip> getByRouteHumanReadableId(String routeHumanReadableId) {
        return dslContext.selectFrom(Tables.TRIPS)
                .where(Tables.TRIPS.ROUTE_HUMAN_READABLE_ID.eq(routeHumanReadableId))
                .fetchOptional().map(tripMapper::toModel);
    }

    private <T extends Record> SelectConditionStep<T> appendHumanReadableId(Serializable value, SelectConditionStep<T> select) {
        var stringArray = value.toString().split("-");
        if (stringArray.length > 0) {
            var stringDigitId = stringArray[stringArray.length - 1];
            BigInteger digitId;
            try {
                digitId = BigInteger.valueOf(Long.parseLong(stringDigitId));
            } catch (NumberFormatException numberFormatException) {
                return select;
            }
            return select.and(Tables.TRIPS.DIGIT_ID.eq(digitId));
        }
        return select;
    }

    private <T extends Record> SelectConditionStep<T> appendRequestHumanReadableId(Serializable value, SelectConditionStep<T> select) {
        return select.and(Tables.TRIPS.REQUESTS.cast(String.class).likeRegex("\"humanReadableId\"\\s{0,}:\\s{0,}\"[-\\w]*(%s)[-\\w]*\"".formatted(value))
                .or(Tables.TRIPS.ROUTE_HUMAN_READABLE_ID.like("%" + value + "%")));
    }

    private <T extends Record> SelectConditionStep<T> appendDesireDateStart(Serializable value, SelectConditionStep<T> select) {
        return select.and(Tables.TRIPS.START_TIME.greaterOrEqual(ZonedDateTime.parse(String.valueOf(value)).toOffsetDateTime()));
    }

    private <T extends Record> SelectConditionStep<T> appendDesireDateEnd(Serializable value, SelectConditionStep<T> select) {
        return select.and(Tables.TRIPS.START_TIME.lessOrEqual(ZonedDateTime.parse(String.valueOf(value)).toOffsetDateTime()));
    }

    private <T extends Record> SelectConditionStep<T> appendDriverIds(Serializable value, SelectConditionStep<T> select){
        return select.and(Tables.TRIPS.DRIVER_ID.in(objectMapper.convertValue(value, new TypeReference<List<UUID>>(){})));
    }

    private <T extends Record> SelectConditionStep<T> appendAutoparkId(Serializable value, SelectConditionStep<T> select){
        return select.and(Tables.TRIPS.AUTOPARK_ID.eq(UUID.fromString(value.toString())));
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
                    case AUTOPARK_ID -> select = appendAutoparkId(value, select);
                }
            }
        }
        return select;
    }
}
