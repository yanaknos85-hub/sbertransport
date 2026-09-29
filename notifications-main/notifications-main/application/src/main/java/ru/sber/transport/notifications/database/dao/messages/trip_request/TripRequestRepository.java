package ru.sber.transport.notifications.database.dao.messages.trip_request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.sber.transport.notifications.database.model.request.TripRequest;

import java.util.Set;
import java.util.UUID;

/**
 * Работа с сообщениями о заявках на поездку в базе.
 */
public interface TripRequestRepository extends JpaRepository<TripRequest, UUID> {
    
    @Query("select trip from TripRequest trip where trip.sharedRide is not null and trip.sharedRide.id = :rideId")
    Set<TripRequest> findAllBySharedRideId(@Param("rideId") UUID rideId);
}
