package ru.sberbank.ditsib.transport.request.database.dao;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.dao.projection.RequestForPersonalSplitCheckProjection;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPersonal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RequestForPersonalRepository extends TypedRequestRepository<RequestForPersonal>, CoopedRequestRepository<RequestForPersonal> {
    
    @NotNull
    @EntityGraph(value = "request")
    @Override
    @Query("select distinct req from RequestForPersonal req " +
           " left join fetch req.approvedBy " +
           " left join fetch req.author " +
           " left join fetch req.historyItemsForPersonal " +
           " left join fetch req.waypoints " +
           " left join fetch req.passenger" +
           " left join fetch req.purpose")
    List<RequestForPersonal> findAll();
    
    @NotNull
    @EntityGraph(value = "request")
    @Override
    @Query("select distinct req from RequestForPersonal req " +
           " left join fetch req.approvedBy " +
           " left join fetch req.author " +
           " left join fetch req.historyItemsForPersonal " +
           " left join fetch req.waypoints " +
           " left join fetch req.passenger" +
           " left join fetch req.purpose")
    List<RequestForPersonal> findAll(@NotNull Sort sort);
    
    @NotNull
    @Override
    @EntityGraph(value = "request")
    Page<RequestForPersonal> findAll(@Nullable Specification<RequestForPersonal> var1, @NotNull Pageable var2);
    
    List<RequestForPersonal> findByRideId(UUID rideId);
    
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE from RequestForPersonal")
    void deleteAll();
    
    List<RequestForPersonal> findAllByStatusAndDesiredDateBefore(TripRequestStatus personalApproved,
                                                                 LocalDateTime date);
    
    @Query("SELECT r FROM RequestForPersonal r " +
           "WHERE r.approvalDeadline IS NOT NULL " +
           "AND r.status = :awaitingApprovalStatus " +
           "AND r.approvalDate IS NULL and r.approvalDeadline < :now")
    List<RequestForPersonal> findRequestForPersonalWithApprovalDeadlineViolation(LocalDateTime now, TripRequestStatus awaitingApprovalStatus);
    
    @Query("SELECT r FROM RequestForPersonal r " +
           "WHERE r.tripApprovalDeadline IS NOT NULL " +
           "AND r.status = :awaitingTripApprovalStatus " +
           "AND r.tripApprovalDatetime IS NULL and r.tripApprovalDeadline < :now")
    List<RequestForPersonal> findRequestForPersonalWithTripApprovalDeadlineViolation(LocalDateTime now, TripRequestStatus awaitingTripApprovalStatus);
    
    @Query("SELECT r FROM RequestForPersonal r " +
           "WHERE (r.status = :personalOrderPaymentFormationStatus OR r.status = :personalPaymentAwaiting) " +
           "AND r.finishedTime IS NULL and r.paymentDoneDeadline < :now " +
           "AND r.paymentDoneDeadlineState <> :deadlineStateRed")
    List<RequestForPersonal> findRequestForPersonalForSettingPaymentDoneDeadlineViolation(LocalDateTime now,
                                                                                          TripRequestStatus personalOrderPaymentFormationStatus,
                                                                                          TripRequestStatus personalPaymentAwaiting,
                                                                                          DeadlineState deadlineStateRed
                                                                                         );

    @Transactional(readOnly = true)
    @Query("SELECT new ru.sberbank.ditsib.transport.request.database.dao.projection.RequestForPersonalSplitCheckProjection(" +
            "   r.id, " +
            "   r.humanReadableId, " +
            "   r.desiredDate, " +
            "   r.expected.time, " +
            "   r.status, " +
            "   r.employeeDeviceTimeZone )" +
            "FROM RequestForPersonal r " +
            "WHERE r.passenger.id = :employeeId " +
            "  AND r.desiredDate >= :startOfDay " +
            "  AND r.desiredDate < :endOfDay " +
            "  AND r.status NOT IN :statuses " +
            "  AND r.expected.cost <= :maxExpectedCost")
    List<RequestForPersonalSplitCheckProjection> findSplitCheckProjectionsByPassengerIdAndStatusNotInAndDesiredDateBetween(
            UUID employeeId,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay,
            List<TripRequestStatus> statuses,
            Double maxExpectedCost
    );

    @Override
    default TransportTypeEnum type() {
        return TransportTypeEnum.PERSONAL;
    }

}
