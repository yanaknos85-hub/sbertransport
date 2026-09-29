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
import ru.sberbank.ditsib.transport.request.database.model.RequestForGroupTransfer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RequestForGroupTransferRepository extends TypedRequestRepository<RequestForGroupTransfer> {
    
    @EntityGraph(value = "request")
    @Override
    @Query("select distinct req from RequestForGroupTransfer req " +
           " left join fetch req.approvedBy " +
           " left join fetch req.author " +
           " left join fetch req.historyItemsForGroupTransfer" +
           " left join fetch req.waypoints " +
           " left join fetch req.passenger" +
           " left join fetch req.purpose ")
    @NonNull
    List<RequestForGroupTransfer> findAll();
    
    @EntityGraph(value = "request")
    @Override
    @Query("select distinct req from RequestForGroupTransfer req " +
           " left join fetch req.approvedBy " +
           " left join fetch req.author " +
           " left join fetch req.historyItemsForGroupTransfer " +
           " left join fetch req.waypoints " +
           " left join fetch req.passenger" +
           " left join fetch req.purpose ")
    @NonNull
    List<RequestForGroupTransfer> findAll(@NonNull Sort sort);
    
    @EntityGraph(value = "request")
    @Query("SELECT request FROM RequestForGroupTransfer request WHERE request.id = :id")
    @Override
    @NonNull
    Optional<RequestForGroupTransfer> findById(@NonNull UUID id);
    
    @Query("""
           select distinct request from RequestForGroupTransfer request
            where request.sentToContractor = false and (request.status = 'GROUP_TRANSFER_APPROVED')
            and request.desiredDate > :notExpiredTime
            order by request.desiredDate asc""")
    List<RequestForGroupTransfer> findNewReadyToSend(LocalDateTime notExpiredTime);
    
    
    @Override
    @EntityGraph(value = "requestGroupTransferForDispatcher")
    @NonNull
    Page<RequestForGroupTransfer> findAll(Specification<RequestForGroupTransfer> var1, @NonNull Pageable var2);
    
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE from RequestForGroupTransfer ")
    void deleteAll();
    
    @Query("SELECT r FROM RequestForGroupTransfer r " +
           "WHERE r.approvalDeadline IS NOT NULL " +
           "AND r.status = :awaitingApprovalStatus " +
           "AND r.approvalDate IS NULL and r.approvalDeadline < :now")
    List<RequestForGroupTransfer> findRequestForGroupTransferWithApprovalDeadlineViolation(LocalDateTime now, TripRequestStatus awaitingApprovalStatus);

    @Override
    default TransportTypeEnum type() {
        return TransportTypeEnum.GROUP_TRANSFER;
    }

}
