package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.request.database.model.TaxiTrip;
import ru.sberbank.ditsib.transport.request.database.model.TaxiTrip_;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Репозиторий поездок на такси
 */
@Repository
public interface TaxiTripRepository extends JpaRepository<TaxiTrip, UUID> {
    
    @EntityGraph(attributePaths = TaxiTrip_.REQUESTS, type = EntityGraph.EntityGraphType.LOAD)
    List<TaxiTrip> findByHumanReadableId(String id);
    
    @Query(nativeQuery = true, value = "select id from request.taxi_trip " +
                                       " where id in (select distinct taxi_trip_id from request.request_for_taxi where " +
                                       " request_closed_datetime is null and active = true" +
                                       " and taxi_trip_id is not null" +
                                       " and (desired_date > :notExpiredTime or finished_time is not null and finished_time > :notExpiredTime)) " +
                                       " and status in ('SENT_TO_CONTRACTOR', 'WAITING_FOR_ASSIGNMENT', 'DRIVER_ASSIGNED', 'DRIVER_APPROVED', " +
                                       "'DRIVER_ON_THE_WAY', 'DRIVER_ARRIVED', 'TRIP_IN_PROGRESS')" +
                                       " and taxi_id is not null")
    Set<UUID> findTripsIdInProgress(LocalDateTime notExpiredTime);
    
    @Query(nativeQuery = true, value = "select id from request.taxi_trip " +
                                       " where id in (select distinct taxi_trip_id from request.request_for_taxi where " +
                                       " request_closed_datetime is null and active = true" +
                                       " and taxi_trip_id is not null" +
                                       " and (desired_date > :notExpiredTime or finished_time is not null and finished_time > :notExpiredTime)) " +
                                       " and status in ('SENT_TO_CONTRACTOR', 'WAITING_FOR_ASSIGNMENT', 'DRIVER_ASSIGNED', 'DRIVER_APPROVED', " +
                                       "'DRIVER_ON_THE_WAY', 'DRIVER_ARRIVED', 'TRIP_IN_PROGRESS', 'ORDER_FINISHED')" +
                                       " and taxi_id is not null")
    Set<UUID> findTripsIdInProgressAndFinished(LocalDateTime notExpiredTime);
    
    @Query(nativeQuery = true, value = "select CAST( id as varchar ) from request.taxi_trip " +
                                       " where id in (select distinct taxi_trip_id from request.request_for_taxi where " +
                                       " request_closed_datetime is null and active = true" +
                                       " and taxi_trip_id is not null" +
                                       " and (desired_date < :notExpiredTime or finished_time is not null and finished_time < :notExpiredTime)) " +
                                       " and status in ('DRIVER_ARRIVED', 'TRIP_IN_PROGRESS', 'ORDER_FINISHED')" +
                                       " and taxi_id is not null")
    List<String> findTripForClosing(LocalDateTime notExpiredTime);
    
    @EntityGraph(attributePaths = TaxiTrip_.REQUESTS, type = EntityGraph.EntityGraphType.LOAD)
    List<TaxiTrip> findAllById(Iterable<UUID> ids);

    @Modifying
    @Query(value = """
            update request.taxi_trip tt
            set
                registry_fact_waiting_time = :factWaitingTime,
                registry_human_readable_id = :registryHrId,
                registry_fact_cost = :factCost,
                registry_fact_distance = :factDistance,
                registry_fact_payment = cast(:factPayment as boolean)
            where (
                select rft.taxi_trip_id
                from request.request_for_taxi rft
                where rft.humanreadableid = :hrId
            ) = tt.id
            """, nativeQuery = true)
    void updateFactDataByHrId(
            @Param("factWaitingTime") Double factWaitingTime,
            @Param("registryHrId") String registryHrId,
            @Param("factCost") Double factCost,
            @Param("factDistance") Double factDistance,
            @Param("factPayment") boolean factPayment,
            @Param("hrId") String hrId);
}
