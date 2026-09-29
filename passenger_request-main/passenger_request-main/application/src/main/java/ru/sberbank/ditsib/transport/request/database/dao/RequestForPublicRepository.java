package ru.sberbank.ditsib.transport.request.database.dao;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPublic;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RequestForPublicRepository extends TypedRequestRepository<RequestForPublic> {
    
    /**
     * Find all requests authored by specified user
     *
     * @param authorId employee id
     *
     * @return list of qualified requests
     */
    List<RequestForPublic> findAllByAuthorId(UUID authorId);
    
    List<RequestForPublic> findAllByHumanReadableId(String humanReadableId);
    
    @NotNull
    @Override
    @Query("select distinct req from RequestForPublic req " +
           " left join fetch req.approvedBy " +
           " left join fetch req.author " +
           " left join fetch req.historyItemsForPublic " +
           " left join fetch req.waypoints " +
           " left join fetch req.passenger" +
           " left join fetch req.purpose")
    List<RequestForPublic> findAll();
    
    @NotNull
    @EntityGraph(value = "request")
    @Override
    @Query("select distinct req from RequestForPublic req " +
           " left join fetch req.approvedBy " +
           " left join fetch req.author " +
           " left join fetch req.historyItemsForPublic " +
           " left join fetch req.waypoints " +
           " left join fetch req.passenger" +
           " left join fetch req.purpose")
    List<RequestForPublic> findAll(@NotNull Sort sort);
    
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE from RequestForPublic")
    void deleteAll();
    
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "truncate table request.request_for_public CASCADE")
    void clearPublic();
    
    @Query("SELECT r FROM RequestForPublic r " +
           "WHERE r.approvalDeadline IS NOT NULL " +
           "AND r.status = :awaitingApprovalStatus " +
           "AND r.approvalDate IS NULL and r.approvalDeadline < :now")
    List<RequestForPublic> findRequestForPublicWithApprovalDeadlineViolation(LocalDateTime now, TripRequestStatus awaitingApprovalStatus);
    
    @Query("SELECT r FROM RequestForPublic r " +
           "WHERE (r.status = :publicOrderPaymentFormationStatus OR r.status = :publicPaymentAwaiting) " +
           "AND r.paymentDoneDatetime IS NULL and r.paymentDoneDeadline < :now " +
           "AND r.paymentDoneDeadlineState <> :deadlineStateRed")
    List<RequestForPublic> findRequestForPublicForSettingPaymentDoneDeadlineViolation(
            LocalDateTime now,
            TripRequestStatus publicOrderPaymentFormationStatus,
            TripRequestStatus publicPaymentAwaiting,
            DeadlineState deadlineStateRed
                                                                                     );
    
    @Query("select r.id from RequestForPublic r where r.payRequestId = :payRequestId")
    List<UUID> findAllByPayRequestId(UUID payRequestId);

    @Override
    default TransportTypeEnum type() {
        return TransportTypeEnum.PUBLIC;
    }
}
