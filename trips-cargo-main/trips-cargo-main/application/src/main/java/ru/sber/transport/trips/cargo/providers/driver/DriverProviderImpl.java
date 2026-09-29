package ru.sber.transport.trips.cargo.providers.driver;

import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SelectConditionStep;
import org.jooq.SelectWhereStep;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.sber.transport.trips.cargo.business.model.Driver;
import ru.sber.transport.trips.cargo.messaging.providers.DriverProvider;
import ru.sber.transport.trips.cargo.providers.driver.mapper.DriverMapper;
import ru.sber.transport.trips_cargo.database.trips_cargo.Keys;
import ru.sber.transport.trips_cargo.database.trips_cargo.Tables;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.records.DriverRecord;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
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
                .from(Tables.TRIPS).innerJoin(Tables.DRIVER)
                .on(Tables.DRIVER.ID.eq(Tables.TRIPS.DRIVER_ID))
                .where(Tables.TRIPS.ID.eq(tripId))
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
    public List<Driver> findAllByActiveAndOnlineAndContractorIdAndAutoparkId(boolean online, boolean active,
                                                                             UUID contractorId, UUID autoparkId) {

        Condition contractorCondition = contractorId != null ? Tables.DRIVER.CONTRACTOR_ID.eq(contractorId) : DSL.noCondition();
        Condition autoparkCondition = autoparkId != null ? Tables.DRIVER.AUTOPARK_ID.eq(autoparkId) : DSL.noCondition();

        return dslContext.selectFrom(Tables.DRIVER)
                .where(contractorCondition)
                .and(autoparkCondition)
                .and(Tables.DRIVER.ONLINE.eq(online))
                .and(Tables.DRIVER.ACTIVE.eq(active))
                .fetchInto(DriverRecord.class).stream()
                .map(driverMapper::toModel)
                .collect(Collectors.toList());
    }

    public Page<Driver> findAllByContractorIdAndOnline(UUID contractorId, UUID autoparkId, boolean online, Pageable page) {
        Condition autoparkCondition = autoparkId != null ? Tables.DRIVER.AUTOPARK_ID.eq(autoparkId) : DSL.noCondition();

        var func = (Function<SelectWhereStep<?>, SelectConditionStep<?>>) from ->
                from.where(Tables.DRIVER.CONTRACTOR_ID.eq(contractorId))
                        .and(autoparkCondition)
                        .and(Tables.DRIVER.ONLINE.eq(online))
                        .and(Tables.DRIVER.ACTIVE.isTrue());
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
    public List<Driver> getAllDriversByShiftDateIn(LocalDateTime time, UUID contractorId, UUID autoparkId) {
        Condition autoparkCondition = autoparkId != null ? Tables.DRIVER.AUTOPARK_ID.eq(autoparkId) : DSL.noCondition();

        return dslContext.select(Tables.DRIVER.ID.as("ID"),
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
                .where(Tables.SHIFT.START_DATE.lessOrEqual(time))
                .and(Tables.SHIFT.CONTRACTOR_ID.eq(contractorId))
                .and(Tables.SHIFT.END_DATE.greaterOrEqual(time))
                .and(Tables.SHIFT.DELETED.eq(false))
                .and(Tables.DRIVER.ACTIVE.isTrue())
                .and(autoparkCondition)
                .fetchInto(DriverRecord.class)
                .stream().map(driverMapper::toModel).collect(Collectors.toList()); //NOSONAR
    }
}
