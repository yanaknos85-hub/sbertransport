package ru.sberbank.ditsib.transport.request.database.dao;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.database.model.RequestForGroupTransfer;
import ru.sberbank.ditsib.transport.request.database.model.RequestForTaxi;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.RatingsDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Repository of requests
 */
@Repository
public interface RequestRepository extends JpaRepository<Request, UUID>, JpaSpecificationExecutor<Request> {
    
    /**
     * Find all requests authored by specified user
     *
     * @param authorId employee id
     *
     * @return list of qualified requests
     */
    List<Request> findAllByAuthorId(UUID authorId);
    
    /**
     * Найти все записи по человекочитаемому идентификатору
     * @param hrId человекочитаемый идентификатор
     * @return запрос
     */
    @Query("SELECT r FROM RequestForTaxi r LEFT JOIN FETCH r.taxiTrip  WHERE r.humanReadableId = :hrId ")
    Optional<RequestForTaxi> findByHumanReadableId(String hrId);
    
    @NotNull
    @Override
    @Query("select distinct req from Request req " +
           " left join fetch req.approvedBy " +
           " left join fetch req.author " +
           " left join fetch req.waypoints " +
           " left join fetch req.passenger" +
           " left join fetch req.purpose")
    List<Request> findAll();
    
    @NotNull
    @Override
    @Query("select distinct req from Request req " +
           " left join fetch req.approvedBy " +
           " left join fetch req.author " +
           " left join fetch req.waypoints " +
           " left join fetch req.passenger" +
           " left join fetch req.purpose")
    List<Request> findAll(@NotNull Sort sort);
    
    @NotNull
    @Query("SELECT request FROM Request request WHERE request.id = :id")
    @Override
    Optional<Request> findById(@NotNull UUID id);
    
    @Query("SELECT request FROM Request request WHERE request.status in (:terminalStatusSet) and passenger = " +
           ":employee")
    Page<Request> findByStatusSetAndEmployee(Set<TripRequestStatus> terminalStatusSet, Employee employee, Pageable var2);

    @Deprecated(since = "Используется только в тестах")
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE from Request")
    void deleteAll();

    @Deprecated(since = "Используется только в тестах")
    @Override
    @Transactional
    @Modifying
    @Query("update Request req set req.active=false where req.id = :requestId")
    void deleteById(@NotNull UUID requestId);
    
    @Query("SELECT count(r) from Request r where year(r.desiredDate) = :year and month(r.desiredDate) = :month and r.organizationId = " +
           ":organizationId and r.transportType = :transportType")
    long countAllByYearAndMonthAndOrganizationIdAndTransportType(int year, int month, UUID organizationId, TransportTypeEnum transportType);
    
    @Query("SELECT count(r) from Request r where year(r.desiredDate) = :year and month(r.desiredDate) = :month and r.organizationId = " +
           ":organizationId and r.transportType = :transportType and r.status in :executedStatuses")
    long countAllByYearAndMonthAndOrganizationIdAndTransportTypeAndStatuses(
            int year, int month, UUID organizationId, TransportTypeEnum transportType, List<TripRequestStatus> executedStatuses);
    
    @Query("SELECT sum(r.expected.cost) from Request r where r.expected.cost is not null and year(r.desiredDate) = :year and month(r.desiredDate) =" +
           " :month and r.organizationId = :organizationId and r.transportType = :transportType")
    Double sumAllByYearAndMonthAndOrganizationIdAndTransportType(int year, int month, UUID organizationId, TransportTypeEnum transportType);
    
    @Query("SELECT new ru.sberbank.ditsib.transport.request.dto.RatingsDTO(r.requestRating.rating, count(r)) from Request r " +
           "where r.requestRating.rating is not null and year(r.desiredDate) = :year and month(r.desiredDate) = :month and r.organizationId = :organizationId" +
           " and r.transportType = :transportType group by r.requestRating.rating")
    List<RatingsDTO> countAllPerStarsByYearAndMonthAndOrganizationIdAndTransportType(
            int year, int month, UUID organizationId, TransportTypeEnum transportType);
    
    @Query("SELECT count(r) from Request r where year(r.desiredDate) = :year and month(r.desiredDate) = :month and r.organizationId = :organizationId and r" +
           ".transportType = :transportType and (r.status in :executedStatuses or (r.status = :cancelStatus and r.statusCode in :statusCodes))")
    long countAllByYearAndMonthAndOrganizationIdAndTransportTypeAndStatusesOrCancelStatusAndStatusCodes(
            int year, int month, UUID organizationId, TransportTypeEnum transportType, List<TripRequestStatus> executedStatuses, TripRequestStatus cancelStatus,
            List<Integer> statusCodes);
    
