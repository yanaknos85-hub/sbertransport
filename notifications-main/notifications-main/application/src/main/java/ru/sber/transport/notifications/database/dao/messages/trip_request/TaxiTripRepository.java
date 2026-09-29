package ru.sber.transport.notifications.database.dao.messages.trip_request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.sber.transport.notifications.database.model.request.TaxiTrip;

import java.util.UUID;

public interface TaxiTripRepository extends JpaRepository<TaxiTrip, UUID> {
    
    @Query("select tt from TaxiTrip tt where tt.requestId = :requestId")
    TaxiTrip findByRequestId(@Param("requestId") UUID requestId);
    
    @Query("select tt from TaxiTrip tt where tt.sharedRideId = :sharedRideId")
    TaxiTrip findBySharedRideId(@Param("sharedRideId") UUID sharedRideId);
    
}
