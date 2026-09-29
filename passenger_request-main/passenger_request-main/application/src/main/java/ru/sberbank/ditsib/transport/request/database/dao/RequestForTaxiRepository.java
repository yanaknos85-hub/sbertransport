package ru.sberbank.ditsib.transport.request.database.dao;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.model.RequestForTaxi;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RequestForTaxiRepository extends TypedRequestRepository<RequestForTaxi>, CoopedRequestRepository<RequestForTaxi> {
    
    @EntityGraph(value = "request")
    @Override
    @Query("select distinct req from RequestForTaxi req " +
           " left join fetch req.approvedBy " +
           " left join fetch req.author " +
           " left join fetch req.historyItemsForTaxi " +
           " left join fetch req.waypoints " +
           " left join fetch req.passenger" +
           " left join fetch req.purpose ")
    @NonNull
    List<RequestForTaxi> findAll();
    
    @EntityGraph(value = "request")
    @Override
    @Query("select distinct req from RequestForTaxi req " +
           " left join fetch req.approvedBy " +
           " left join fetch req.author " +
           " left join fetch req.historyItemsForTaxi " +
           " left join fetch req.waypoints " +
           " left join fetch req.passenger" +
           " left join fetch req.purpose ")
    @NonNull
    List<RequestForTaxi> findAll(@NonNull Sort sort);
    
    @Query("""
           select distinct request from RequestForTaxi request
            where request.sentToContractor = false and (request.status = 'TAXI_APPROVED')
            and request.desiredDate > :notExpiredTime
            order by request.desiredDate asc""")
    List<RequestForTaxi> findNewReadyToSend(LocalDateTime notExpiredTime);
    
    @Query("select distinct req from RequestForTaxi req where req.rideId = :rideId and req.active = true")
    List<RequestForTaxi> findActiveByRideId(UUID rideId);
    
    @Query("select distinct req from RequestForTaxi req where req.rideId in (:rideIds) and req.active = true")
    List<RequestForTaxi> findActiveByRideIdIn(List<UUID> rideIds);
    
    @Override
    @EntityGraph(value = "requestTaxiForDispatcher")
    @NonNull
    Page<RequestForTaxi> findAll(Specification<RequestForTaxi> var1, @NonNull Pageable var2);
    
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE from RequestForTaxi")
    void deleteAll();
 
    @Query("select req from RequestForTaxi req " +
           " left join fetch req.taxiTrip " +
           " where req.id = :requestId")
    Optional<RequestForTaxi> getWithFactData(UUID requestId);
    
    @Query("select request from RequestForTaxi request inner join request.taxiTrip trip where trip.id = :id")
    RequestForTaxi findByTripId(UUID id);

    List<RequestForTaxi> findByRideId(UUID rideId);
    
    @Query("SELECT r FROM RequestForTaxi r " +
           "WHERE r.approvalDeadline IS NOT NULL " +
           "AND r.status = :awaitingApprovalStatus " +
           "AND r.approvalDate IS NULL and r.approvalDeadline < :now")
    List<RequestForTaxi> findRequestForTaxiWithApprovalDeadlineViolation(LocalDateTime now, TripRequestStatus awaitingApprovalStatus);
    
    @Query("select r from RequestForTaxi r " +
           "left join fetch r.taxiTrip " +
           "where r.id in (:ids)")
    List<RequestForTaxi> findAllByIdWithTrips(List<UUID> ids);

    @Override
    default TransportTypeEnum type() {
        return TransportTypeEnum.TAXI;
    }

}