    @Query("SELECT count(r) from Request r " +
           "where year(r.desiredDate) = :year and month(r.desiredDate) = :month " +
           "and r.organizationId = :organizationId " +
           "and r.transportType = :transportTypeTaxi " +
           "and ((r.status in :executedStatuses and r.deadlineState = :deadlineState) " +
           "     or (r.status = :cancelStatus and r.statusCode in :statusCodes))")
    long countWithViolationTaxi(
            int year, int month, UUID organizationId, TransportTypeEnum transportTypeTaxi,
            List<TripRequestStatus> executedStatuses, DeadlineState deadlineState,
            TripRequestStatus cancelStatus, List<Integer> statusCodes
                           );
    
    @Query("SELECT count(r) from Request r " +
           "where year(r.desiredDate) = :year and month(r.desiredDate) = :month " +
           "and r.organizationId = :organizationId " +
           "and r.transportType = :transportTypePersonal " +
           "and r.status in :executedStatuses " +
           "and r.isSlaExpired = true")
    long countWithViolationPersonal(
            int year, int month, UUID organizationId, TransportTypeEnum transportTypePersonal,
            List<TripRequestStatus> executedStatuses);
    
    @Query("SELECT count(r) from Request r " +
           "where year(r.desiredDate) = :year and month(r.desiredDate) = :month " +
           "and r.organizationId = :organizationId " +
           "and r.transportType = :transportTypePublic " +
           "and r.status in :executedStatuses " +
           "and r.isSlaExpired = true")
    long countWithViolationPublic(
            int year, int month, UUID organizationId, TransportTypeEnum transportTypePublic,
            List<TripRequestStatus> executedStatuses);
    
    
    @Query("SELECT count(r) from Request r " +
           "where r.organizationId = :organizationId " +
           "and year(r.desiredDate) = :year and month(r.desiredDate) = :month")
    long countAll(UUID organizationId, int year, int month);

    @Query("SELECT r FROM RequestForTaxi r " +
           "WHERE r.driverArrivedDeadline IS NOT NULL " +
           "AND (r.driverArrivedDatetime IS NULL AND r.driverArrivedDeadline < :now OR r.driverArrivedDatetime > r.driverArrivedDeadline) " +
           "AND (r.deadlineState IS NULL OR r.deadlineState = :deadlineStateNone) " +
           "AND r.status <> :cancelStatus"
    )
    List<RequestForTaxi> findRequestsForTaxiWithDriverArrivedDeadlineViolation(LocalDateTime now, DeadlineState deadlineStateNone,
                                                                               TripRequestStatus cancelStatus);
    
    @Query("SELECT r FROM RequestForGroupTransfer r " +
           "WHERE r.driverArrivedDeadline IS NOT NULL " +
           "AND (r.driverArrivedDatetime IS NULL AND r.driverArrivedDeadline < :now OR r.driverArrivedDatetime > r.driverArrivedDeadline) " +
           "AND (r.deadlineState IS NULL OR r.deadlineState = :deadlineStateNone) " +
           "AND r.status <> :cancelStatus"
    )
    List<RequestForGroupTransfer> findRequestsForGroupTransferWithDriverArrivedDeadlineViolation(LocalDateTime now, DeadlineState deadlineStateNone
            , TripRequestStatus cancelStatus);
    
    // Возвращает все id заявок, которые есть в request, но нет в reports, либо id заявок, статусы которых не совпадают
    @Query(nativeQuery = true, value =
            """
            select distinct cast(id as varchar) from (
            select subquery.id, subquery.request_status, sum(subquery.cnt) cnt
            from (
            select id, request_status, 1 as cnt from request.request_for_taxi where creation_time BETWEEN :creationTimeFrom and :creationTimeTo
            union
            select id, request_status, 2 from request.request_for_personal where creation_time BETWEEN :creationTimeFrom and :creationTimeTo
            union
            select id, request_status, 4 from request.request_for_public where creation_time BETWEEN :creationTimeFrom and :creationTimeTo
            union
            select id, request_status, 8 from request.request_for_carsharing where creation_time BETWEEN :creationTimeFrom and :creationTimeTo
            union
            select id, request_status, 16 from reports.request where creation_time BETWEEN :creationTimeFrom and :creationTimeTo) subquery
            group by subquery.id, subquery.request_status
            having sum(subquery.cnt) <= 16) requestForSync
            """)
    List<String> findAllRequestIdForSynchronize(LocalDateTime creationTimeFrom, LocalDateTime creationTimeTo);

    @Query("SELECT request.transportType FROM Request request where request.id = :id")
    Optional<TransportTypeEnum> findTransportType(UUID id);
}