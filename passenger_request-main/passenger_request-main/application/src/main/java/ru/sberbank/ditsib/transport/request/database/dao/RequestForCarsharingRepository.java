package ru.sberbank.ditsib.transport.request.database.dao;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.model.RequestForCarsharing;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface RequestForCarsharingRepository extends TypedRequestRepository<RequestForCarsharing> {
    
    Optional<RequestForCarsharing> findFirstByAuthorIdOrderByCreationTimeDesc(UUID authorId);
    
    @NotNull
    @EntityGraph(value = "request")
    @Override
    @Query("select distinct req from RequestForCarsharing req " +
           " left join fetch req.approvedBy " +
           " left join fetch req.author " +
           " left join fetch req.historyItemsForCarsharing " +
           " left join fetch req.waypoints " +
           " left join fetch req.passenger" +
           " left join fetch req.purpose")
    List<RequestForCarsharing> findAll();
    
    @NotNull
    @EntityGraph(value = "request")
    @Override
    @Query("select distinct req from RequestForCarsharing req " +
           " left join fetch req.approvedBy " +
           " left join fetch req.author " +
           " left join fetch req.historyItemsForCarsharing " +
           " left join fetch req.waypoints " +
           " left join fetch req.passenger" +
           " left join fetch req.purpose")
    List<RequestForCarsharing> findAll(@NotNull Sort sort);
    
    Optional<RequestForCarsharing> findFirstByAuthorAndStatusInOrderByCreationTimeDesc(Employee author, Set<TripRequestStatus> statusSet);
    
    @EntityGraph(value = "requestForCarsharingWithHistory")
    @Query("SELECT r FROM RequestForCarsharing r " +
           "LEFT JOIN r.historyItemsForCarsharing " +
           "WHERE r.approvalDeadline IS NOT NULL " +
           "AND r.status = :awaitingApprovalStatus " +
           "AND r.approvalDate IS NULL and r.approvalDeadline < :now")
    List<RequestForCarsharing> findAllWithApprovalDeadlineViolation(LocalDateTime now, TripRequestStatus awaitingApprovalStatus);

    @Override
    default TransportTypeEnum type() {
        return TransportTypeEnum.CARSHARING;
    }
}
