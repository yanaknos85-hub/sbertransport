package ru.sber.transport.dispatcher.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.transport.dispatcher.database.model.Trip;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TripsRepository extends JpaRepository<Trip, UUID>{

    @Query("select trip from Trip trip " +
            "where trip.vehicleId in (:vehicleIds) " +
            "and ((trip.startTime >= :start and trip.endTime <= :end) or " +
            "(trip.startTime <= :start and trip.endTime >= :end) or" +
            "(trip.startTime between :start and :end) or " +
            "(trip.endTime between :start and :end))")
    List<Trip> findAllByVehicleIdsAndDate(List<UUID> vehicleIds, OffsetDateTime start, OffsetDateTime end);

}
