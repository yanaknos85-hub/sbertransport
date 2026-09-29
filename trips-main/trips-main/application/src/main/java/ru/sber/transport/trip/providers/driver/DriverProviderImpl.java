package ru.sber.transport.trip.providers.driver;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.SelectConditionStep;
import org.jooq.SelectWhereStep;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.sber.transport.trip.business.model.Driver;
import ru.sber.transport.trip.business.model.TripStatus;
import ru.sber.transport.trip.database.trips.Keys;
import ru.sber.transport.trip.database.trips.Tables;
import ru.sber.transport.trip.database.trips.tables.records.DriverRecord;
import ru.sber.transport.trip.messaging.providers.DriverProvider;
import ru.sber.transport.trip.providers.driver.mapper.DriverMapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DriverProviderImpl implements DriverProvider {

    private final DSLContext dslContext;

    private final DriverMapper driverMapper;

    @Override
    public int save(Driver driver) {
        var driverRecord = driverMapper.toRecord(driver);
        return dslContext.insertInto(Tables.DRIVER).set(driverRecord)
                .onConflict(Keys.PK_DRIVER.getFields()).doUpdate().set(driverRecord).execute();
    }

    @Override
    public Optional<Driver> getDriverByTripId(UUID tripId) {
        return dslContext.select(Tables.DRIVER)
                .from(Tables.TRIPS_).innerJoin(Tables.DRIVER)
                .on(Tables.DRIVER.ID.eq(Tables.TRIPS_.DRIVER_ID))
                .where(Tables.TRIPS_.ID.eq(tripId))
                .fetchOptionalInto(DriverRecord.class)
                .map(driverMapper::toModel);
    }

    @Override
    public Optional<Driver> get(UUID id) {
        return dslContext.selectFrom(Tables.DRIVER)
                .where(Tables.DRIVER.ID.eq(id))
                .fetchOptional().map(driverMapper::toModel);
    }

    @Override
    public Optional<Driver> getByOauthId(UUID oauthId) {
        return dslContext.selectFrom(Tables.DRIVER)
                .where(Tables.DRIVER.OAUTH_ID.eq(oauthId))
                .fetchOptional().map(driverMapper::toModel);
    }

    @Override
    public Optional<Driver> findByContractorIdAndId(UUID contractorId, UUID id) {
        return dslContext.selectFrom(Tables.DRIVER)
                .where(Tables.DRIVER.ID.eq(id))
                .and(Tables.DRIVER.CONTRACTOR_ID.eq(contractorId))
                .fetchOptional().map(driverMapper::toModel);
    }

    @Override
    public List<Driver> findAllByActiveAndOnlineAndContractorId(boolean online, boolean active, UUID contractorId, String name, UUID autoparkId) {
        var selectConditionStep = dslContext.selectFrom(Tables.DRIVER)
                .where(Tables.DRIVER.CONTRACTOR_ID.eq(contractorId))
                .and(Tables.DRIVER.ONLINE.eq(online))
                .and(Tables.DRIVER.ACTIVE.eq(active));
        if (name != null && !name.isEmpty()) {
            selectConditionStep = selectConditionStep.and(Tables.DRIVER.FIRST_NAME.likeIgnoreCase("%" + name + "%")
                    .or(Tables.DRIVER.LAST_NAME.likeIgnoreCase("%" + name + "%"))
                    .or(Tables.DRIVER.PATRONYMIC.likeIgnoreCase("%" + name + "%")));
        }
        if(autoparkId != null){
            selectConditionStep = selectConditionStep.and(Tables.DRIVER.AUTOPARK_ID.eq(autoparkId));
        }
        return selectConditionStep.fetchInto(DriverRecord.class)
                .stream().map(driverMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Driver> findAllByContractorIdAndOnline(UUID contractorId, boolean online, Pageable page, String name, UUID autoparkId) {
        var func = (Function<SelectWhereStep<?>, SelectConditionStep<?>>) from ->
        {
            var selectConditionStep = from.where(Tables.DRIVER.CONTRACTOR_ID.eq(contractorId))
                    .and(Tables.DRIVER.ONLINE.eq(online))
                    .and(Tables.DRIVER.ACTIVE.isTrue());
            if (name != null && !name.isEmpty()) {
                selectConditionStep = selectConditionStep.and(Tables.DRIVER.FIRST_NAME.likeIgnoreCase("%" + name + "%")
                        .or(Tables.DRIVER.LAST_NAME.likeIgnoreCase("%" + name + "%"))
                        .or(Tables.DRIVER.PATRONYMIC.likeIgnoreCase("%" + name + "%")));
            }
            if(autoparkId != null){
                selectConditionStep = selectConditionStep.and(Tables.DRIVER.AUTOPARK_ID.eq(autoparkId));
            }
            return selectConditionStep;
        };
        var request = func.apply(dslContext.selectFrom(Tables.DRIVER));
        var list = request
                .offset(page.getOffset())
                .limit(page.getPageSize())
                .fetchInto(DriverRecord.class);
        var total = func.apply(dslContext.selectCount().from(Tables.DRIVER)).fetchInto(Integer.class).get(0);
        return new PageImpl<>(
                list.stream().map(driverMapper::toModel).collect(Collectors.toList()),
                page,
                total
        );
    }

    @Override
    public List<Driver> getAllDriversByShiftDateIn(LocalDateTime startTime, LocalDateTime endTime, UUID contractorId, String name, UUID autoparkId) {
        var selectConditionStep = dslContext.select(Tables.DRIVER.ID.as("ID"),
                        Tables.DRIVER.HUMAN_READABLE_ID.as("HUMAN_READABLE_ID"),
                        Tables.DRIVER.LAST_NAME.as("LAST_NAME"),
                        Tables.DRIVER.FIRST_NAME.as("FIRST_NAME"),
                        Tables.DRIVER.PATRONYMIC.as("PATRONYMIC"),
                        Tables.DRIVER.PASSPORT.as("PASSPORT"),
                        Tables.DRIVER.CONTRACTOR_ID.as("CONTRACTOR_ID"),
                        Tables.DRIVER.ACTIVE.as("ACTIVE"),
                        Tables.DRIVER.RATING.as("RATING"),
                        Tables.DRIVER.DRIVER_LICENSE_NUMBER.as("DRIVER_LICENSE_NUMBER"),
                        Tables.DRIVER.CARGO_LICENCE_NUMBER.as("CARGO_LICENCE_NUMBER"),
                        Tables.DRIVER.LATITUDE.as("LATITUDE"),
                        Tables.DRIVER.LONGITUDE.as("LONGITUDE"),
                        Tables.DRIVER.POINT_TIME.as("POINT_TIME"),
                        Tables.DRIVER.TIME_ZONE.as("TIME_ZONE"),
                        Tables.DRIVER.SERVING.as("SERVING"),
                        Tables.DRIVER.ONLINE.as("ONLINE"),
                        Tables.DRIVER.ACTIVE_TRIP_ID.as("ACTIVE_TRIP_ID"),
                        Tables.SHIFT.ID.as("SHIFT_ID"),
                        Tables.DRIVER.EXPERIENCE.as("EXPERIENCE"),
                        Tables.DRIVER.CONTACT_PHONE.as("CONTACT_PHONE"),
                        Tables.DRIVER.EMAIL.as("EMAIL"),
                        Tables.DRIVER.SERVICE_LICENSE_NUMBER.as("SERVICE_LICENSE_NUMBER"),
                        Tables.DRIVER.CONSENT.as("CONSENT")
                )
                .from(Tables.DRIVER).innerJoin(Tables.SHIFT)
                .on(Tables.DRIVER.ID.eq(Tables.SHIFT.DRIVER_ID))
                .leftJoin(Tables.TRIPS_).on(Tables.SHIFT.ID.eq(Tables.TRIPS_.PLANNED_SHIFT_ID))
                .where(Tables.SHIFT.START_DATE.lessOrEqual(startTime))
                .and(Tables.TRIPS_.PLANNED_SHIFT_ID.isNull()
                        .or(Tables.TRIPS_.EXPECTED_START_TIME.greaterThan(endTime.atOffset(ZoneOffset.UTC)))
                        .or(Tables.TRIPS_.EXPECTED_END_TIME.lessThan(startTime.atOffset(ZoneOffset.UTC)))
                )
                .and(Tables.SHIFT.CONTRACTOR_ID.eq(contractorId))
                .and(Tables.SHIFT.END_DATE.greaterOrEqual(startTime))
                .and(Tables.SHIFT.DELETED.eq(false))
                .and(Tables.DRIVER.ACTIVE.isTrue());

        if (name != null && !name.isEmpty()) {
            selectConditionStep = selectConditionStep.and(Tables.DRIVER.FIRST_NAME.likeIgnoreCase("%" + name + "%")
                    .or(Tables.DRIVER.LAST_NAME.likeIgnoreCase("%" + name + "%"))
                    .or(Tables.DRIVER.PATRONYMIC.likeIgnoreCase("%" + name + "%")));
        }
        if(autoparkId != null){
            selectConditionStep = selectConditionStep.and(Tables.DRIVER.AUTOPARK_ID.eq(autoparkId));
        }
        return selectConditionStep.fetchInto(DriverRecord.class)
                .stream().map(driverMapper::toModel).toList(); //NOSONAR

    }

    @Override
    public void saveLocationData(UUID driverId, String timeZone, OffsetDateTime pointTime, Double latitude, Double longitude, Double azimuth) {
        dslContext.update(Tables.DRIVER)
                .set(Tables.DRIVER.TIME_ZONE, timeZone)
                .set(Tables.DRIVER.POINT_TIME, pointTime)
                .set(Tables.DRIVER.LATITUDE, latitude)
                .set(Tables.DRIVER.LONGITUDE, longitude)
                .set(Tables.DRIVER.AZIMUTH, azimuth)
                .where(Tables.DRIVER.ID.eq(driverId))
                .execute();
    }

    @Override
    @Deprecated
    public List<Driver> findDriversToLiberate() {
        return dslContext.select(Tables.DRIVER.fields())
                .from(Tables.DRIVER).innerJoin(Tables.TRIPS_)
                .on(Tables.TRIPS_.ID.eq(Tables.DRIVER.ACTIVE_TRIP_ID))
                .where(Tables.TRIPS_.STATUS.eq(TripStatus.ORDER_FINISHED.name()))
                .fetchInto(DriverRecord.class)
                .stream().map(driverMapper::toModel)
                .collect(Collectors.toList()); //NOSONAR
    }

    @Override
    public List<Driver> getAllByIds(List<UUID> ids) {
        return dslContext.selectFrom(Tables.DRIVER)
                .where(Tables.DRIVER.ID.in(ids))
                .fetchInto(DriverRecord.class)
                .stream().map(driverMapper::toModel)
                .toList();
    }
}
