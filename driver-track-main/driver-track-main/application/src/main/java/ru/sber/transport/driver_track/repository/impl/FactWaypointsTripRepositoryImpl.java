package ru.sber.transport.driver_track.repository.impl;

import lombok.RequiredArgsConstructor;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import ru.sber.transport.driver_track.config.RouteProperties;
import ru.sber.transport.driver_track.database.driver_track.Tables;
import ru.sber.transport.driver_track.database.driver_track.tables.FactWaypointsTrip;
import ru.sber.transport.driver_track.database.driver_track.tables.records.FactWaypointsTripRecord;
import ru.sber.transport.driver_track.database.driver_track.tables.records.RouteRecord;
import ru.sber.transport.driver_track.repository.FactWaypointsTripRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.jooq.impl.DSL.inline;
import static ru.sber.transport.driver_track.dto.RouteSource.TWO_GIS;

@Repository
@RequiredArgsConstructor
public class FactWaypointsTripRepositoryImpl implements FactWaypointsTripRepository {
    private final RouteProperties routeProperties;

    @Override
    public Optional<FactWaypointsTripRecord> findByTripId(UUID tripId) {
        return context().selectFrom(table())
                .where(table().TRIP_ID.eq(tripId))
                .fetchOptionalInto(FactWaypointsTripRecord.class);
    }

    @Override
    public FactWaypointsTrip table() {
        return Tables.FACT_WAYPOINTS_TRIP;
    }

    @Override
    public List<FactWaypointsTripRecord> getAllNotHandledRecords() {
       return context().selectFrom(table())
               .where(table().IS_TRACK_CREATED.isFalse()
                       .and(table().GENERATION_ATTEMPTS.lt(routeProperties.maxGenerationAttempts()))
                       .and(table().GENERATION_NOT_BEFORE.isNull()
                               .or(table().GENERATION_NOT_BEFORE.le(DSL.field("now()",
                                       LocalDateTime.class))
                               )
                       )
               )
               .forUpdate()
               .skipLocked()
               .fetchInto(FactWaypointsTripRecord.class);
    }

    @Override
    public void handleCompliteCreateExpectedRoute(List<RouteRecord> handledTripRecords) {
        var compliteCreatedRoutes = handledTripRecords.stream().filter(item -> TWO_GIS.name().equals(item.getSource()))
                .map(RouteRecord::getTripId).toList();
        var errorCreatedRoutes = handledTripRecords.stream().filter(item -> !TWO_GIS.name().equals(item.getSource()))
                .map(RouteRecord::getTripId).toList();

        if (!compliteCreatedRoutes.isEmpty()) {
            context().update(table())
                    .set(table().IS_TRACK_CREATED, true)
                    .set(table().GENERATION_ATTEMPTS, inline(0))
                    .set(table().GENERATION_NOT_BEFORE, (LocalDateTime) null)
                    .where(table().TRIP_ID.in(compliteCreatedRoutes))
                    .execute();
        }

        if (!errorCreatedRoutes.isEmpty()) {
            context().update(table())
                    .set(table().GENERATION_ATTEMPTS, table().GENERATION_ATTEMPTS.plus(1))
                    .set(table().GENERATION_NOT_BEFORE, DSL.field("now() + ({0} * interval '5 minute')",
                            LocalDateTime.class,
                            table().GENERATION_ATTEMPTS)
                    )
                    .where(table().TRIP_ID.in(errorCreatedRoutes)
                            .and(table().GENERATION_ATTEMPTS.lt(routeProperties.maxGenerationAttempts()))
                    )
                    .execute();
        }
    }

    @Override
    public void setMaxAttemptsCountToWrongRouteGeneration(List<UUID> expectedRouteWithWrongWaypointsUUIDList) {
        context().update(table())
                .set(table().GENERATION_ATTEMPTS, routeProperties.maxGenerationAttempts())
                .where(table().TRIP_ID.in(expectedRouteWithWrongWaypointsUUIDList))
                .execute();
    }
}
